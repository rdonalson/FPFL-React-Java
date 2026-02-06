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
    config.headers["X-Correlation-ID"] = crypto.randomUUID();
    return config;
  });

  // Auto‑unwrap ApiResponse<T>
  instance.interceptors.response.use(
    (response) => {
      const body = response;

      // Detect your ApiResponse<T> shape
      if (
        body &&
        typeof body === "object" &&
        "data" in body &&
        "status" in body &&
        "statusText" in body
      ) {
        return body.data; // return ONLY the inner data
      }

      // Otherwise return raw body
      return body;
    },
    (error: AxiosError) => {
      const normalized = {
        status: error.response?.status ?? 0,
        message:
          (error.response?.data as any)?.message ??
          error.message ??
          "Unknown error",
        details: error.response?.data ?? null,
      };

      return Promise.reject(normalized);
    }
  );

  return instance;
}

export const apiClient = createClient();