// src/features/itemType/components/ItemTypeListPage.tsx
import { ListBox } from 'primereact/listbox';
import { useItemTypes } from '../hooks/useItemType';

export function ItemTypeListPage() {
  const { data, isLoading, error } = useItemTypes();

  if (isLoading) return <p>Loading Item Types…</p>;
  if (error) return <p>Error loading Item Types</p>;

  return (
    <div style={{ padding: '2rem', maxWidth: '400px' }}>
      <h2>Item Types</h2>

      <ListBox
        value={null}
        options={(data ?? []).map(it => ({
          label: it.name,
          value: it.id,
        }))}
        listStyle={{ maxHeight: '300px' }}
        style={{ width: '100%' }}
      />
    </div>
  );
}
