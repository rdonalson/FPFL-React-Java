// src/api/client.ts
import axios, { AxiosError, AxiosInstance, AxiosResponse } from 'axios';
import { apiConfig } from './config';
import { useSessionStore } from '../app/state/sessionStore';

// Fire-and-forget log sender
function sendLogToServer({ log }: { log: any }): void {
  // Do NOT block the UI — fire and forget
  fetch(`${apiConfig.baseUrl}/client-logs`, {
    method: 'POST',
    body: JSON.stringify(log),
    headers: { 'Content-Type': 'application/json' },
  }).catch(() => {
    // Intentionally ignore failures — logging must never break the app
  });
}

function createClient(): AxiosInstance {
  const instance = axios.create({
    baseURL: apiConfig.baseUrl,
    timeout: apiConfig.timeout,
    headers: {
      'Content-Type': 'application/json',
    },
  });

  // ============================
  // REQUEST INTERCEPTOR
  // ============================
  instance.interceptors.request.use(config => {
    const correlationId = crypto.randomUUID();
    // TS: headers can be undefined or a plain object; coerce to any for assignment
    (config.headers as any) = (config.headers as any) || {};
    (config.headers as any)['X-Correlation-ID'] = correlationId;

    // Pull session values at request time (always fresh)
    const { userId, token } = useSessionStore.getState();

    if (token) {
      (config.headers as any)['Authorization'] = `Bearer ${token}`;
    }

    if (userId) {
      (config.headers as any)['X-User-Id'] = userId;
    }

    // Local DevTools log
    console.log('[API REQUEST]', {
      url: config.url,
      method: config.method,
      correlationId,
      userId,
      payload: config.data,
    });

    return config;
  });

  // ============================
  // RESPONSE INTERCEPTOR
  // ============================
  // IMPORTANT: Do NOT unwrap response.data here. Return the full AxiosResponse so callers
  // can access the ApiResponse<T> wrapper at res.data.
  instance.interceptors.response.use(
    (response: AxiosResponse) => {
      // Local DevTools log
      console.log('[API RESPONSE]', {
        url: response.config.url,
        status: response.status,
        correlationId: (response.config.headers as any)?.['X-Correlation-ID'],
      });

      // Return the full AxiosResponse<ApiResponse<T>>
      return response;
    },

    // ============================
    // ERROR INTERCEPTOR
    // ============================
    (error: AxiosError) => {
      const normalized = {
        status: error.response?.status ?? 0,
        message: (error.response?.data as any)?.message ?? error.message ?? 'Unknown error',
        details: error.response?.data ?? null,
        correlationId: (error.config?.headers as any)?.['X-Correlation-ID'],
        url: error.config?.url,
      };

      // Local DevTools log
      console.error('[API ERROR]', {
        url: normalized.url,
        status: normalized.status,
        message: normalized.message,
        correlationId: normalized.correlationId,
        details: normalized.details,
      });

      // Send to backend logging API (fire-and-forget)
      sendLogToServer({
        log: {
          level: 'error',
          url: normalized.url,
          status: normalized.status,
          message: normalized.message,
          correlationId: normalized.correlationId,
          details: normalized.details,
        },
      });

      // Reject with a normalized error object (not the raw AxiosError)
      return Promise.reject(normalized);
    },
  );

  return instance;
}

export const apiClient = createClient();
