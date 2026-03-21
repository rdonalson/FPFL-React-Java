"use client";

import { useQuery } from "@tanstack/react-query";
import { getItemType } from "@/lib/api/item-types.service";
import { ItemType } from "@/types/item-type";
import { ApiResponse } from "@/types/api-response";

export function useItemType(id: string) {
  return useQuery<ApiResponse<ItemType>>({
    queryKey: ["item-type", id],
    queryFn: () => getItemType(id),
    enabled: !!id,
  });


}
