import { apiClient } from '@/api/client';
import type { TimePeriod } from '@/features/catalog-command/admin/timePeriod/types/TimePeriod';
import type { ApiResponse } from '@/api/models/ApiResponse';

const BASE = '/time-periods';

export const TimePeriodClient = {
  async getAll(): Promise<ApiResponse<TimePeriod[]>> {
    return apiClient.get<ApiResponse<TimePeriod[]>>(BASE).then();
  },

  async getById(id: number): Promise<ApiResponse<TimePeriod>> {
    return apiClient.get<ApiResponse<TimePeriod>>(`${BASE}/${id}`).then();
  },

  async create(payload: { id: number; name: string }): Promise<ApiResponse<TimePeriod>> {
    return apiClient.post<ApiResponse<TimePeriod>>(BASE, payload).then();
  },

  async update(
    id: number,
    payload: { id: number; name: string },
  ): Promise<ApiResponse<TimePeriod>> {
    return apiClient.put<ApiResponse<TimePeriod>>(`${BASE}/${id}`, payload).then();
  },

  async delete(id: number): Promise<ApiResponse<void>> {
    return apiClient.delete<ApiResponse<void>>(`${BASE}/${id}`).then();
  },
};
