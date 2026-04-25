// src/api/generated/ItemTypeClient.ts
import { apiClient } from '@/api/client';
import type { ItemType } from '@/features/catalog-command/admin/itemType/types/ItemType';
import type { ApiResponse } from '@/api/models/ApiResponse';
import { callAndParse } from '@/api/utils/apiParse';

const BASE = '/item-types';

export const ItemTypeClient = {
  async getAll(): Promise<ApiResponse<ItemType[]>> {
    return callAndParse<ItemType[]>(() => apiClient.get<ApiResponse<ItemType[]>>(BASE));
  },

  async getById(id: number): Promise<ApiResponse<ItemType>> {
    return callAndParse<ItemType>(() => apiClient.get<ApiResponse<ItemType>>(`${BASE}/${id}`));
  },

  async create(payload: { id: number; name: string }): Promise<ApiResponse<ItemType>> {
    return callAndParse<ItemType>(() => apiClient.post<ApiResponse<ItemType>>(BASE, payload));
  },

  async update(id: number, payload: { id: number; name: string }): Promise<ApiResponse<ItemType>> {
    return callAndParse<ItemType>(() =>
      apiClient.put<ApiResponse<ItemType>>(`${BASE}/${id}`, payload),
    );
  },

  async delete(id: number): Promise<ApiResponse<void>> {
    return callAndParse<void>(() => apiClient.delete<ApiResponse<void>>(`${BASE}/${id}`));
  },
};
