import { ItemType } from "@/types/item-type";

export default function ItemTypeTable({
  itemTypes,
  loading,
}: {
  itemTypes: ItemType[];
  loading: boolean;
}) {
  if (loading) return <p>Loading...</p>;

  return (
    <table>
      <tbody>
        {itemTypes.map((it) => (
          <tr key={it.id}>
            <td>{it.id}</td>
            <td>{it.name}</td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}
