// src/features/catalog-command/transactions/api/itemApi.ts
import { ItemClient } from '@/api/generated/ItemClient';
import { Item } from '../types/Item';
import { useSessionStore } from '@/app/state/sessionStore';
import { unwrap } from '@/api/utils/responseHelpers';

const DEFAULT_USER_KEY = 'userId';
const INITIAL_AMOUNT_ITEM_TYPE = 3;

/**
 * Resolve the current session user id.
 * Priority:
 *  1) Zustand session store (runtime, preferred)
 *  2) sessionStorage (page reload fallback)
 *  3) empty string (guest fallback)
 */
function getSessionUserId(): string {
  try {
    const fromStore = useSessionStore.getState?.().userId;
    if (fromStore) return fromStore;
  } catch {
    // ignore if Zustand isn't available for some reason
  }

  const fromStorage = sessionStorage.getItem(DEFAULT_USER_KEY);
  return fromStorage ?? '';
}

/* --- InitialAmount helpers --- */

/**
 * Update InitialAmount for current user.
 * - Only updates the single InitialAmount item identified by id.
 * - Enforces business defaults: fkItemType = 3, name = "IA", fkPeriod = 0.
 * - Caller must provide the id of the existing InitialAmount.
 */
export async function updateInitialAmountForCurrentUser(id: number, amount: number): Promise<Item> {
  const userId = getSessionUserId();
  const payload: Item = {
    userId,
    name: 'IA',
    amount,
    fkItemType: INITIAL_AMOUNT_ITEM_TYPE,
    fkPeriod: 0,
    dateRangeReq: false,
  };

  const res = await ItemClient.update(id, payload);
  return unwrap<Item>(res, null, true) as Item;
}

/**
 * Fetch the single InitialAmount for the current session user.
 * Returns the first item or null.
 */
export async function fetchInitialAmountForCurrentUser(): Promise<Item | null> {
  const userId = getSessionUserId();
  const res = await ItemClient.getByUserAndType(userId, INITIAL_AMOUNT_ITEM_TYPE);
  const arr = unwrap<Item[]>(res, []); // fallback is non-null => returns Item[]
  return arr.length > 0 ? arr[0] : null;
}

/**
 * Create InitialAmount for current user.
 * Enforces fkItemType = 3, name = "IA", fkPeriod = 0 and minimal payload.
 */
export async function createInitialAmountForCurrentUser(amount: number): Promise<Item> {
  const userId = getSessionUserId();
  const payload: Item = {
    userId,
    name: 'IA',
    amount,
    fkItemType: INITIAL_AMOUNT_ITEM_TYPE,
    fkPeriod: 0,
    dateRangeReq: false,
  };

  const res = await ItemClient.create(payload);
  return unwrap<Item>(res, null, true) as Item;
}

/* --- Generic item operations (Credit/Debit and others) --- */

export async function getItemsForUserAndType(userId: string, itemType: number): Promise<Item[]> {
  const res = await ItemClient.getByUserAndType(userId, itemType);
  return unwrap<Item[]>(res, []); // fallback non-null => returns Item[]
}

export async function getItemById(id: number): Promise<Item | null> {
  const res = await ItemClient.getById(id);
  return unwrap<Item>(res, null);
}

export async function createItem(payload: Item): Promise<Item> {
  // ensure required fields for backend
  if (!payload.fkItemType && payload.ItemType?.Id) payload.fkItemType = payload.ItemType.Id;
  if (!payload.userId) payload.userId = getSessionUserId();

  const res = await ItemClient.create(payload);
  return unwrap<Item>(res, null, true) as Item;
}

export async function updateItem(id: number, payload: Item): Promise<Item> {
  const res = await ItemClient.update(id, payload);
  return unwrap<Item>(res, null, true) as Item;
}

export async function deleteItem(id: number): Promise<void> {
  const res = await ItemClient.remove(id);
  unwrap<void>(res, undefined, true);
}

export async function fetchItemTypes() {
  const res = await ItemClient.fetchItemTypes();
  return unwrap(res, []);
}

export async function fetchTimePeriods() {
  const res = await ItemClient.fetchTimePeriods();
  return unwrap(res, []);
}
