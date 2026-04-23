// src/api/client.ts
import axios, { AxiosError, AxiosInstance } from 'axios';
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
    config.headers['X-Correlation-ID'] = correlationId;

    // Pull session values at request time (always fresh)
    const { userId, token } = useSessionStore.getState();

    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`;
    }

    if (userId) {
      config.headers['X-User-Id'] = userId;
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
  instance.interceptors.response.use(
    response => {
      // Local DevTools log
      console.log('[API RESPONSE]', {
        url: response.config.url,
        status: response.status,
        correlationId: response.config.headers['X-Correlation-ID'],
      });

      return response.data;
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

      // Local DevTools log
      console.error('[API ERROR]', {
        url: normalized.url,
        status: normalized.status,
        message: normalized.message,
        correlationId: normalized.correlationId,
        details: normalized.details,
      });

      // Send to backend logging API
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

      return Promise.reject(normalized);
    },
  );

  return instance;
}

export const apiClient = createClient();
