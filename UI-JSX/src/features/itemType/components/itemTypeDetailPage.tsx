import { useParams } from "react-router-dom";
import { InlineError } from "@/components/InLineError";
import { useItemType } from "../hooks/useItemType";

export function ItemTypeDetailPage() {
  const { id } = useParams();
  const itemTypeId = Number(id);

  const { data, isLoading, error } = useItemType(itemTypeId);

  if (isLoading) {
    return <div style={{ padding: "2rem" }}>Loading item type…</div>;
  }

  // Inline error for user-correctable issues (404, 400, 422, etc.)
  if (error && (error as any).status < 500) {
    return (
      <div style={{ padding: "2rem" }}>
        <InlineError status={(error as any).status} message={error.message} />
      </div>
    );
  }

  const itemType = data?.data;

  return (
    <div style={{ padding: "2rem" }}>
      <h2>Item Type Details</h2>

      {!itemType && (
        <InlineError message="Item type not found." />
      )}

      {itemType && (
        <ul>
          <li>
            <strong>ID:</strong> {itemType.id}
          </li>
          <li>
            <strong>Name:</strong> {itemType.name}
          </li>
        </ul>
      )}
    </div>
  );
}