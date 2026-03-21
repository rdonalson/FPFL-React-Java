"use client";

import { ApiResponse } from "@/types/api-response";
import { ItemType } from "@/types/item-type";
import Link from "next/link";

export default function ItemTypeTable({
  response,
  loading,
}: {
  response: ApiResponse<ItemType[]> | null;
  loading: boolean;
}) {
  if (loading) return <p>Loading item types...</p>;

  if (!response || !response.data.length) return <p>No item types found.</p>;

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
        {response.data.map((it) => (
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
