// src/features/catalog-command/transactions/components/items/ItemsListPage.tsx
import React, { useEffect, useState, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { Button } from 'primereact/button';
import { DataTable } from 'primereact/datatable';
import { Column } from 'primereact/column';
import { ConfirmDialog, confirmDialog } from 'primereact/confirmdialog';
import { Toast } from 'primereact/toast';
import { Card } from 'primereact/card';

import { Item } from '../../../types/Item';
import { useItem } from '../../../hooks/useItem';
import { getSessionUserId } from '@/app/state/sessionHelpers';

interface ItemsListPageProps {
  /** 1 = Credit, 2 = Debit */
  itemType: number;
}

export default function ItemsListPage({ itemType }: ItemsListPageProps) {
  const navigate = useNavigate();
  const toastRef = useRef<any>(null);

  const { items, loading, error, loadForUserAndType, remove } = useItem();
  const [localLoading, setLocalLoading] = useState<boolean>(false);

  useEffect(() => {
    loadItems();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [itemType]);

  async function loadItems() {
    setLocalLoading(true);
    try {
      const userId = getSessionUserId();
      if (!userId) {
        toastRef.current?.show({
          severity: 'warn',
          summary: 'Not signed in',
          detail: 'No user session found.',
        });
        setLocalLoading(false);
        return;
      }
      await loadForUserAndType(userId, itemType);
    } catch (err) {
      console.error('Failed to load items', err);
      toastRef.current?.show({
        severity: 'error',
        summary: 'Load failed',
        detail: 'Could not load items.',
      });
    } finally {
      setLocalLoading(false);
    }
  }

  function handleAdd() {
    const base = itemType === 1 ? '/command/transactions/credits' : '/command/transactions/debits';
    navigate(`${base}/new`);
  }

  function handleEdit(item: Item) {
    const base = itemType === 1 ? '/command/transactions/credits' : '/command/transactions/debits';
    navigate(`${base}/${item.id}/edit`);
  }

  function confirmDelete(item: Item) {
    confirmDialog({
      message: `Delete "${item.name}"? This action cannot be undone.`,
      header: 'Confirm Delete',
      icon: 'pi pi-exclamation-triangle',
      accept: async () => {
        try {
          await remove(item.id!);
          toastRef.current?.show({
            severity: 'success',
            summary: 'Deleted',
            detail: `"${item.name}" removed.`,
          });
        } catch (err) {
          console.error('Delete failed', err);
          toastRef.current?.show({
            severity: 'error',
            summary: 'Delete failed',
            detail: 'Could not delete item.',
          });
        }
      },
    });
  }

  return (
    <div className="p-4 flex flex-col gap-4">
      <Toast ref={toastRef} />
      <ConfirmDialog />

      <Card>
        <div className="flex justify-between items-center">
          <h2 className="text-xl font-semibold">{itemType === 1 ? 'Credits' : 'Debits'}</h2>

          {/* NEW BUTTON */}
          <Button
            label={itemType === 1 ? 'New Credit' : 'New Debit'}
            icon="pi pi-plus"
            className="p-button-success"
            onClick={handleAdd}
          />
        </div>
      </Card>

      <Card>
        {(loading || localLoading) && <p>Loading…</p>}
        {error && <p className="text-red-500">{error}</p>}

        <DataTable value={items} paginator rows={10} stripedRows>
          <Column field="name" header="Name" />

          <Column
            field="amount"
            header="Amount"
            body={(row: Item) => {
              const amt = row.amount ?? 0;
              // Keep amount positive; rely on fkItemType to distinguish credit/debit semantics
              return `$${amt.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
            }}
          />

          <Column header="Period" body={(row: Item) => row.TimePeriod?.Name ?? ''} />

          <Column
            header="Actions"
            body={(row: Item) => (
              <div className="flex gap-2">
                <Button
                  icon="pi pi-pencil"
                  label="Edit"
                  className="p-button-secondary"
                  onClick={() => handleEdit(row)}
                />
                <Button
                  icon="pi pi-trash"
                  label="Delete"
                  className="p-button-danger"
                  onClick={() => confirmDelete(row)}
                />
              </div>
            )}
          />
        </DataTable>
      </Card>
    </div>
  );
}
