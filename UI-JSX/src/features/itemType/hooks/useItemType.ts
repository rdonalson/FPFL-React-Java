// src/features/itemType/hooks/useItemType.ts
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { itemTypeApi } from '../api/itemTypeApi';
//import type { ItemType } from '../types/ItemType';

const ITEM_TYPE_KEY = ['item-types'];

export function useItemTypes() {
  return useQuery({
    queryKey: ITEM_TYPE_KEY,
    queryFn: itemTypeApi.getAll,
  });
}

export function useItemType(id: number) {
  return useQuery({
    queryKey: [...ITEM_TYPE_KEY, id],
    queryFn: () => itemTypeApi.getById(id),
    enabled: !!id,
  });
}

export function useCreateItemType() {
  const qc = useQueryClient();

  return useMutation({
    mutationFn: itemTypeApi.create,
    onSuccess: () => qc.invalidateQueries({ queryKey: ITEM_TYPE_KEY }),
  });
}

export function useUpdateItemType(id: number) {
  const qc = useQueryClient();

  return useMutation({
    mutationFn: (payload: { name: string }) => itemTypeApi.update(id, payload),
    onSuccess: () => qc.invalidateQueries({ queryKey: ITEM_TYPE_KEY }),
  });
}

export function useDeleteItemType() {
  const qc = useQueryClient();

  return useMutation({
    mutationFn: itemTypeApi.remove,
    onSuccess: () => qc.invalidateQueries({ queryKey: ITEM_TYPE_KEY }),
  });
}
