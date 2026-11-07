// src/features/catalog-query/accounts/AccountsLedgerPanel.tsx
import { useMemo, useState } from 'react';
import { DataTable } from 'primereact/datatable';
import { Column } from 'primereact/column';
import { Dropdown } from 'primereact/dropdown';
import { formatMoney } from '@/features/debt-paydown/types/Debt';
import {
  accountColor,
  describeAccountEvent,
  type AccountEvent,
  type AccountLedger,
} from './accountLedger';

interface AccountsLedgerPanelProps {
  accounts: AccountLedger[];
}

interface AccountRow {
  key: string;
  accountIndex: number;
  accountName: string;
  event: AccountEvent;
  activity: string;
}

const formatDay = (iso: string) => {
  const [y, m, d] = iso.slice(0, 10).split('-').map(Number);
  const local = new Date(y, m - 1, d);
  return `${iso.slice(0, 10)} - ${local.toLocaleString('en-US', { weekday: 'short' })}`;
};

/** The Cards & loans ledger tab: every purchase, payment and interest charge, with the balance after it. */
export function AccountsLedgerPanel({ accounts }: AccountsLedgerPanelProps) {
  /** null = every account. */
  const [accountFilter, setAccountFilter] = useState<number | null>(null);

  const filterOptions = [
    { label: 'All cards and loans', value: null },
    ...accounts.map(a => ({ label: a.name, value: a.accountId })),
  ];

  const rows = useMemo<AccountRow[]>(
    () =>
      accounts
        .flatMap((account, accountIndex) =>
          accountFilter !== null && account.accountId !== accountFilter
            ? []
            : account.events.map((event, i) => ({
                key: `${account.accountId}-${i}`,
                accountIndex,
                accountName: account.name,
                event,
                activity: describeAccountEvent(event),
              })),
        )
        .sort(
          (a, b) => a.event.date.localeCompare(b.event.date) || a.accountIndex - b.accountIndex,
        ),
    [accounts, accountFilter],
  );

  return (
    <div className="app-panel p-4">
      <div className="flex flex-wrap items-center gap-3 mb-3">
        <h3 className="text-lg font-semibold m-0 mr-auto">Cards &amp; Loans Ledger</h3>
        <label htmlFor="accounts-filter" className="text-sm">
          Show
        </label>
        <Dropdown
          inputId="accounts-filter"
          options={filterOptions}
          optionLabel="label"
          optionValue="value"
          value={accountFilter}
          onChange={e => setAccountFilter(e.value ?? null)}
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
        emptyMessage="No purchases, payments or interest fall in this range."
        className="ledger-table"
      >
        <Column header="Date" body={(r: AccountRow) => formatDay(r.event.date)} />
        <Column
          header="Account"
          body={(r: AccountRow) => (
            <span className="inline-flex items-center gap-2">
              <span
                className="paydown-swatch"
                style={{ background: accountColor(r.accountIndex) }}
                aria-hidden="true"
              />
              {r.accountName}
            </span>
          )}
        />
        <Column header="Activity" field="activity" />
        <Column
          header="Amount"
          body={(r: AccountRow) => (
            <span
              style={{
                color: r.event.kind === 'payment' ? '#066F3B' : '#EF5350',
                fontVariantNumeric: 'tabular-nums',
                fontWeight: 600,
              }}
            >
              {r.event.kind === 'payment' ? '−' : '+'}
              {formatMoney(Math.abs(r.event.amount))}
            </span>
          )}
        />
        <Column
          header="Balance"
          body={(r: AccountRow) =>
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
