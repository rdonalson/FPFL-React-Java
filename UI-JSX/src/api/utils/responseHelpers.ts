// src/api/utils/responseHelpers.ts
import type { ApiResponse } from '@/api/models/ApiResponse';

/**
 * unwrap overloads:
 * - When a non-null fallback is provided, unwrap returns T (never null).
 * - When fallback is omitted or null, unwrap returns T | null.
 */
export function unwrap<T>(
  res: ApiResponse<T> | null | undefined,
  fallback: T,
  throwOnMissing?: boolean,
): T;
export function unwrap<T>(
  res: ApiResponse<T> | null | undefined,
  fallback?: T | null,
  throwOnMissing?: boolean,
): T | null;
export function unwrap<T>(
  res: ApiResponse<T> | null | undefined,
  fallback: T | null = null,
  throwOnMissing = false,
): T | null {
  if (!res) {
    if (throwOnMissing) throw new Error('No response from API');
    return fallback;
  }

  if (res.data !== undefined && res.data !== null) return res.data as T;

  if (throwOnMissing) {
    throw new Error(res.message ?? 'No data returned from API');
  }

  return fallback;
}
