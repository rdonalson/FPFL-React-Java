// src/api/utils/apiParse.ts
import type { ApiResponse } from '@/api/models/ApiResponse';

/**
 * parseApi adapter
 * - If payload already looks like ApiResponse<T>, return it.
 * - If payload is raw data, wrap it as ApiResponse<T>.
 * - If payload is null/undefined, return { data: undefined }.
 */
export async function parseApi<T>(payload: unknown): Promise<ApiResponse<T>> {
  if (payload === null || payload === undefined) {
    return { data: undefined } as ApiResponse<T>;
  }

  if (typeof payload === 'object' && payload !== null && 'data' in (payload as any)) {
    return payload as ApiResponse<T>;
  }

  return { data: payload as T } as ApiResponse<T>;
}

/**
 * Helper to call apiClient and normalize errors to throw Error(message)
 * - fn should be a function that returns the axios call promise (e.g. () => apiClient.get(...))
 */
export async function callAndParse<T>(fn: () => Promise<unknown>): Promise<ApiResponse<T>> {
  try {
    const result = await fn();
    return await parseApi<T>(result);
  } catch (err: any) {
    // apiClient's error interceptor normalizes errors into a plain object:
    // { status, message, details, correlationId, url }
    if (err && typeof err === 'object') {
      const message = (err as any).message ?? JSON.stringify(err);
      throw new Error(message);
    }
    throw err;
  }
}
