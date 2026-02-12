// src/features/itemType/hooks/useItemType.ts
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { itemTypeApi } from '../api/itemTypeApi';

const ITEM_TYPE_KEY = ['item-types'];

export function useItemTypes() {
  return useQuery({
    queryKey: ITEM_TYPE_KEY,
    queryFn: itemTypeApi.fetchAll,
  });
}

export function useItemType(id: number) {
  return useQuery({
    queryKey: [...ITEM_TYPE_KEY, id],
    queryFn: () => itemTypeApi.fetchById(id),
    enabled: !!id,
  });
}

export function useCreateItemType() {
  const qc = useQueryClient();

  return useMutation({
    mutationFn: (name: string) => itemTypeApi.create(name),
    onSuccess: () => qc.invalidateQueries({ queryKey: ITEM_TYPE_KEY }),
  });
}

export function useUpdateItemType(id: number) {
  const qc = useQueryClient();

  return useMutation({
    mutationFn: (name: string) => itemTypeApi.update(id, name),
    onSuccess: () => qc.invalidateQueries({ queryKey: ITEM_TYPE_KEY }),
  });
}

export function useDeleteItemType() {
  const qc = useQueryClient();

  return useMutation({
    mutationFn: (id: number) => itemTypeApi.remove(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: ITEM_TYPE_KEY }),
  });
}
