import { ListBox } from "primereact/listbox";
import { useItemTypes } from "../hooks/useItemType";

export function ItemTypeListPage() {
  const { data, isLoading, error } = useItemTypes();

  console.log("[ItemTypeListPage] data:", data, "error:", error, "loading:", isLoading);

  if (isLoading) return <p>Loading Item Types…</p>;

  if (error) {
    console.error("[ItemTypeListPage] Error:", error);
    return <p style={{ color: "red" }}>Error loading Item Types: {error.message}</p>;
  }

  const options = (data ?? []).map((it) => ({
    label: it.name,
    value: it.id,
  }));

  return (
    <div style={{ padding: "2rem", maxWidth: "400px" }}>
      <h2>Item Types</h2>

      <ListBox
        value={null}
        options={options}
        listStyle={{ maxHeight: "300px" }}
        style={{ width: "100%" }}
      />
    </div>
  );
}
