import React, { useCallback, useEffect, useRef, useState } from 'react';
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
  const debitHeaderRef = useRef<HTMLDivElement | null>(null);
  const rootRef = useRef<HTMLDivElement | null>(null);

  const setDebitHeader = useCallback((el: HTMLDivElement | null) => {
    debitHeaderRef.current = el;
  }, []);

  useEffect(() => {
    const setWidth = () => {
      const w = debitHeaderRef.current?.getBoundingClientRect().width ?? 160;
      if (rootRef.current) {
        rootRef.current.style.setProperty('--debit-col-width', `${Math.round(w)}px`);
      }
    };
    setWidth();
    window.addEventListener('resize', setWidth);
    return () => window.removeEventListener('resize', setWidth);
  }, []);

  if (!ledger || ledger.length === 0) {
    return (
      <div className="p-4">
        <h3 className="text-lg font-semibold mb-2">Ledger Details</h3>
        <p>No ledger data available.</p>
      </div>
    );
  }

  function renderCurrencyOrDash(value?: number, color?: string) {
    if (value == null) return <span>--</span>;
    if (value === 0) return <span>--</span>;
    return <span style={{ color }}>{formatCurrency(value)}</span>;
  }

  // Horizontal columns item template
  const itemTemplate = (row: LedgerDto) => (
    <div
      className="p-3 ledger-detail-row"
      role="region"
      aria-label={`Details for ${row.rollupKey}`}
      id={`detail-${row.rollupKey}`}
    >
      <div className="ledger-detail-panel-container">
        <div className="ledger-detail-panel">
          <div className="ledger-detail-columns-header" aria-hidden>
            <div className="col col-type">Type</div>
            <div className="col col-amount">Amount</div>
            <div className="col col-name">Name</div>
            <div className="col col-period">Period</div>
          </div>

          <div className="ledger-detail-columns" role="list">
            {row.items?.map((it: ItemDto, idx: number) => (
              <div key={idx} className="ledger-detail-row-item" role="listitem">
                <div
                  className="col col-type"
                  style={{ color: it.amount < 0 ? '#EF5350' : '#066F3B', fontWeight: 700 }}
                >
                  {it.itemType}
                </div>

                <div
                  className="col col-amount"
                  style={{
                    color: it.amount < 0 ? '#EF5350' : '#066F3B',
                    fontVariantNumeric: 'tabular-nums',
                    fontWeight: 700,
                  }}
                >
                  {formatCurrency(it.amount)}
                </div>

                <div className="col col-name">{it.name}</div>

                <div className="col col-period">{it.period}</div>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );

  return (
    <div className="p-4" ref={rootRef}>
      <h3 className="text-lg font-semibold mb-4">Ledger Details</h3>

      <DataTable
        value={ledger}
        dataKey="rollupKey"
        expandedRows={expandedRows}
        onRowToggle={e => setExpandedRows(e.data)}
        rowExpansionTemplate={itemTemplate}
        className="ledger-table"
      >
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
                aria-expanded={!!expandedRows?.[row.rollupKey]}
                aria-controls={`detail-${row.rollupKey}`}
              >
                <span className={`pi ${isExpanded ? 'pi-chevron-down' : 'pi-chevron-right'}`} />
              </button>
            );
          }}
        />

        <Column
          field="wDate"
          header="Date"
          body={(row: LedgerDto) => formatLedgerDate(row.wDate)}
          headerClassName="p-column-header-content-date"
          style={{ textAlign: 'left' }}
        />

        <Column
          field="creditSummary"
          header="Credit Summary"
          body={(row: LedgerDto) => renderCurrencyOrDash(row.creditSummary, '#66BB6A')}
        />

        <Column
          header={() => <div ref={setDebitHeader}>Debit Summary</div>}
          field="debitSummary"
          body={(row: LedgerDto) => renderCurrencyOrDash(row.debitSummary, '#EF5350')}
        />

        <Column
          field="net"
          header="Net Change"
          body={(row: LedgerDto) => {
            const v = row.net ?? 0;
            if (v === 0) return <span>--</span>;
            return (
              <span style={{ color: v < 0 ? '#EF5350' : '#66BB6A' }}>{formatCurrency(v)}</span>
            );
          }}
        />

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
