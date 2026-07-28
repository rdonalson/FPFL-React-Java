import React, { useState } from 'react';
import { DataTable } from 'primereact/datatable';
import { Column } from 'primereact/column';
import type { LedgerDto, ItemDto } from '../types/DispayResponse';

interface LedgerPanelProps {
  ledger: LedgerDto[] | null;
}

const formatCurrency = (value: number) =>
  value.toLocaleString('en-US', {
    style: 'currency',
    currency: 'USD',
    minimumFractionDigits: 2,
  });

const formatLedgerDate = (iso: string) => {
  const d = new Date(iso);
  const yyyy = d.getFullYear();
  const mm = String(d.getMonth() + 1).padStart(2, '0');
  const dd = String(d.getDate()).padStart(2, '0');
  const weekday = d.toLocaleString('en-US', { weekday: 'short' });
  return `${yyyy}-${mm}-${dd} - ${weekday}`;
};

export function LedgerPanel({ ledger }: LedgerPanelProps) {
  const [expandedRows, setExpandedRows] = useState<any>({});

  if (!ledger || ledger.length === 0) {
    return (
      <div className="p-4">
        <h3 className="text-lg font-semibold mb-2">Ledger Details</h3>
        <p>No ledger data available.</p>
      </div>
    );
  }

  const itemTemplate = (row: LedgerDto) => (
    <div className="p-3">
      <h4 className="font-semibold mb-2">Items</h4>

      <DataTable value={row.items} size="small">
        <Column field="name" header="Item Name" />

        <Column
          field="amount"
          header="Amount"
          body={(item: ItemDto) => (
            <span style={{ color: item.amount < 0 ? '#EF5350' : '#66BB6A' }}>
              {formatCurrency(item.amount)}
            </span>
          )}
        />

        <Column field="period" header="Period" />
        <Column field="itemType" header="Type" />
      </DataTable>
    </div>
  );

  return (
    <div className="p-4">
      <h3 className="text-lg font-semibold mb-4">Ledger Details</h3>

      <DataTable
        value={ledger}
        dataKey="rollupKey"
        expandedRows={expandedRows}
        onRowToggle={e => setExpandedRows(e.data)}
        rowExpansionTemplate={itemTemplate}
      >
        {/* Conditional expander */}
        <Column
          header=""
          style={{ width: '3rem' }}
          body={(row: LedgerDto) => {
            const hasItems = row.items && row.items.length > 0;
            if (!hasItems) return null;

            const isExpanded = !!expandedRows?.[row.rollupKey];

            return (
              <button
                className="p-link"
                onClick={() =>
                  setExpandedRows((prev: any) => {
                    const expanded = !!prev?.[row.rollupKey];

                    if (expanded) {
                      const updated = { ...prev };
                      delete updated[row.rollupKey];
                      return updated;
                    }

                    return {
                      ...prev,
                      [row.rollupKey]: true,
                    };
                  })
                }
              >
                <span className={`pi ${isExpanded ? 'pi-chevron-down' : 'pi-chevron-right'}`} />
              </button>
            );
          }}
        />

        {/* Date */}
        <Column
          field="wDate"
          header="Date"
          body={(row: LedgerDto) => formatLedgerDate(row.wDate)}
        />

        {/* Credit Summary */}
        <Column
          field="creditSummary"
          header="Credit Summary"
          body={(row: LedgerDto) => (
            <span style={{ color: '#66BB6A' }}>{formatCurrency(row.creditSummary)}</span>
          )}
        />

        {/* Debit Summary */}
        <Column
          field="debitSummary"
          header="Debit Summary"
          body={(row: LedgerDto) => (
            <span style={{ color: '#EF5350' }}>{formatCurrency(row.debitSummary)}</span>
          )}
        />

        {/* Net Change */}
        <Column
          field="net"
          header="Net Change"
          body={(row: LedgerDto) => (
            <span style={{ color: row.net < 0 ? '#EF5350' : '#66BB6A' }}>
              {formatCurrency(row.net)}
            </span>
          )}
        />

        {/* Balance */}
        <Column
          field="runningTotal"
          header="Balance"
          body={(row: LedgerDto) => (
            <span style={{ fontWeight: 600 }}>{formatCurrency(row.runningTotal)}</span>
          )}
        />
      </DataTable>
    </div>
  );
}
