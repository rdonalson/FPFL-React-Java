"use client";

import ItemTypeDetail from "@/features/inventory/item-types/detail/ItemTypeDetail";
import Link from "next/link";


type Props = {
  params: Promise<{ id: string }>;
};

export default async function ItemTypeDetailPage({ params }: Props) {
  const { id } = await params;

  return (
    <div className="card">
      <h2>Item Type Detail</h2>
      <ItemTypeDetail id={id} />
      <Link href="/inventory/item-types" className="text-sm text-gray-500">← Back to list</Link>
    </div>
  );
}

// export default function Page({ params }: { params: { id: string } }) {
//   return <ItemTypeDetail id={params.id} />;
// }

//import Link from "next/link";

// type Props = {
//   params: Promise<{ id: string }>;
// };

// export default async function ItemTypeDetailPage({ params }: Props) {
//   const { id } = await params;

//   return (
//     <div className="card">
//       <h2>Item Type Detail</h2>
//       <p>Viewing details for Item Type ID: <strong>{id}</strong></p>
//       <Link href="/inventory/item-types" className="text-sm text-gray-500">← Back to list</Link>
//     </div>
//   );
// }