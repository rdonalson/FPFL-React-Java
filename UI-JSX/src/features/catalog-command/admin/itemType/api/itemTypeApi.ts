// src/features/itemType/api/itemTypeApi.ts
import { ItemTypeClient } from '@/api/generated/ItemTypeClient';
import type { ApiResponse } from '@/api/models/ApiResponse';
import type { ItemType } from '../types/ItemType';

export const itemTypeApi = {
  async fetchAll(): Promise<ApiResponse<ItemType[]>> {
    return ItemTypeClient.getAll();
  },

  async fetchById(id: number): Promise<ApiResponse<ItemType>> {
    return ItemTypeClient.getById(id);
  },

  async create(item: ItemType): Promise<ApiResponse<ItemType>> {
    return ItemTypeClient.create({
      id: item.id,
      name: item.name,
    });
  },

  async update(item: ItemType): Promise<ApiResponse<ItemType>> {
    return ItemTypeClient.update(item.id, {
      id: item.id,
      name: item.name,
    });
  },

  async remove(id: number): Promise<ApiResponse<void>> {
    return ItemTypeClient.delete(id);
  },
};
