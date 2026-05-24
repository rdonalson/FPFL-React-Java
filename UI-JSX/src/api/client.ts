// src/api/client.ts
import axios, { AxiosError, AxiosInstance, AxiosResponse } from 'axios';
import { AppConfig } from '@/config/appConfig';
import { useSessionStore } from '@/app/state/sessionStore';

/**
 * Fire-and-forget log sender
 * Uses runtime/build-time config for the API base URL.
 */
function sendLogToServer({ log }: { log: any }): void {
  try {
    const baseUrl = AppConfig.get().api.baseUrl;
    // best-effort, don't block or throw
    void fetch(`${baseUrl.replace(/\/$/, '')}/client-logs`, {
      method: 'POST',
      body: JSON.stringify(log),
      headers: { 'Content-Type': 'application/json' },
      // don't wait for response
    }).catch(() => {});
  } catch {
    // swallow any errors — logging must never break the app
  }
}

function createClient(): AxiosInstance {
  const cfg = AppConfig.get();
  const instance = axios.create({
    baseURL: cfg.api.baseUrl,
    timeout: cfg.api.timeoutMs,
    headers: {
      'Content-Type': 'application/json',
    },
  });

  // ============================
  // REQUEST INTERCEPTOR
  // ============================
  instance.interceptors.request.use(config => {
    const correlationId = crypto.randomUUID();

    // Normalize headers
    config.headers = config.headers ?? {};

    // Add correlation ID
    (config.headers as Record<string, unknown>)['X-Correlation-ID'] = correlationId;

    // Pull fresh session values
    const { accessToken, userId } = useSessionStore.getState();

    // Add Authorization header
    if (accessToken) {
      (config.headers as Record<string, unknown>)['Authorization'] = `Bearer ${accessToken}`;
    }

    // Add userId header (optional but useful)
    if (userId) {
      (config.headers as Record<string, unknown>)['X-User-Id'] = userId;
    }

    // Local DevTools log (respect debug flag)
    if (cfg.session.debug) {
      // eslint-disable-next-line no-console
      console.debug('[API REQUEST]', {
        url: config.url,
        method: config.method,
        correlationId,
        userId,
        payload: config.data,
      });
    }

    return config;
  });

  // ============================
  // RESPONSE INTERCEPTOR
  // ============================
  instance.interceptors.response.use(
    (response: AxiosResponse) => {
      if (cfg.session.debug) {
        // eslint-disable-next-line no-console
        console.debug('[API RESPONSE]', {
          url: response.config.url,
          status: response.status,
          correlationId: response.config.headers?.['X-Correlation-ID'],
        });
      }

      return response; // IMPORTANT: return full AxiosResponse
    },

    // ============================
    // ERROR INTERCEPTOR
    // ============================
    (error: AxiosError) => {
      const normalized = {
        status: error.response?.status ?? 0,
        message: (error.response?.data as any)?.message ?? error.message ?? 'Unknown error',
        details: error.response?.data ?? null,
        correlationId: error.config?.headers?.['X-Correlation-ID'],
        url: error.config?.url,
      };

      // eslint-disable-next-line no-console
      console.error('[API ERROR]', normalized);

      sendLogToServer({
        log: {
          level: 'error',
          url: normalized.url,
          status: normalized.status,
          message: normalized.message,
          correlationId: normalized.correlationId,
          details: normalized.details,
          // include environment for easier debugging
          env: cfg.app.nodeEnv,
        },
      });

      return Promise.reject(normalized);
    },
  );

  return instance;
}

export const apiClient = createClient();
