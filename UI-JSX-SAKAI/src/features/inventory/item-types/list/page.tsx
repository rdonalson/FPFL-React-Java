import Link from 'next/link';

async function getItemTypes() {
  // In a real app, replace with: fetch('https://api.example.com/item-types')
  return [
    { id: '1', name: 'Electronics' },
    { id: '2', name: 'Furniture' },
  ];
}

export default async function ItemTypesListPage() {
  const itemTypes = await getItemTypes();

  return (
     <div className="card">
      <h2>Item Types</h2>
      <ul>
        {itemTypes.map((type) => (
          <li key={type.id}>
            <Link href={`/inventory/item-types/${type.id}`} className="text-blue-500 underline">
              {type.name}
            </Link>
          </li>
        ))}
      </ul>
    </div>
  );
}