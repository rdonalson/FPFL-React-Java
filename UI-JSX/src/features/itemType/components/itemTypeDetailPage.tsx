import { useEffect } from "react";
import { useParams } from "react-router-dom";
import { InlineError } from "@/components/InLineError";
import { useItemType } from "../hooks/useItemType";
import { useToast } from "@/shared/hooks/useToast";

export function ItemTypeDetailPage() {
  const { id } = useParams();
  const itemTypeId = Number(id);

  const toast = useToast();
  const { data, isLoading, error } = useItemType(itemTypeId);

  // 🔥 Show toast for server errors (500+ or network)
  useEffect(() => {
    if (error && (error as any).status >= 500) {
      toast.current?.show({
        severity: "error",
        summary: "Server Error",
        detail: error.message ?? "An unexpected server error occurred.",
        life: 4000,
      });
    }
  }, [error, toast]);

  if (isLoading) {
    return <div className="item-type-loading">Loading item type…</div>;
  }

  // ❗ Inline error for user-correctable issues (400, 404, 422)
  if (error && (error as any).status < 500) {
    return (
      <div className="item-type-error">
        <InlineError status={(error as any).status} message={error.message} />
      </div>
    );
  }

  const itemType = data?.data;

  return (
    <div className="item-type-detail">
      <h2>Item Type Details</h2>

      {!itemType && <InlineError message="Item type not found." />}

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