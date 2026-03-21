"use client";

import Link from "next/link";

type Props = {
  params: Promise<{ id: string }>;
};

export default async function ItemTypeDetailPage({ params }: Props) {
  const { id } = await params;

  return (
    <div className="card">
      <h2>Item Type Detail</h2>
      <p>Viewing details for Item Type ID: <strong>{id}</strong></p>
      <Link href="/inventory/item-types" className="text-sm text-gray-500">← Back to list</Link>
    </div>
  );
}