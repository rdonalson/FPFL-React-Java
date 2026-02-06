// src/features/itemType/api/itemTypeApi.ts
import { apiClient } from '@/api/client';
import type { ItemType } from '../types/ItemType';

const BASE = '/item-types';

export const itemTypeApi = {
  async getAll(): Promise<ItemType[]> {
    const res = await apiClient.get(BASE);
    return res.data;
  },

  async getById(id: number): Promise<ItemType> {
    const res = await apiClient.get(`${BASE}/${id}`);
    return res.data;
  },

  async create(payload: { id: number; name: string }): Promise<ItemType> {
    const res = await apiClient.post(BASE, payload);
    return res.data;
  },

  async update(id: number, payload: { name: string }): Promise<ItemType> {
    const res = await apiClient.put(`${BASE}/${id}`, payload);
    return res.data;
  },

  async remove(id: number): Promise<void> {
    await apiClient.delete(`${BASE}/${id}`);
  },
};
