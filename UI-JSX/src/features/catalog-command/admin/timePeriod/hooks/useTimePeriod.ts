// src/features/itemType/hooks/useItemType.ts
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { timePeriodApi } from '../api/timePeriodApi';
import type { TimePeriod } from '../types/TimePeriod';

const TIME_PERIOD_KEY = ['time-periods'];

export function useTimePeriods() {
  return useQuery({
    queryKey: TIME_PERIOD_KEY,
    queryFn: timePeriodApi.fetchAll,
  });
}

export function useTimePeriod(id: number) {
  return useQuery({
    queryKey: [...TIME_PERIOD_KEY, id],
    queryFn: () => timePeriodApi.fetchById(id),
    enabled: !!id,
  });
}

export function useCreateTimePeriod() {
  const qc = useQueryClient();

  return useMutation({
    mutationFn: (item: TimePeriod) => timePeriodApi.create(item),
    onSuccess: () => qc.invalidateQueries({ queryKey: TIME_PERIOD_KEY }),
  });
}

export function useUpdateTimePeriod() {
  const qc = useQueryClient();

  return useMutation({
    mutationFn: (item: TimePeriod) => timePeriodApi.update(item),
    onSuccess: () => qc.invalidateQueries({ queryKey: TIME_PERIOD_KEY }),
  });
}

export function useDeleteTimePeriod() {
  const qc = useQueryClient();

  return useMutation({
    mutationFn: (id: number) => timePeriodApi.remove(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: TIME_PERIOD_KEY }),
  });
}
