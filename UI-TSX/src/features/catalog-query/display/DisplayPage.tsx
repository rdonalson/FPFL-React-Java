// src/features/catalog-query/display/DisplayPage.tsx
import React, { useEffect, useState } from 'react';
import * as RadixTabs from '@radix-ui/react-tabs';
import { useNavigate } from 'react-router-dom';
import { Button } from 'primereact/button';

import { getSessionUserId } from '@/app/state/sessionHelpers';
import { useToast } from '@/shared/hooks/useToast';
import { toDateOnlyString } from '@/shared/utils/dateUtils';

import { DateRangePanel } from '../range/DateRangePanel';
import type { DisplayRequest } from '../types/DisplayRequest';
import { fetchDisplayLedger, fetchDisplayPaydown } from '../api/displayApi';

import type { LedgerDto } from '../types/DispayResponse';
import { ChartPanel } from '../chart/ChartPanel'; // <-- your new chart panel
import { LedgerPanel } from '../ledger/LedgerPanel';
import { PaydownChartPanel } from '../paydown/PaydownChartPanel';
import { PaydownLedgerPanel } from '../paydown/PaydownLedgerPanel';
import type { DebtPaydown } from '../paydown/paydown';
import { exportLedgerToExcel } from '../export/ledgerExport';

type DisplayTab = 'date' | 'chart' | 'ledger' | 'paydown-chart' | 'paydown-ledger';

