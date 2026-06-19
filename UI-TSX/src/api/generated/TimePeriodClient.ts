// src/api/generated/TimePeriodClient.ts
import { apiClient } from '@/api/client';
import type { ApiResponse } from '@/api/models/ApiResponse';
import type { TimePeriodDto } from '@/features/catalog-command/transactions/types/Item';
import { callAndParse } from '@/api/utils/apiParse';

const BASE = '/time-periods';

export const TimePeriodClient = {
  async getAll(): Promise<ApiResponse<TimePeriodDto[]>> {
    return callAndParse<TimePeriodDto[]>(() => apiClient.get<ApiResponse<TimePeriodDto[]>>(BASE));
  },

  async getById(id: number): Promise<ApiResponse<TimePeriodDto>> {
    return callAndParse<TimePeriodDto>(() =>
      apiClient.get<ApiResponse<TimePeriodDto>>(`${BASE}/${id}`),
    );
  },

  async create(payload: Partial<TimePeriodDto>): Promise<ApiResponse<TimePeriodDto>> {
    return callAndParse<TimePeriodDto>(() =>
      apiClient.post<ApiResponse<TimePeriodDto>>(BASE, payload),
    );
  },

  async update(id: number, payload: Partial<TimePeriodDto>): Promise<ApiResponse<TimePeriodDto>> {
    return callAndParse<TimePeriodDto>(() =>
      apiClient.put<ApiResponse<TimePeriodDto>>(`${BASE}/${id}`, payload),
    );
  },

  async delete(id: number): Promise<ApiResponse<void>> {
    return callAndParse<void>(() => apiClient.delete<ApiResponse<void>>(`${BASE}/${id}`));
  },
};
