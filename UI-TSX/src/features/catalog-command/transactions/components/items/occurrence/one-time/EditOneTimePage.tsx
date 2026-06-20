// src/features/catalog-command/transactions/components/items/occurrence/one-time/EditOneTimePage.tsx
import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Card } from 'primereact/card';
import { ProgressSpinner } from 'primereact/progressspinner';
import { Toast } from 'primereact/toast';

import OneTimeForm from './OneTimeForm';
import { useItem } from '../../../../hooks/useItem';
import type { Item } from '../../../../types/Item';
import { getSessionUserId } from '@/app/state/sessionHelpers';

interface EditOneTimePageProps {
  itemType: number;
}

export default function EditOneTimePage({ itemType }: EditOneTimePageProps) {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const toastRef = React.useRef<Toast | null>(null);

  const { items, loadForUserAndType, update } = useItem();
  const [item, setItem] = useState<Item | null>(null);
  const [localLoading, setLocalLoading] = useState<boolean>(true);

  useEffect(() => {
    let mounted = true;

    async function load() {
      setLocalLoading(true);
      try {
        if (!id) throw new Error('Missing id');

        const found = items?.find(it => String(it.id) === String(id));
        if (found) {
          if (mounted) setItem(found);
          return;
        }

        const userId = getSessionUserId();
        if (!userId) throw new Error('No user session');
        await loadForUserAndType(userId, itemType);

        const refreshed = (items ?? []).find(it => String(it.id) === String(id));
        if (mounted) setItem(refreshed ?? null);
      } catch (err: any) {
        console.error('Failed to load item', err);
        toastRef.current?.show({
          severity: 'error',
          summary: 'Load failed',
          detail: err?.message ?? 'Could not load item',
        });
      } finally {
        if (mounted) setLocalLoading(false);
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

  if (localLoading) {
    return (
      <div className="p-4 flex items-center justify-center">
        <ProgressSpinner />
      </div>
    );
  }

  if (!item) {
    return (
      <div className="p-4">
        <Card>
          <h3 className="text-red-600">Item not found</h3>
        </Card>
      </div>
    );
  }

  return (
    <div className="p-4">
      <Toast ref={toastRef} />

      {/* Title Card */}
      <Card className="w-full mb-3 px-4 sm:px-6 py-3">
        <h2 className="text-lg font-semibold">
          {itemType === 1 ? 'Edit One Time Credit' : 'Edit One Time Debit'}
        </h2>
      </Card>

      {/* Form Card */}
      <Card className="w-full">
        <OneTimeForm
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
