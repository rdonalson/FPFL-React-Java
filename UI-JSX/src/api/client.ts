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

  // Return full ApiResponse<T>
  instance.interceptors.response.use(
    (response) => {
      return response.data; // <-- THIS is your ApiResponse<T>
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