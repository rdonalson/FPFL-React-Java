// src/features/catalog-command/admin/itemType/api/itemTypeApi.ts
import { ItemTypeClient } from '@/api/generated/ItemTypeClient';
import { unwrap } from '@/api/utils/responseHelpers';
import type { ItemType } from '../types/ItemType';

export const itemTypeApi = {
  async fetchAll(): Promise<ItemType[]> {
    const res = await ItemTypeClient.getAll();
    return unwrap<ItemType[]>(res, []) as ItemType[];
  },

  async fetchById(id: number): Promise<ItemType | null> {
    const res = await ItemTypeClient.getById(id);
    return unwrap<ItemType>(res, null);
  },

  async create(item: ItemType): Promise<ItemType> {
    const res = await ItemTypeClient.create({ id: item.id, name: item.name });
    return unwrap<ItemType>(res, null, true) as ItemType;
  },

  async update(item: ItemType): Promise<ItemType> {
    const res = await ItemTypeClient.update(item.id, { id: item.id, name: item.name });
    return unwrap<ItemType>(res, null, true) as ItemType;
  },

  // <-- Updated remove: do not force unwrap with throwOnMissing
  async remove(id: number): Promise<void> {
    const res = await ItemTypeClient.delete(id);
    // res.data may be undefined for 204 No Content — treat that as success.
    // Optionally assert that we got a response object:
    if (!res) {
      throw new Error('No response from server');
    }
    return;
  },
};
