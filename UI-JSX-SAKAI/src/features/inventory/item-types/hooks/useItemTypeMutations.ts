"use client";

import { useMutation, useQueryClient } from "@tanstack/react-query";
import {
  createItemType,
  updateItemType,
  deleteItemType,
} from "@/lib/api/item-types.service";
import { ItemType } from "@/types/item-type";

export function useItemTypeMutations() {
  const queryClient = useQueryClient();

  const create = useMutation({
    mutationFn: (payload: Partial<ItemType>) => createItemType(payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["item-types"] });
    },
  });

  const update = useMutation({
    mutationFn: ({ id, payload }: { id: string; payload: Partial<ItemType> }) =>
      updateItemType(id, payload),
    onSuccess: (_, { id }) => {
      queryClient.invalidateQueries({ queryKey: ["item-types"] });
      queryClient.invalidateQueries({ queryKey: ["item-type", id] });
    },
  });

  const remove = useMutation({
    mutationFn: (id: string) => deleteItemType(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["item-types"] });
    },
  });

  return { create, update, remove };
}