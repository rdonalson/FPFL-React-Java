import { ListBox } from 'primereact/listbox';
import { useItemTypes } from '../hooks/useItemType';

export function ItemTypeListPage() {
  const { data, isLoading } = useItemTypes();

  if (isLoading) return <p>Loading Item Types…</p>;

  const options = (data?.data ?? []).map(it => ({
    label: it.name,
    value: it.id.toString(),
  }));

  return (
    <div className="item-type-list-container">
      <h2>Item Types</h2>

      <ListBox
        value={null}
        options={options}
        listStyle={{ maxHeight: '300px' }}
        className="item-type-listbox"
      />
    </div>
  );
}
