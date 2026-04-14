// src/features/timePeriod/api/timePeriodApi.ts
import { TimePeriodClient } from '@/api/generated/TimePeriodClient';
import type { ApiResponse } from '@/api/models/ApiResponse';
import type { TimePeriod } from '../types/TimePeriod';

export const timePeriodApi = {
  async fetchAll(): Promise<ApiResponse<TimePeriod[]>> {
    return TimePeriodClient.getAll();
  },

  async fetchById(id: number): Promise<ApiResponse<TimePeriod>> {
    return TimePeriodClient.getById(id);
  },

  async create(item: TimePeriod): Promise<ApiResponse<TimePeriod>> {
    return TimePeriodClient.create({
      id: item.id,
      name: item.name,
    });
  },

  async update(item: TimePeriod): Promise<ApiResponse<TimePeriod>> {
    return TimePeriodClient.update(item.id, {
      id: item.id,
      name: item.name,
    });
  },

  async remove(id: number): Promise<ApiResponse<void>> {
    return TimePeriodClient.delete(id);
  },
};
