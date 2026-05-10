// src/api/generated/ItemClient.ts
import { apiClient } from '@/api/client';
import type { ApiResponse } from '@/api/models/ApiResponse';
import type {
  Item,
  ItemTypeDto,
  TimePeriodDto,
} from '@/features/catalog-command/transactions/types/Item';
import { callAndParse } from '@/api/utils/apiParse';
import { getSessionUserId } from '@/api/utils/userId';

const BASE_ITEMS = '/items';
const BASE_ITEM_TYPES = '/item-types';
const BASE_TIME_PERIODS = '/time-periods';

export const ItemClient = {
  async fetchForCurrentUser(itemType: number): Promise<ApiResponse<Item[]>> {
    const userID = getSessionUserId();
    return callAndParse<Item[]>(() =>
      apiClient.get<ApiResponse<Item[]>>(`${BASE_ITEMS}/${encodeURIComponent(userID)}/${itemType}`),
    );
  },

  async getById(id: number): Promise<ApiResponse<Item>> {
    return callAndParse<Item>(() => apiClient.get<ApiResponse<Item>>(`${BASE_ITEMS}/${id}`));
  },

  async create(payload: Item): Promise<ApiResponse<Item>> {
    return callAndParse<Item>(() => apiClient.post<ApiResponse<Item>>(BASE_ITEMS, payload));
  },

  async update(id: number, payload: Item): Promise<ApiResponse<Item>> {
    return callAndParse<Item>(() =>
      apiClient.put<ApiResponse<Item>>(`${BASE_ITEMS}/${id}`, payload),
    );
  },

  async remove(id: number): Promise<ApiResponse<void>> {
    return callAndParse<void>(() => apiClient.delete<ApiResponse<void>>(`${BASE_ITEMS}/${id}`));
  },

  async fetchItemTypes(): Promise<ApiResponse<ItemTypeDto[]>> {
    return callAndParse<ItemTypeDto[]>(() =>
      apiClient.get<ApiResponse<ItemTypeDto[]>>(BASE_ITEM_TYPES),
    );
  },

  async fetchTimePeriods(): Promise<ApiResponse<TimePeriodDto[]>> {
    return callAndParse<TimePeriodDto[]>(() =>
      apiClient.get<ApiResponse<TimePeriodDto[]>>(BASE_TIME_PERIODS),
    );
  },
};
