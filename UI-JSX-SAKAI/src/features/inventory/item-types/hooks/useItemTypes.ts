"use client";

import { useQuery } from "@tanstack/react-query";
import { getItemTypes } from "@/lib/api/item-types.service";
import { ItemType } from "@/types/item-type";

export function useItemTypes() {
  const query = useQuery<ItemType[]>({
    queryKey: ["item-types"],
    queryFn: getItemTypes,
  });

  console.log("useItemTypes running on:", typeof window === "undefined" ? "server" : "client");
  console.log("React Query state:", {
    isFetching: query.isFetching,
    isPaused: query.isPaused,
    status: query.status,
  });

  return query;
}
