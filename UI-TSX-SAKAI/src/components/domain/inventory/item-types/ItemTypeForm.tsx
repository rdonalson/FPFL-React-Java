"use client";

import { useState } from "react";
import { ItemType } from "@/types/item-type";
import { useItemTypeMutations } from "@/features/inventory/item-types/hooks/useItemTypeMutations";

export default function ItemTypeForm({
  itemType,
  loading,
}: {
  itemType?: ItemType | null;
  loading: boolean;
}) {
  const [name, setName] = useState(itemType?.name ?? "");
  const { create, update, remove } = useItemTypeMutations();

  if (loading) return <p>Loading...</p>;

  const isEdit = !!itemType;

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (isEdit) {
      update.mutate({ id: String(itemType!.id), payload: { name } });
    } else {
      create.mutate({ name });
    }
  };

  const handleDelete = () => {
    if (!itemType) return;
    remove.mutate(String(itemType.id));
  };

  return (
    <form onSubmit={handleSubmit}>
      <label>
        Name
        <input
          value={name}
          onChange={(e) => setName(e.target.value)}
          required
        />
      </label>

      <button type="submit" disabled={create.isPending || update.isPending}>
        {isEdit ? "Update" : "Create"}
      </button>

      {isEdit && (
        <button
          type="button"
          onClick={handleDelete}
          disabled={remove.isPending}
        >
          Delete
        </button>
      )}
    </form>
  );
}