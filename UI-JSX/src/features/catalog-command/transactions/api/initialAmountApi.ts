import { ApiResponse } from '@/api/models/ApiResponse';

import { InitialAmountClient } from '@/api/generated/InitialAmountClient';
import { Item } from '../types/Item';
import { unwrap } from '@/api/utils/responseHelpers';
import { InitialAmountResponse } from '../types/InitialAmount';

/* -------------------------------------------------------
 * InitialAmount API (clean, standalone)
 * ----------------------------------------------------- */
/**
 * Fetch the single InitialAmount for the current session user.
 * Returns the first item or null.
 */
export async function fetchInitialAmount(): Promise<ApiResponse<InitialAmountResponse | null>> {
  const res = await InitialAmountClient.fetchForCurrentUser();
  const arr = unwrap<InitialAmountResponse>(res | null); // always returns array
  return arr.length > 0 ? arr[0] : null;
}

/**
 * Create InitialAmount for current user.
 * Enforces:
 *  - fkItemType = 3
 *  - name = "IA"
 *  - fkPeriod = 0
 */
export async function createInitialAmount(amount: number): Promise<Item> {
  const res = await InitialAmountClient.create(amount);
  return unwrap<InitialAmountResponse>(res, null, true) as InitialAmountResponse;
}

/**
 * Update InitialAmount for current user.
 * Caller must provide the existing InitialAmount id.
 */
export async function updateInitialAmount(id: number, amount: number): Promise<Item> {
  const userId = getSessionUserId();

  const payload: Item = {
    userId,
    name: 'IA',
    amount,
    fkItemType: INITIAL_AMOUNT_ITEM_TYPE,
    fkPeriod: 0,
    // dateRangeReq intentionally omitted
  };

  const res = await InitialAmountClient.update(id, payload);
  return unwrap<Item>(res, null, true) as Item;
}
