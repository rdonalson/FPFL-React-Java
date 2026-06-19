// src/features/catalog-command/transactions/api/itemApi.ts
import { ItemClient } from '@/api/generated/ItemClient';
import { Item } from '../types/Item';
import { unwrap } from '@/api/utils/responseHelpers';
import { getSessionUserId } from '@/api/utils/userId';

/* --- Generic item operations (Credit/Debit and others) --- */

export async function getItemsForUserAndType(userId: string, itemType: number): Promise<Item[]> {
  const res = await ItemClient.fetchForCurrentUser(itemType);
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
