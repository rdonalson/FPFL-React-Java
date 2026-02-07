import { apiClient } from '@/api/client';
import type { ItemType } from '@/features/itemType/types/ItemType';
import type { ApiResponse } from '@/api/models/ApiResponse';

const BASE = '/item-types';

export const ItemTypeClient = {
  async getAll(): Promise<ApiResponse<ItemType[]>> {
    return apiClient.get<ApiResponse<ItemType[]>>(`${BASE}`).then();
  },

  async getById(id: number): Promise<ApiResponse<ItemType>> {
    return apiClient.get<ApiResponse<ItemType>>(`${BASE}/${id}`).then();
  },

  async create(payload: { name: string }): Promise<ApiResponse<ItemType>> {
    return apiClient.post<ApiResponse<ItemType>>(`${BASE}`, payload).then();
  },

  async update(id: number, payload: { name: string }): Promise<ApiResponse<ItemType>> {
    return apiClient.put<ApiResponse<ItemType>>(`${BASE}/${id}`, payload).then();
  },

  async delete(id: number): Promise<ApiResponse<void>> {
    return apiClient.delete<ApiResponse<void>>(`${BASE}/${id}`).then();
  },
};
