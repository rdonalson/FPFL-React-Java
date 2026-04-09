// src/features/itemType/hooks/useItemType.ts
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { itemTypeApi } from '../api/itemTypeApi';
import type { ItemType } from '../types/ItemType';

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
    mutationFn: (item: ItemType) => itemTypeApi.create(item),
    onSuccess: () => qc.invalidateQueries({ queryKey: ITEM_TYPE_KEY }),
  });
}

export function useUpdateItemType() {
  const qc = useQueryClient();

  return useMutation({
    mutationFn: (item: ItemType) => itemTypeApi.update(item),
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
