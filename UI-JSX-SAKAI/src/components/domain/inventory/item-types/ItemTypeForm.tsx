import { ItemType } from "@/types/item-type";

export default function ItemTypeForm({
  itemType,
  loading,
}: {
  itemType?: ItemType;
  loading: boolean;
}) {
  if (loading) return <p>Loading...</p>;

  return (
    <form>
      <label>Name</label>
      <input defaultValue={itemType?.name ?? ""} />
    </form>
  );
}
