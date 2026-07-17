// src/features/catalog-query/display/DisplayPage.tsx
import React, { useEffect, useState } from 'react';
import * as RadixTabs from '@radix-ui/react-tabs';
import { Calendar } from 'primereact/calendar';
import { Checkbox } from 'primereact/checkbox';
import { Button } from 'primereact/button';

import { getSessionUserId } from '@/app/state/sessionHelpers';

interface Criteria {
  userId: string | null;
  ledgerStartDate: Date | null;
  ledgerEndDate: Date | null;
  includeGrouping: boolean;
}

export default function DisplayPage() {
  const [criteria, setCriteria] = useState<Criteria>(() => ({
    userId: getSessionUserId() ?? null,
    ledgerStartDate: new Date(),
    ledgerEndDate: new Date(Date.now() + 90 * 24 * 60 * 60 * 1000),
    includeGrouping: true,
  }));

  const [activeTab, setActiveTab] = useState<'date' | 'chart' | 'ledger'>('date');

  // Theme detection fallback class so your CSS selectors match reliably
  const [themeClass, setThemeClass] = useState<'theme-light' | 'theme-dark'>(() => {
    try {
      const attr = document?.documentElement?.getAttribute?.('data-theme');
      if (attr === 'light') return 'theme-light';
      if (attr === 'dark') return 'theme-dark';
      if (document?.documentElement?.classList?.contains('light')) return 'theme-light';
      if (document?.body?.classList?.contains('light')) return 'theme-light';
    } catch {
      /* SSR safe */
    }
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
      } catch {
        /* noop */
      }
    });

    observer.observe(document.documentElement, {
      attributes: true,
      attributeFilter: ['data-theme', 'class'],
    });
    observer.observe(document.body, { attributes: true, attributeFilter: ['class'] });

    return () => observer.disconnect();
  }, []);

  const updateStart = (value: Date | null) =>
    setCriteria(prev => ({ ...prev, ledgerStartDate: value }));
  const updateEnd = (value: Date | null) =>
    setCriteria(prev => ({ ...prev, ledgerEndDate: value }));

  return (
    <div className={`${themeClass} p-6`}>
      <h2 className="text-xl font-semibold mb-2">Display</h2>

      <div className="fpfl-divider border-b mb-4" />

      {/* Tabs wrapper — keep .pr-tabs so your overrides apply */}
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
                         data-[state=active]:bg-transparent data-[state=active]:text-sky-700 data-[state=active]:font-semibold
                         data-[state=inactive]:text-gray-500"
            >
              Date Range
            </RadixTabs.Trigger>

            <RadixTabs.Trigger
              value="chart"
              className="fpfl-tab flex items-center gap-2 px-3 py-2 rounded-md font-medium
                         data-[state=active]:bg-transparent data-[state=active]:text-sky-700 data-[state=active]:font-semibold
                         data-[state=inactive]:text-gray-500"
            >
              Chart
            </RadixTabs.Trigger>

            <RadixTabs.Trigger
              value="ledger"
              className="fpfl-tab flex items-center gap-2 px-3 py-2 rounded-md font-medium
                         data-[state=active]:bg-transparent data-[state=active]:text-sky-700 data-[state=active]:font-semibold
                         data-[state=inactive]:text-gray-500"
            >
              Ledger
            </RadixTabs.Trigger>
          </RadixTabs.List>

          <div className="pt-4">
            <RadixTabs.Content value="date" className="outline-none">
              <div className="mt-4">
                <div className="p-4 surface-card border-round shadow-2 max-w-sm">
                  <div className="p-fluid">
                    <div className="field mb-4">
                      <label htmlFor="ledgerStartDate" className="font-bold mb-2 block">
                        Start Date
                      </label>
                      <Calendar
                        id="ledgerStartDate"
                        value={criteria.ledgerStartDate}
                        onChange={e => updateStart(e.value as Date)}
                        dateFormat="mm/dd/yy"
                        showIcon
                        className="w-full"
                      />
                    </div>

                    <div className="field mb-4">
                      <label htmlFor="ledgerEndDate" className="font-bold mb-2 block">
                        End Date
                      </label>
                      <Calendar
                        id="ledgerEndDate"
                        value={criteria.ledgerEndDate}
                        onChange={e => updateEnd(e.value as Date)}
                        dateFormat="mm/dd/yy"
                        showIcon
                        className="w-full"
                      />
                    </div>

                    <div className="field mb-4 flex items-center">
                      <Checkbox
                        inputId="includeGrouping"
                        checked={criteria.includeGrouping}
                        onChange={e =>
                          setCriteria(prev => ({ ...prev, includeGrouping: e.checked ?? false }))
                        }
                      />
                      <label htmlFor="includeGrouping" className="ml-2 font-bold">
                        Grouping?
                      </label>
                    </div>

                    <Button
                      label="Calculate"
                      icon="pi pi-clock"
                      className="p-button-success w-full"
                      onClick={() => setActiveTab('chart')}
                    />
                  </div>
                </div>
              </div>
            </RadixTabs.Content>

            <RadixTabs.Content value="chart" className="outline-none">
              <div className="p-4">
                <h3 className="text-lg font-semibold mb-2">Chart Output</h3>
                <p>The chart will appear here after calculation.</p>
              </div>
            </RadixTabs.Content>

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
