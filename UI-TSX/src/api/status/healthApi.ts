// src/app/api/healthApi.ts
//
// Talks to Spring Boot Actuator. Plain fetch, no dependency on the app's axios
// instance — if /actuator/** sits behind the JWT filter, swap the fetch in
// getJson() for your configured client so the auth header is sent.

const RAW_API_URL = (import.meta.env.VITE_API_URL as string | undefined) ?? '';

/** The app's API root, e.g. http://localhost:8080/api ('' = same origin). */
export const API_BASE_URL = RAW_API_URL.replace(/\/+$/, '');

/**
 * Actuator is mounted at the SERVER root, not under the API path, so a
 * VITE_API_URL of http://localhost:8080/api must probe
 * http://localhost:8080/actuator — hence stripping a trailing /api or /api/v1.
 * Override with VITE_ACTUATOR_URL if your management port/path differs
 * (e.g. management.server.port=9090).
 */
const ACTUATOR_ROOT =
  (import.meta.env.VITE_ACTUATOR_URL as string | undefined)?.replace(/\/+$/, '') ??
  API_BASE_URL.replace(/\/api(\/v\d+)?$/i, '');

export const ACTUATOR_URL = `${ACTUATOR_ROOT}/actuator`;
export const HEALTH_URL = `${ACTUATOR_URL}/health`;
export const INFO_URL = `${ACTUATOR_URL}/info`;

/**
 * Deliberately shorter than VITE_API_TIMEOUT_MS (30s): a status page should
 * report "not answering" quickly rather than hang for half a minute.
 */
const PROBE_TIMEOUT_MS = Number(import.meta.env.VITE_HEALTH_TIMEOUT_MS ?? 5000) || 5000;

export type HealthStatus = 'UP' | 'DOWN' | 'OUT_OF_SERVICE' | 'UNKNOWN' | string;

export interface ActuatorComponent {
  status: HealthStatus;
  details?: Record<string, unknown>;
  components?: Record<string, ActuatorComponent>;
}

export interface ActuatorHealth {
  status: HealthStatus;
  components?: Record<string, ActuatorComponent>;
}

export interface ActuatorInfo {
  app?: { name?: string; version?: string; description?: string };
  build?: { name?: string; version?: string; time?: string; artifact?: string };
  git?: { branch?: string; commit?: { id?: string; time?: string } };
  [key: string]: unknown;
}

export interface ProbeResult<T> {
  ok: boolean;
  /** HTTP status code, or 0 when the request never reached the server. */
  httpStatus: number;
  latencyMs: number;
  url: string;
  body?: T;
  error?: string;
  /** Short "what to check" line for the failure. */
  hint?: string;
  /** Server answered, but the browser refused to let us read it (CORS). */
  blocked?: boolean;
  checkedAt: Date;
}

/**
 * A no-cors request returns an opaque response we can't read, but it only
 * RESOLVES if something actually answered. That separates "API is down" from
 * "API is up and CORS blocked the read" — the browser reports both as
 * "Failed to fetch".
 */
async function isReachable(url: string, timeoutMs: number): Promise<boolean> {
  const controller = new AbortController();
  const timer = window.setTimeout(() => controller.abort(), timeoutMs);

  try {
    await fetch(url, { mode: 'no-cors', cache: 'no-store', signal: controller.signal });
    return true;
  } catch {
    return false;
  } finally {
    window.clearTimeout(timer);
  }
}

async function getJson<T>(url: string, timeoutMs = PROBE_TIMEOUT_MS): Promise<ProbeResult<T>> {
  const controller = new AbortController();
  const timer = window.setTimeout(() => controller.abort(), timeoutMs);
  const started = performance.now();

  try {
    const response = await fetch(url, {
      method: 'GET',
      // Actuator answers with application/vnd.spring-boot.actuator.v3+json;
      // asking for application/json alone can draw a 406 on some setups.
      headers: { Accept: 'application/vnd.spring-boot.actuator.v3+json, application/json' },
      // With show-details=when_authorized the server only returns component
      // detail to an authenticated caller. Set VITE_HEALTH_WITH_CREDENTIALS=true
      // to send cookies cross-origin — the API must then echo a SPECIFIC origin
      // (not *) plus Access-Control-Allow-Credentials, or the call fails.
      credentials:
        import.meta.env.VITE_HEALTH_WITH_CREDENTIALS === 'true' ? 'include' : 'same-origin',
      signal: controller.signal,
    });

    const latencyMs = Math.round(performance.now() - started);

    // Actuator answers 503 with a JSON body when a component is DOWN, so parse
    // regardless; a non-JSON body means we hit something that isn't Actuator.
    const text = await response.text();
    let body: T | undefined;
    try {
      body = JSON.parse(text) as T;
    } catch {
      body = undefined;
    }

    let hint: string | undefined;
    if (!body) {
      hint = `Response wasn't JSON — ${url} is probably not the actuator endpoint (got ${
        text.slice(0, 40) || 'an empty body'
      }…)`;
    } else if (response.status === 401 || response.status === 403) {
      hint = 'Actuator is behind Spring Security — permit it, or send the auth header.';
    } else if (response.status === 404) {
      hint = 'Endpoint not found — check management.endpoints.web.exposure.include=health,info.';
    }

    return {
      ok: response.ok && !!body,
      httpStatus: response.status,
      latencyMs,
      url,
      body,
      error: response.ok ? undefined : `HTTP ${response.status} ${response.statusText}`,
      hint,
      checkedAt: new Date(),
    };
  } catch (err) {
    const latencyMs = Math.round(performance.now() - started);
    const aborted = err instanceof DOMException && err.name === 'AbortError';
    const blocked = !aborted && (await isReachable(url, timeoutMs));

    return {
      ok: false,
      httpStatus: 0,
      latencyMs,
      url,
      blocked,
      error: aborted
        ? `No response within ${timeoutMs / 1000}s`
        : blocked
          ? 'Blocked by the browser (CORS)'
          : (err as Error).message,
      hint: aborted
        ? 'The API accepted the connection but never answered.'
        : blocked
          ? `The API answered at ${url}, but the browser refused to hand the response to this origin (${window.location.origin}). Add that origin to the API's CORS config, or route through the Vite dev proxy so the call is same-origin.`
          : `Nothing answered at ${url} — check the API is running and the port matches VITE_API_URL.`,
      checkedAt: new Date(),
    };
  } finally {
    window.clearTimeout(timer);
  }
}

export function fetchHealth() {
  return getJson<ActuatorHealth>(HEALTH_URL);
}

export function fetchInfo() {
  return getJson<ActuatorInfo>(INFO_URL);
}
