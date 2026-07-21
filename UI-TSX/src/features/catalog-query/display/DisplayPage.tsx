// src/features/catalog-query/display/DisplayPage.tsx
import React, { useEffect, useState } from 'react';
import * as RadixTabs from '@radix-ui/react-tabs';

import { getSessionUserId } from '@/app/state/sessionHelpers';

import { DateRangePanel } from '../range/DateRangePanel';
import type { DisplayRequest } from '../types/DisplayRequest';
import { fetchDisplayLedger } from '../api/displayApi';

import type { LedgerDto } from '../types/DispayResponse';
import { ChartPanel } from '../chart/ChartPanel'; // <-- your new chart panel

export default function DisplayPage() {
  const [criteria, setCriteria] = useState<DisplayRequest>(() => ({
    userId: getSessionUserId() ?? null,
    ledgerStartDate: new Date(),
    ledgerEndDate: new Date(Date.now() + 90 * 24 * 60 * 60 * 1000),
    includeGrouping: true,
  }));

  const [ledgerRows, setLedgerRows] = useState<LedgerDto[] | null>(null);
  const [activeTab, setActiveTab] = useState<'date' | 'chart' | 'ledger'>('date');

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
      const result = await fetchDisplayLedger(criteria);
      setLedgerRows(result);
      setActiveTab('chart');
    } catch (err) {
      console.error('Display API error:', err);
    }
  };

  return (
    <div className={`${themeClass} p-6`}>
      <h2 className="text-xl font-semibold mb-2">Display</h2>
      <div className="fpfl-divider border-b mb-4" />

      <div className="pr-tabs mb-2">
        <RadixTabs.Root
          defaultValue={activeTab}
          value={activeTab}
          onValueChange={(val: string) =>
            setActiveTab((val as 'date' | 'chart' | 'ledger') ?? 'date')
          }
          className="max-w-full"
        >
          <RadixTabs.List
            className="fpfl-tabs-list flex gap-4 border-b pb-2"
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
              <div className="p-4">
                <h3 className="text-lg font-semibold mb-2">Ledger Details</h3>
                <p>This tab will contain the expandable ledger list.</p>
              </div>
            </RadixTabs.Content>
          </div>
        </RadixTabs.Root>
      </div>

      <div className="fpfl-divider border-b mb-4" />
    </div>
  );
}