export default function DisplayPage() {
  const navigate = useNavigate();
  const [criteria, setCriteria] = useState<DisplayRequest>(() => ({
    userId: getSessionUserId() ?? null,
    ledgerStartDate: new Date(),
    ledgerEndDate: new Date(Date.now() + 90 * 24 * 60 * 60 * 1000),
    includeGrouping: true,
  }));

  const [ledgerRows, setLedgerRows] = useState<LedgerDto[] | null>(null);
  const [paydown, setPaydown] = useState<DebtPaydown[]>([]);
  const [activeTab, setActiveTab] = useState<DisplayTab>('date');
  /** The range the results on screen were calculated for ('yyyy-MM-dd'), used to title the export. */
  const [shownRange, setShownRange] = useState<{ start: string; end: string } | null>(null);
  const [exporting, setExporting] = useState(false);
  const toast = useToast();

  // theme detection (unchanged)
  const [themeClass, setThemeClass] = useState<'theme-light' | 'theme-dark'>(() => {
    try {
      const attr = document?.documentElement?.getAttribute?.('data-theme');
      if (attr === 'light') return 'theme-light';
      if (attr === 'dark') return 'theme-dark';
      if (document?.documentElement?.classList?.contains('light')) return 'theme-light';
      if (document?.body?.classList?.contains('light')) return 'theme-light';
    } catch {}
    return 'theme-dark';
  });

  useEffect(() => {
    const observer = new MutationObserver(() => {
      try {
        const attr = document.documentElement.getAttribute('data-theme');
        if (attr === 'light') setThemeClass('theme-light');
        else if (attr === 'dark') setThemeClass('theme-dark');
        else if (document.documentElement.classList.contains('light')) setThemeClass('theme-light');
        else if (document.body.classList.contains('light')) setThemeClass('theme-light');
        else setThemeClass('theme-dark');
      } catch {}
    });

    observer.observe(document.documentElement, {
      attributes: true,
      attributeFilter: ['data-theme', 'class'],
    });
    observer.observe(document.body, { attributes: true, attributeFilter: ['class'] });

    return () => observer.disconnect();
  }, []);

  // update helpers
  const updateStart = (value: Date | null) =>
    setCriteria(prev => ({ ...prev, ledgerStartDate: value }));

  const updateEnd = (value: Date | null) =>
    setCriteria(prev => ({ ...prev, ledgerEndDate: value }));

  const updateGrouping = (value: boolean) =>
    setCriteria(prev => ({ ...prev, includeGrouping: value }));

  const handleCalculate = async () => {
    try {
      // Debt Paydown is extra: if it fails, the forecast still shows
      const [result, debts] = await Promise.all([
        fetchDisplayLedger(criteria),
        fetchDisplayPaydown(criteria).catch(err => {
          console.error('Paydown API error:', err);
          return [] as DebtPaydown[];
        }),
      ]);
      setLedgerRows(result);
      setPaydown(debts);
      const start = toDateOnlyString(criteria.ledgerStartDate);
      const end = toDateOnlyString(criteria.ledgerEndDate);
      setShownRange(start && end ? { start, end } : null);
      setActiveTab('chart');
    } catch (err) {
      console.error('Display API error:', err);
    }
  };

  /** Downloads what's on screen as an Excel workbook laid out like the Ledger tab. */
  const handleExport = async () => {
    if (!ledgerRows?.length || !shownRange) return;
    setExporting(true);
    try {
      await exportLedgerToExcel(ledgerRows, { ...shownRange, paydown });
    } catch (err) {
      console.error('Excel export error:', err);
      toast?.current?.show({
        severity: 'error',
        summary: 'Export failed',
        detail: 'The Excel file could not be created. Please try again.',
        life: 5000,
      });
    } finally {
      setExporting(false);
    }
  };

  return (
    <div className={`${themeClass} app-page app-page-gutter p-6`}>
      <div className="mb-2">
        <Button
          label="Back to Home"
          icon="pi pi-arrow-left"
          className="p-button-text"
          onClick={() => navigate('/')}
        />
      </div>

      <div className="flex flex-wrap items-center justify-between gap-2 mb-2">
        <h2 className="text-xl font-semibold">Display</h2>
        <Button
          label="Export to Excel"
          icon="pi pi-file-excel"
          outlined
          loading={exporting}
          disabled={!ledgerRows?.length || !shownRange || exporting}
          onClick={handleExport}
        />
      </div>
      <div className="fpfl-divider border-b mb-4" />

      <div className="pr-tabs mb-2">
        <RadixTabs.Root
          defaultValue={activeTab}
          value={activeTab}
          onValueChange={(val: string) => setActiveTab((val as DisplayTab) ?? 'date')}
          className="max-w-full"
        >
          <RadixTabs.List
            className="fpfl-tabs-list flex flex-wrap gap-4 border-b pb-2"
            aria-label="Display tabs"
          >
            <RadixTabs.Trigger
              value="date"
              className="fpfl-tab flex items-center gap-2 px-3 py-2 rounded-md font-medium
                         data-[state=active]:text-sky-700 data-[state=active]:font-semibold
                         data-[state=inactive]:text-gray-500"
            >
              Date Range
            </RadixTabs.Trigger>

            <RadixTabs.Trigger
              value="chart"
              className="fpfl-tab flex items-center gap-2 px-3 py-2 rounded-md font-medium
                         data-[state=active]:text-sky-700 data-[state=active]:font-semibold
                         data-[state=inactive]:text-gray-500"
            >
              Chart
            </RadixTabs.Trigger>

            <RadixTabs.Trigger
              value="ledger"
              className="fpfl-tab flex items-center gap-2 px-3 py-2 rounded-md font-medium
                         data-[state=active]:text-sky-700 data-[state=active]:font-semibold
                         data-[state=inactive]:text-gray-500"
            >
              Ledger
            </RadixTabs.Trigger>

            {paydown.length > 0 && (
              <>
                <RadixTabs.Trigger
                  value="paydown-chart"
                  className="fpfl-tab flex items-center gap-2 px-3 py-2 rounded-md font-medium
                             data-[state=active]:text-sky-700 data-[state=active]:font-semibold
                             data-[state=inactive]:text-gray-500"
                >
                  Paydown Chart
                </RadixTabs.Trigger>

                <RadixTabs.Trigger
                  value="paydown-ledger"
                  className="fpfl-tab flex items-center gap-2 px-3 py-2 rounded-md font-medium
                             data-[state=active]:text-sky-700 data-[state=active]:font-semibold
                             data-[state=inactive]:text-gray-500"
                >
                  Paydown Ledger
                </RadixTabs.Trigger>
              </>
            )}
          </RadixTabs.List>

          <div className="pt-4">
            {/* DATE RANGE PANEL */}
            <RadixTabs.Content value="date" className="outline-none">
              <DateRangePanel
                criteria={criteria}
                updateStart={updateStart}
                updateEnd={updateEnd}
                updateGrouping={updateGrouping}
                onCalculate={handleCalculate}
              />
            </RadixTabs.Content>

            {/* CHART PANEL */}
            <RadixTabs.Content
              value="chart"
              className="outline-none"
              style={{ minHeight: '900px', height: '900px' }}
            >
              <ChartPanel ledger={ledgerRows} />
            </RadixTabs.Content>

            {/* LEDGER PANEL (placeholder) */}
            <RadixTabs.Content value="ledger" className="outline-none">
              <LedgerPanel ledger={ledgerRows} />
            </RadixTabs.Content>

            {/* DEBT PAYDOWN PANELS (only when a debt is tracked) */}
            {paydown.length > 0 && (
              <>
                <RadixTabs.Content value="paydown-chart" className="outline-none">
                  <PaydownChartPanel debts={paydown} ledger={ledgerRows} />
                </RadixTabs.Content>

                <RadixTabs.Content value="paydown-ledger" className="outline-none">
                  <PaydownLedgerPanel debts={paydown} />
                </RadixTabs.Content>
              </>
            )}
          </div>
        </RadixTabs.Root>
      </div>

      <div className="fpfl-divider border-b mb-4" />
    </div>
  );
}
