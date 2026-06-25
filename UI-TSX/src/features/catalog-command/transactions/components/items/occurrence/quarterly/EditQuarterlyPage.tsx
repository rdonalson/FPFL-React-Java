// src/features/catalog-command/transactions/components/items/occurrence/quarterly/EditQuarterlyPage.tsx
import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Card } from 'primereact/card';
import { ProgressSpinner } from 'primereact/progressspinner';
import { Toast } from 'primereact/toast';

import QuarterlyForm from './QuarterlyForm';
import { useItem } from '../../../../hooks/useItem';
import { getSessionUserId } from '@/app/state/sessionHelpers';
import type { Item } from '../../../../types/Item';

interface EditQuarterlyPageProps {
  itemType: number;
}

export default function EditQuarterlyPage({ itemType }: EditQuarterlyPageProps) {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const toastRef = React.useRef<Toast | null>(null);

  const { items, loadForUserAndType, update } = useItem();
  const [item, setItem] = useState<Item | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let mounted = true;

    async function load() {
      setLoading(true);
      try {
        if (!id) throw new Error('Missing id');

        // Try to find item in already-loaded items
        const found = items?.find(it => String(it.id) === String(id));
        if (found) {
          if (mounted) setItem(found);
          return;
        }

        // Otherwise load items for this user + type
        const userId = getSessionUserId();
        if (!userId) throw new Error('No user session');

        await loadForUserAndType(userId, itemType);

        const refreshed = (items ?? []).find(it => String(it.id) === String(id));
        if (mounted) setItem(refreshed ?? null);
      } catch (err: any) {
        toastRef.current?.show({
          severity: 'error',
          summary: 'Load failed',
          detail: err?.message ?? 'Could not load item',
        });
      } finally {
        if (mounted) setLoading(false);
      }
    }

    load();
    return () => {
      mounted = false;
    };
  }, [id, itemType, loadForUserAndType]); // ✔ matches your standardized pattern

  function handleSaved() {
    const base = itemType === 1 ? '/command/transactions/credits' : '/command/transactions/debits';
    navigate(base);
  }

  if (loading) {
    return (
      <div className="p-4 flex justify-center">
        <ProgressSpinner />
      </div>
    );
  }

  if (!item) {
    return (
      <div className="p-4">
        <Card className="w-full">
          <h3 className="text-red-600">Item not found</h3>
        </Card>
      </div>
    );
  }

  return (
    <div className="p-0 md:p-4 w-full">
      <Toast ref={toastRef} />

      <Card className="w-full mb-3 px-4 sm:px-6">
        <h2 className="text-lg font-semibold">
          {itemType === 1 ? 'Edit Quarterly Credit' : 'Edit Quarterly Debit'}
        </h2>
      </Card>

      <Card className="w-full">
        <QuarterlyForm
          itemType={itemType}
          initial={item}
          create={async () => {
            throw new Error('create not supported here');
          }}
          update={update}
          onSaved={handleSaved}
        />
      </Card>
    </div>
  );
}
