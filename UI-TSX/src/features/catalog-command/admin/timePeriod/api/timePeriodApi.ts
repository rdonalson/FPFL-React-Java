// src/features/catalog-command/transactions/api/timePeriodApi.ts
import { TimePeriodClient } from '@/api/generated/TimePeriodClient';
import { unwrap } from '@/api/utils/responseHelpers';
import type { TimePeriodDto } from '@/features/catalog-command/transactions/types/Item';

export const timePeriodApi = {
  async fetchAll(): Promise<TimePeriodDto[]> {
    const res = await TimePeriodClient.getAll();
    return unwrap<TimePeriodDto[]>(res, []) as TimePeriodDto[];
  },

  async fetchById(id: number): Promise<TimePeriodDto | null> {
    const res = await TimePeriodClient.getById(id);
    return unwrap<TimePeriodDto>(res, null);
  },

  async create(tp: Partial<TimePeriodDto>): Promise<TimePeriodDto> {
    const res = await TimePeriodClient.create(tp);
    return unwrap<TimePeriodDto>(res, null, true) as TimePeriodDto;
  },

  // <-- CHANGED: accept the full item so callers can call update(item)
  async update(item: TimePeriodDto): Promise<TimePeriodDto> {
    const res = await TimePeriodClient.update(item.id, item);
    return unwrap<TimePeriodDto>(res, null, true) as TimePeriodDto;
  },

  // <-- Updated remove: do not force unwrap with throwOnMissing
  async remove(id: number): Promise<void> {
    const res = await TimePeriodClient.delete(id);
    // res.data may be undefined for 204 No Content — treat that as success.
    // Optionally assert that we got a response object:
    if (!res) {
      throw new Error('No response from server');
    }
    return;
  },

  //   async remove(id: number): Promise<void> {
  //     const res = await TimePeriodClient.delete(id);
  //     unwrap<void>(res, undefined, true);
  //   },
};
