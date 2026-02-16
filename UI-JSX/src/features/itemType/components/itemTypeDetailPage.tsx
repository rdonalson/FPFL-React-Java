import { useParams } from 'react-router-dom';
import { InlineError } from '@/components/InLineError';
import { useItemType } from '../hooks/useItemType';
import { ApiErrorBoundary } from '@/shared/components/ApiErrorBoundary';

export function ItemTypeDetailPage() {
  const { id } = useParams();
  const itemTypeId = Number(id);

  const { data, isLoading, error } = useItemType(itemTypeId);

  if (isLoading) {
    return <div className="item-type-loading">Loading item type…</div>;
  }

  const itemType = data?.data;

  return (
    <ApiErrorBoundary error={error}>
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
    </ApiErrorBoundary>
  );
}
