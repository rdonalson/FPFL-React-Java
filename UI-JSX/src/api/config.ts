// src/api/config.ts

/**
 * Reads environment variables and exposes typed config values.
 * This keeps all environment handling in one place.
 */
export const apiConfig = {
  baseUrl: import.meta.env.VITE_API_URL ?? 'http://localhost:9000',
  timeout: 10_000,
};

console.log('Axios baseURL:', import.meta.env.VITE_API_URL);
console.log('Node Environment:', import.meta.env.VITE_NODE_ENV);
