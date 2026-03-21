"use client";

import http from "./http";
import { ItemType } from "@/types/item-type";

export async function getItemTypes(): Promise<ItemType[]> {
  const res = await http.get("/item-types");
  return res.data;
}

export async function getItemType(id: string): Promise<ItemType> {
  const res = await http.get(`/item-types/${id}`);
  return res.data;
}

export async function createItemType(payload: Partial<ItemType>) {
  const res = await http.post("/item-types", payload);
  return res.data;
}

export async function updateItemType(id: string, payload: Partial<ItemType>) {
  const res = await http.put(`/item-types/${id}`, payload);
  return res.data;
}

export async function deleteItemType(id: string) {
  const res = await http.delete(`/item-types/${id}`);
  return res.data;
}
