// src/api/utils/apiParse.ts
import type { ApiResponse } from '@/api/models/ApiResponse';
import type { AxiosResponse } from 'axios';

/**
 * parseApi adapter
 * - If payload is an AxiosResponse, extract its .data and continue.
 * - If payload already looks like ApiResponse<T>, return it.
 * - If payload is raw data, wrap it as ApiResponse<T>.
 * - If payload is null/undefined, return { data: undefined }.
 */
export async function parseApi<T>(payload: unknown): Promise<ApiResponse<T>> {
  // 1) AxiosResponse case: has 'config' and 'status' properties
  if (
    payload &&
    typeof payload === 'object' &&
    'config' in (payload as any) &&
    'status' in (payload as any)
  ) {
    const axiosRes = payload as AxiosResponse;
    // axiosRes.data might be ApiResponse<T> or raw T
    payload = axiosRes.data;
  }

  // 2) Null / undefined
  if (payload === null || payload === undefined) {
    return { data: undefined } as ApiResponse<T>;
  }

  // 3) ApiResponse<T> shape detection
  if (typeof payload === 'object' && payload !== null && 'data' in (payload as any)) {
    // Heuristic: treat it as ApiResponse<T>
    return payload as ApiResponse<T>;
  }

  // 4) Raw payload -> wrap it
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
