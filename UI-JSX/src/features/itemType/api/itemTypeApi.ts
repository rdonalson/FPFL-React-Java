import { ItemTypeClient } from "@/api/generated/ItemTypeClient";
import type { ApiResponse } from "@/api/models/ApiResponse";
import type { ItemType } from "../types/ItemType";

export const itemTypeApi = {
  async fetchAll(): Promise<ApiResponse<ItemType[]>> {
    return ItemTypeClient.getAll();
  },

  async fetchById(id: number): Promise<ApiResponse<ItemType>> {
    return ItemTypeClient.getById(id);
  },

  async create(name: string): Promise<ApiResponse<ItemType>> {
    return ItemTypeClient.create({ name });
  },

  async update(id: number, name: string): Promise<ApiResponse<ItemType>> {
    return ItemTypeClient.update(id, { name });
  },

  async remove(id: number): Promise<ApiResponse<void>> {
    return ItemTypeClient.delete(id);
  },
};
