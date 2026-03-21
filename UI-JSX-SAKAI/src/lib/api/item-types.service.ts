"use client";

import { ApiResponse } from "@/types/api-response";
import http from "./http";
import { ItemType } from "@/types/item-type";

// export async function getItemTypes(): Promise<ItemType[]> {
//   console.log("HTTP instance:", http);

//   const res = await http.get("/item-types");
//   return res.data;
// }

export async function getItemTypes(): Promise<ApiResponse<ItemType[]>> {
  const res = await http.get<ApiResponse<ItemType[]>>("/item-types");
  return res.data;
}


export async function getItemType(id: string): Promise<ApiResponse<ItemType>> {
  const res = await http.get<ApiResponse<ItemType>>(`/item-types/${id}`);
  return res.data;
}

export async function createItemType(payload: Partial<ItemType>): Promise<ApiResponse<ItemType>> {
  const res = await http.post<ApiResponse<ItemType>>("/item-types", payload);
  return res.data;
}

export async function updateItemType(id: string, payload: Partial<ItemType>): Promise<ApiResponse<ItemType>> {
  const res = await http.put<ApiResponse<ItemType>>(`/item-types/${id}`, payload);
  return res.data;
}

export async function deleteItemType(id: string): Promise<ApiResponse<null>> {
  const res = await http.delete<ApiResponse<null>>(`/item-types/${id}`);
  return res.data;
}
