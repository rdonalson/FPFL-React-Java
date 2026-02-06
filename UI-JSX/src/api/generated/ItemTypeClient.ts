// src/api/generated/ItemTypeClient.ts
import { apiClient } from '../client';
import type { ItemType } from '@/features/itemType/types/ItemType';

const BASE = '/item-types';

/**
 * Low-level typed client for the ItemType API.
 * Mirrors your Java controller exactly.
 */
export const ItemTypeClient = {
  async getAll(): Promise<ItemType[]> {
    const res = await apiClient.get(BASE);
    return res.data;
  },

  async getById(id: number): Promise<ItemType> {
    const res = await apiClient.get(`${BASE}/${id}`);
    return res.data;
  },

  async create(payload: ItemType): Promise<ItemType> {
    const res = await apiClient.post(BASE, payload);
    return res.data;
  },

  async update(id: number, payload: Partial<ItemType>): Promise<ItemType> {
    const res = await apiClient.put(`${BASE}/${id}`, payload);
    return res.data;
  },

  async remove(id: number): Promise<void> {
    await apiClient.delete(`${BASE}/${id}`);
  },
};
