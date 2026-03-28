"use client";

import type { AxiosInstance } from "axios";

export function attachInterceptors(http: AxiosInstance) {
  http.interceptors.request.use((config) => {
    config.headers["X-Correlation-ID"] = crypto.randomUUID();
    return config;
  });

  http.interceptors.response.use(
    (response) => response,
    (error) => {
      const normalized = {
        status: error.response?.status,
        message: error.response?.data?.message ?? "Unexpected error",
        details: error.response?.data,
      };
      return Promise.reject(normalized);
    }
  );
}
