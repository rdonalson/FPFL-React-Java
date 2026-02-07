// src/api/config.ts

/**
 * Reads environment variables and exposes typed config values.
 * This keeps all environment handling in one place.
 */
export const apiConfig = {
  // Base URL for the docker API, defaults to localhost if not set
  //baseUrl: import.meta.env.VITE_API_URL ?? 'http://localhost:8000',
  baseUrl: import.meta.env.VITE_API_URL ?? 'http://localhost:9000',
  timeout: 10_000,
};
