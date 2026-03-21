"use client";

import { useItemType } from "../hooks/useItemType";
import ItemTypeForm from "@/components/domain/inventory/item-types/ItemTypeForm";

export default function ItemTypeDetail({ id }: { id: string }) {
  const { data, isLoading } = useItemType(id);

  return (
    <div>
      <h1>{data ? `Edit Item Type #${data.data.id}` : "Item Type"}</h1>
      <ItemTypeForm itemType={data?.data ?? null} loading={isLoading} />
    </div>
  );
}
