// transactions/itemApi.ts
import { itemClient } from '@/api/generated/ItemClient';
import { Item } from '../types/Item';

/**
 * Business-level API for Items used by transactions UI.
 * - wraps itemClient and applies small business rules (defaults, session user).
 * - exposes functions for InitialAmount (special case) and general items (full CRUD).
 */

const DEFAULT_USER_KEY = 'userId';
const ZERO_UUID = '00000000-0000-0000-0000-000000000000';
const INITIAL_AMOUNT_ITEM_TYPE = 3;

function getSessionUserId(): string {
  return sessionStorage.getItem(DEFAULT_USER_KEY) ?? ZERO_UUID;
}

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
  return itemClient.update(id, payload);
}

/**
 * Fetch the single InitialAmount for the current session user.
 * Returns the first item or null.
 */
export async function fetchInitialAmountForCurrentUser(): Promise<Item | null> {
  const userId = getSessionUserId();
  const arr = await itemClient.getByUserAndType(userId, INITIAL_AMOUNT_ITEM_TYPE);
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
  return itemClient.create(payload);
}

/* --- Generic item operations (Credit/Debit and others) --- */

export async function getItemsForUserAndType(userId: string, itemType: number): Promise<Item[]> {
  return itemClient.getByUserAndType(userId, itemType);
}

export async function getItemById(id: number): Promise<Item | null> {
  return itemClient.getById(id);
}

export async function createItem(payload: Item): Promise<Item> {
  // ensure required fields for backend
  if (!payload.fkItemType && payload.ItemType?.Id) payload.fkItemType = payload.ItemType.Id;
  if (!payload.userId) payload.userId = getSessionUserId();
  return itemClient.create(payload);
}

export async function updateItem(id: number, payload: Item): Promise<Item> {
  return itemClient.update(id, payload);
}

export async function deleteItem(id: number): Promise<void> {
  return itemClient.remove(id);
}

export async function fetchItemTypes() {
  return itemClient.fetchItemTypes();
}

export async function fetchTimePeriods() {
  return itemClient.fetchTimePeriods();
}
