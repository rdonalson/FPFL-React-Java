"use client";

import { useQuery } from "@tanstack/react-query";
import { getItemType } from "@/lib/api/item-types.service";

export function useItemType(id: string) {
  return useQuery({
    queryKey: ["item-type", id],
    queryFn: () => getItemType(id),
    enabled: !!id,
  });
}
