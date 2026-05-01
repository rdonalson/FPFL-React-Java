// src/features/catalog-command/transactions/api/initialAmountApi.ts
import { InitialAmountClient } from '@/api/generated/InitialAmountClient';
import { unwrap } from '@/api/utils/responseHelpers';
import { InitialAmountResponse } from '../types/InitialAmount';

/**
 * Fetch the InitialAmount for the current session user.
 * Returns InitialAmountResponse | null.
 */
export async function fetchInitialAmount(): Promise<InitialAmountResponse | null> {
  const res = await InitialAmountClient.fetchForCurrentUser();
  // res: ApiResponse<InitialAmountResponse | null>
  return unwrap<InitialAmountResponse | null>(res, null);
}

/**
 * Create InitialAmount for current user.
 * Guaranteed non-null on success.
 */
export async function createInitialAmount(amount: number): Promise<InitialAmountResponse> {
  const res = await InitialAmountClient.create(amount);
  // Use a non-null fallback so unwrap<T> returns T (not T | null)
  return unwrap<InitialAmountResponse>(res, {} as InitialAmountResponse, true);
}

/**
 * Update InitialAmount for current user.
 * Guaranteed non-null on success.
 */
export async function updateInitialAmount(
  id: number,
  amount: number,
): Promise<InitialAmountResponse> {
  const res = await InitialAmountClient.update(id, amount);
  return unwrap<InitialAmountResponse>(res, {} as InitialAmountResponse, true);
}
