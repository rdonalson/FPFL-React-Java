// src/features/catalog-command/transactions/components/items/occurrence/annual/EditAnnualPage.tsx
import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Card } from 'primereact/card';
import { ProgressSpinner } from 'primereact/progressspinner';
import { Toast } from 'primereact/toast';

import AnnualForm from './AnnualForm';
import { useItem } from '../../../../hooks/useItem';
import { getSessionUserId } from '@/app/state/sessionHelpers';
import type { Item } from '../../../../types/Item';

export default function EditAnnualPage({ itemType }: { itemType: number }) {
  const { id } = useParams();
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
        // Try to find item in existing state
        const found = items?.find(it => String(it.id) === String(id));
        if (found) {
          if (mounted) setItem(found);
          return;
        }

        // Otherwise load from backend
        const userId = getSessionUserId();
        if (!userId) throw new Error('No user session');

        await loadForUserAndType(userId, itemType);

        const refreshed = (items ?? []).find(it => String(it.id) === String(id));
        if (mounted) setItem(refreshed ?? null);
      } catch (err: any) {
        toastRef.current?.show({
          severity: 'error',
          summary: 'Load failed',
          detail: err?.message,
        });
      } finally {
        if (mounted) setLoading(false);
      }
    }

    load();
    return () => {
      mounted = false;
    };
  }, [id, items, itemType, loadForUserAndType]);

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
        <Card>
          <h3>Item not found</h3>
        </Card>
      </div>
    );
  }

  return (
    <div className="p-4">
      <Toast ref={toastRef} />

      <Card>
        <h2 className="text-lg font-semibold">
          {itemType === 1 ? 'Edit Annual Credit' : 'Edit Annual Debit'}
        </h2>
      </Card>

      <div className="mt-3">
        <AnnualForm
          itemType={itemType}
          initial={item}
          create={async () => {
            throw new Error('create not supported');
          }}
          update={update}
          onSaved={handleSaved}
        />
      </div>
    </div>
  );
}
