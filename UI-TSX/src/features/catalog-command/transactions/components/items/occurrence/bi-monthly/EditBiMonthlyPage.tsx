// src/features/catalog-command/transactions/components/items/occurrence/bi-monthly/EditBiMonthlyPage.tsx
import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Card } from 'primereact/card';
import { ProgressSpinner } from 'primereact/progressspinner';
import { Toast } from 'primereact/toast';

import BiMonthlyForm from './BiMonthlyForm';
import { useItem } from '../../../../hooks/useItem';
import { getSessionUserId } from '@/app/state/sessionHelpers';
import type { Item } from '../../../../types/Item';

interface EditBiMonthlyPageProps {
  itemType: number;
}

export default function EditBiMonthlyPage({ itemType }: EditBiMonthlyPageProps) {
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
    // Intentionally depend on id and itemType and the loader function only to avoid repeated calls
  }, [id, itemType, loadForUserAndType]);

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
          {itemType === 1 ? 'Edit Bi-Monthly Credit' : 'Edit Bi-Monthly Debit'}
        </h2>
      </Card>

      <Card className="w-full">
        <BiMonthlyForm
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
