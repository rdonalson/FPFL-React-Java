"use client";

import { useItemTypes } from "../hooks/useItemTypes";
import ItemTypeTable from "@/components/domain/inventory/item-types/ItemTypeTable";
import Link from "next/link";

export default function ItemTypeList() {
  const { data, isLoading } = useItemTypes();

  return (
    <div>
      <h1>Item Types</h1>
      <Link href="/inventory/item-types/new">Create Item Type</Link>
      <ItemTypeTable response={data ?? null} loading={isLoading} />
    </div>
  );
}
