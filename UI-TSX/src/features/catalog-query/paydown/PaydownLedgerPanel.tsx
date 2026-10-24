// src/features/catalog-query/paydown/PaydownLedgerPanel.tsx
import { useMemo, useState } from 'react';
import { DataTable } from 'primereact/datatable';
import { Column } from 'primereact/column';
import { Dropdown } from 'primereact/dropdown';
import { formatMoney } from '@/features/debt-paydown/types/Debt';
import { describeEvent, paydownColor, type DebtPaydown, type PaydownEvent } from './paydown';

interface PaydownLedgerPanelProps {
  debts: DebtPaydown[];
}

interface PaydownRow {
  key: string;
  debtIndex: number;
  debtName: string;
  event: PaydownEvent;
  activity: string;
}

const formatDay = (iso: string) => {
  const [y, m, d] = iso.slice(0, 10).split('-').map(Number);
  const local = new Date(y, m - 1, d);
  return `${iso.slice(0, 10)} - ${local.toLocaleString('en-US', { weekday: 'short' })}`;
};

/** The Paydown ledger tab: every payment, allocation and interest charge, with the balance left. */
export function PaydownLedgerPanel({ debts }: PaydownLedgerPanelProps) {
  /** null = every debt. */
  const [debtFilter, setDebtFilter] = useState<number | null>(null);

  const filterOptions = [
    { label: 'All debts', value: null },
    ...debts.map(d => ({ label: d.name ?? 'Debt', value: d.debtId })),
  ];

  const rows = useMemo<PaydownRow[]>(
    () =>
      debts
        .flatMap((debt, debtIndex) =>
          debtFilter !== null && debt.debtId !== debtFilter
            ? []
            : debt.events.map((event, i) => ({
                key: `${debt.debtId}-${i}`,
                debtIndex,
                debtName: debt.name ?? 'Debt',
                event,
                activity: describeEvent(event),
              })),
        )
        .sort((a, b) => a.event.date.localeCompare(b.event.date) || a.debtIndex - b.debtIndex),
    [debts, debtFilter],
  );

  return (
    <div className="app-panel p-4">
      <div className="flex flex-wrap items-center gap-3 mb-3">
        <h3 className="text-lg font-semibold m-0 mr-auto">Paydown Ledger</h3>
        <label htmlFor="paydown-filter" className="text-sm">
          Show
        </label>
        <Dropdown
          inputId="paydown-filter"
          options={filterOptions}
          optionLabel="label"
          optionValue="value"
          value={debtFilter}
          onChange={e => setDebtFilter(e.value ?? null)}
        />
        <span className="text-sm text-color-secondary">
          {rows.length} {rows.length === 1 ? 'entry' : 'entries'}
        </span>
      </div>

      <DataTable
        value={rows}
        dataKey="key"
        stripedRows
        paginator={rows.length > 31}
        rows={31}
        emptyMessage="No payments or interest fall in this range."
        className="ledger-table"
      >
        <Column header="Date" body={(r: PaydownRow) => formatDay(r.event.date)} />
        <Column
          header="Debt"
          body={(r: PaydownRow) => (
            <span className="inline-flex items-center gap-2">
              <span
                className="paydown-swatch"
                style={{ background: paydownColor(r.debtIndex) }}
                aria-hidden="true"
              />
              {r.debtName}
            </span>
          )}
        />
        <Column header="Activity" field="activity" />
        <Column
          header="Amount"
          body={(r: PaydownRow) => (
            <span
              style={{
                color: r.event.kind === 'interest' ? '#EF5350' : '#066F3B',
                fontVariantNumeric: 'tabular-nums',
                fontWeight: 600,
              }}
            >
              {r.event.kind === 'interest' ? '+' : ''}
              {formatMoney(Math.abs(r.event.amount))}
            </span>
          )}
        />
        <Column
          header="Balance left"
          body={(r: PaydownRow) =>
            r.event.balanceAfter === 0 ? (
              <strong style={{ color: '#066F3B' }}>Paid off</strong>
            ) : (
              <span style={{ fontVariantNumeric: 'tabular-nums' }}>
                {formatMoney(r.event.balanceAfter)}
              </span>
            )
          }
        />
      </DataTable>
    </div>
  );
}
