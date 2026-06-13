// src/features/catalog-command/transactions/components/items/SelectPeriodPage.tsx
import React from 'react';
import { useNavigate } from 'react-router-dom';
import { Card } from 'primereact/card';
import { DataTable } from 'primereact/datatable';
import { Column } from 'primereact/column';
import { Button } from 'primereact/button';
import { ProgressSpinner } from 'primereact/progressspinner';

import { useTimePeriods } from '@/features/catalog-command/admin/timePeriod/hooks/useTimePeriod';
import type { TimePeriod } from '@/features/catalog-command/admin/timePeriod/types/TimePeriod';

interface SelectPeriodPageProps {
  itemType: number; // 1 = Credit, 2 = Debit
}

const FALLBACK_PERIODS: { id: number; name: string }[] = [
  { id: 1, name: 'One Time Occurrence' },
  { id: 2, name: 'Daily' },
  { id: 3, name: 'Weekly' },
  { id: 4, name: 'Every Two Weeks' },
  { id: 5, name: 'Bi-Monthly' },
  { id: 6, name: 'Monthly' },
  { id: 7, name: 'Quarterly' },
  { id: 8, name: 'Semi-Annually' },
  { id: 9, name: 'Annually' },
];

export default function SelectPeriodPage({ itemType }: SelectPeriodPageProps) {
  const navigate = useNavigate();
  const base = itemType === 1 ? '/command/transactions/credits' : '/command/transactions/debits';

  const { data, isLoading, isError } = useTimePeriods();

  // Map API data to TimePeriod[] and sort by id ascending.
  const periods: TimePeriod[] = React.useMemo(() => {
    const src: TimePeriod[] = Array.isArray(data)
      ? (data as TimePeriod[])
      : (FALLBACK_PERIODS as TimePeriod[]);
    // Create a shallow copy and sort to avoid mutating react-query cache
    return [...src].sort((a, b) => a.id - b.id);
  }, [data]);

  function handleChoose(periodId: number) {
    navigate(`${base}/new/${periodId}`);
  }

  // If the period is One Time (id === 1) we offer a direct "Add One Time" action
  const actionBody = (row: TimePeriod) => (
    <div className="flex items-center justify-center gap-2">
      <Button
        label="Select"
        icon="pi pi-check"
        className="p-button-sm compact-select-btn"
        onClick={() => handleChoose(row.id)}
        aria-label={`Select ${row.name}`}
      />

      {row.id === 1 && (
        <Button
          icon="pi pi-plus"
          className="p-button-sm p-button-text"
          aria-label="Add One Time Occurrence"
          onClick={() => navigate(`${base}/new/1`)}
          tooltip="Add One Time"
        />
      )}
    </div>
  );

  // Optional compact header-level shortcut to add a One Time occurrence
  function goToAddOneTime() {
    navigate(`${base}/new/1`);
  }

  return (
    <div className="p-4">
      <Card>
        <div className="flex items-center justify-between">
          <h2 className="text-lg font-semibold">{itemType === 1 ? 'New Credit' : 'New Debit'}</h2>

          <div className="flex items-center gap-2">
            <Button
              label="Add One Time"
              icon="pi pi-calendar"
              className="p-button-sm p-button-primary"
              onClick={goToAddOneTime}
              aria-label="Add One Time Occurrence"
            />
          </div>
        </div>
      </Card>

      <Card className="mt-3">
        {isLoading && (
          <div className="flex items-center justify-center p-4">
            <ProgressSpinner />
          </div>
        )}

        {isError && (
          <div className="p-2 text-yellow-700">
            <small>Could not load time periods from the server. Showing default options.</small>
          </div>
        )}

        <DataTable
          value={periods}
          className="compact-datatable mt-2"
          paginator={false}
          rows={9}
          emptyMessage="No time periods available"
          responsiveLayout="scroll"
          sortField="id"
          sortOrder={1} // ascending
          showGridlines={false}
        >
          <Column field="id" header="ID" style={{ width: '60px', textAlign: 'center' }} />
          <Column
            field="name"
            header="Period"
            body={(r: TimePeriod) => <span className="compact-period-name">{r.name}</span>}
          />
          <Column header=" " body={actionBody} style={{ width: '120px', textAlign: 'center' }} />
        </DataTable>
      </Card>
    </div>
  );
}
