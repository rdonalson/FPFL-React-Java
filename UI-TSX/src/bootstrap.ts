// src/bootstrap.ts
/**
 * Load runtime configuration early in app startup.
 * - Tries server-injected window.__RUNTIME_CONFIG__ first
 * - Falls back to fetching /config.json
 * - Times out after a short period to avoid blocking the app forever
 */

type RuntimeConfigShape = Record<string, unknown>;

const RUNTIME_CONFIG_URL = '/config.json';
const FETCH_TIMEOUT_MS = 2000;

async function fetchWithTimeout(url: string, timeout = FETCH_TIMEOUT_MS) {
  const controller = new AbortController();
  const id = setTimeout(() => controller.abort(), timeout);
  try {
    const res = await fetch(url, { signal: controller.signal, cache: 'no-store' });
    clearTimeout(id);
    if (!res.ok) throw new Error(`Failed to fetch ${url}: ${res.status}`);
    return await res.json();
  } finally {
    clearTimeout(id);
  }
}

/**
 * Returns runtime configuration object or null.
 * Priority:
 *  1) window.__RUNTIME_CONFIG__ injected by server
 *  2) /config.json fetched from public
 *  3) null to indicate use of build-time defaults
 */
export async function loadRuntimeConfig(): Promise<RuntimeConfigShape | null> {
  // 1. If server injected config exists, use it
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const injected = (window as any).__RUNTIME_CONFIG__;
  if (injected && typeof injected === 'object') {
    return injected as RuntimeConfigShape;
  }

  // 2. Try to fetch /config.json with a short timeout
  try {
    const runtime = await fetchWithTimeout(RUNTIME_CONFIG_URL);
    if (runtime && typeof runtime === 'object') return runtime as RuntimeConfigShape;
  } catch (err) {
    // swallow; we'll fall back to build-time defaults
    // Optionally log in dev
    // eslint-disable-next-line no-console
    console.debug('loadRuntimeConfig: no runtime config found', err);
  }

  return null;
}
