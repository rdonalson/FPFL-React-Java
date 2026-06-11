// src/features/catalog-command/transactions/pages/ItemsListPage.tsx
//import { useEffect, useState } from 'react';
import { useEffect, useRef } from 'react';
import { useItem } from '../../hooks/useItem';
import { getSessionUserId } from '@/api/utils/userId';
import { DataTable } from 'primereact/datatable';
import { Column } from 'primereact/column';
import { Button } from 'primereact/button';
import { Card } from 'primereact/card';

export function ItemsListPage() {
  const userId = getSessionUserId();
  const { items, loading, error, loadForUserAndType } = useItem();
  const didLoadRef = useRef(false);
  // const [setSelectedItem] = useState(null);

  useEffect(() => {
    if (didLoadRef.current) return;
    didLoadRef.current = true;

    loadForUserAndType(userId, 1);
  }, [userId, loadForUserAndType]);

  return (
    <div className="p-4 flex flex-col gap-4">
      <Card>
        <div className="flex justify-between items-center">
          <h2 className="text-xl font-semibold">Credits</h2>

          {/* ⭐ NEW BUTTON */}
          <Button
            label="New Credit"
            icon="pi pi-plus"
            className="p-button-success"
            // onClick={() => setSelectedItem(null)}
          />
        </div>
      </Card>

      <Card>
        {loading && <p>Loading…</p>}
        {error && <p className="text-red-500">{error}</p>}

        <DataTable value={items} paginator rows={10} stripedRows>
          <Column field="name" header="Name" />

          <Column field="amount" header="Amount" body={row => `$${row.amount}`} />

          <Column header="Period" body={row => row.TimePeriod?.Name ?? ''} />

          <Column
            header="Actions"
            body={row => (
              <div className="flex gap-2">
                <Button
                  icon="pi pi-pencil"
                  label="Edit"
                  severity="secondary"
                  // onClick={() => setSelectedItem(row)}
                />
                <Button
                  icon="pi pi-trash"
                  label="Delete"
                  severity="danger"
                  onClick={() => console.log('delete', row)}
                />
              </div>
            )}
          />
        </DataTable>
      </Card>
    </div>
  );
}
