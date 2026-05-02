// src/api/utils/responseHelpers.ts
import type { ApiResponse } from '@/api/models/ApiResponse';

/**
 * unwrap overloads:
 * 1) When a non-null fallback is provided, unwrap returns T (never null).
 * 2) When fallback is omitted or null, unwrap returns T | null.
 *
 * The function accepts either:
 * - ApiResponse<T> (wrapped)
 * - T (already unwrapped)
 * - null | undefined
 */

/* Overload 1: non-null fallback -> returns T (non-null) */
export function unwrap<T>(
  res: ApiResponse<T> | T | null | undefined,
  fallback: T,
  throwOnMissing?: boolean,
): T;

/* Overload 2: nullable fallback or omitted -> returns T | null */
export function unwrap<T>(
  res: ApiResponse<T> | T | null | undefined,
  fallback?: T | null,
  throwOnMissing?: boolean,
): T | null;

/* Implementation */
export function unwrap<T>(
  res: ApiResponse<T> | T | null | undefined,
  fallback: T | null = null,
  throwOnMissing = false,
): T | null {
  if (res === null || res === undefined) {
    if (throwOnMissing) throw new Error('No response from API');
    return fallback;
  }

  // Case A: ApiResponse<T> wrapper (has 'data' property)
  if (typeof res === 'object' && res !== null && 'data' in (res as any)) {
    const api = res as ApiResponse<T>;
    if (api.data !== undefined && api.data !== null) {
      return api.data;
    }
    if (throwOnMissing) {
      throw new Error(api.message ?? 'No data returned from API');
    }
    return fallback;
  }

  // Case B: already-unwrapped T
  return res as T;
}
