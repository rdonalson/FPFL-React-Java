"use client";

import { ItemType } from "@/types/item-type";
import Link from "next/link";

export default function ItemTypeTable({
  itemTypes,
  loading,
}: {
  itemTypes: ItemType[];
  loading: boolean;
}) {
  if (loading) return <p>Loading item types...</p>;

  if (!itemTypes.length) return <p>No item types found.</p>;

  return (
    <table>
      <thead>
        <tr>
          <th>ID</th>
          <th>Name</th>
          <th />
        </tr>
      </thead>
      <tbody>
        {itemTypes.map((it) => (
          <tr key={it.id}>
            <td>{it.id}</td>
            <td>{it.name}</td>
            <td>
              <Link href={`/inventory/item-types/${it.id}`}>Edit</Link>
            </td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}
