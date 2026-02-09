// src/api/client.ts
import axios, { AxiosError, AxiosInstance } from "axios";
import { apiConfig } from "./config";

function createClient(): AxiosInstance {
  const instance = axios.create({
    baseURL: apiConfig.baseUrl,
    timeout: apiConfig.timeout,
    headers: {
      "Content-Type": "application/json",
    },
  });

  // Add correlation ID for tracing
  instance.interceptors.request.use((config) => {
    const correlationId = crypto.randomUUID();
    config.headers["X-Correlation-ID"] = correlationId;

    // Log outgoing request
    console.log("[API REQUEST]", {
      url: config.url,
      method: config.method,
      correlationId,
      payload: config.data,
    });

    return config;
  });

  instance.interceptors.response.use(
    (response) => {
      // Log successful response
      console.log("[API RESPONSE]", {
        url: response.config.url,
        status: response.status,
        correlationId: response.config.headers["X-Correlation-ID"],
      });

      return response.data;
    },
    (error: AxiosError) => {
      const normalized = {
        status: error.response?.status ?? 0,
        message:
          (error.response?.data as any)?.message ??
          error.message ??
          "Unknown error",
        details: error.response?.data ?? null,
        correlationId: error.config?.headers?.["X-Correlation-ID"],
      };

      // Log error
      console.error("[API ERROR]", {
        url: error.config?.url,
        status: normalized.status,
        message: normalized.message,
        correlationId: normalized.correlationId,
        details: normalized.details,
      });

      return Promise.reject(normalized);
    }
  );

  return instance;
}

export const apiClient = createClient();