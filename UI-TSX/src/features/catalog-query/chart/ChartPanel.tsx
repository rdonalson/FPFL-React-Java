// src/features/catalog-query/display/ChartPanel.tsx
import React from 'react';
import { Chart } from 'primereact/chart';
import type { LedgerDto } from '../types/DispayResponse';
import { useIsSmallScreen } from '@/shared/hooks/useIsSmallScreen';

interface ChartPanelProps {
  ledger: LedgerDto[] | null;
}

export function ChartPanel({ ledger }: ChartPanelProps) {
  const isSm = useIsSmallScreen(); // updates on rotate

  if (!ledger || ledger.length === 0) {
    return (
      <div className="app-panel p-4">
        <h3 className="text-lg font-semibold mb-2">Chart Output</h3>
        <p>No data available. Run calculation first.</p>
      </div>
    );
  }

  const labels = ledger.map(row => row.wDate);
  const runningTotals = ledger.map(row => row.runningTotal);

  // Debit values are already negative in your API — plot them directly
  const debitTotals = ledger.map(row => row.debitSummary);

  const creditTotals = ledger.map(row => row.creditSummary);

  const data = {
    labels,
    datasets: [
      {
        label: 'Running Total',
        data: runningTotals,
        borderColor: '#C19A6B', // light brown
        backgroundColor: 'rgba(193,154,107,0.25)',
        tension: 0.3,
        fill: true,
      },
      {
        label: 'Debit Summary',
        data: debitTotals, // negative values shown directly
        borderColor: '#EF5350',
        backgroundColor: 'rgba(239,83,80,0.2)',
        tension: 0.3,
        fill: true,
      },
      {
        label: 'Credit Summary',
        data: creditTotals,
        borderColor: '#66BB6A',
        backgroundColor: 'rgba(102,187,106,0.2)',
        tension: 0.3,
        fill: true,
      },
    ],
  };

  // SM (phone, portrait or landscape): smaller legend/ticks so the plot area gets most of the width

  const options = {
    responsive: true,
    maintainAspectRatio: false,
    layout: { padding: isSm ? 0 : 8 },
    plugins: {
      legend: {
        position: 'top',
        labels: isSm ? { boxWidth: 12, font: { size: 10 } } : {},
      },
      tooltip: { mode: 'index', intersect: false },
    },
    scales: {
      x: {
        ticks: isSm
          ? { color: '#666', font: { size: 10 }, maxRotation: 0, autoSkip: true, maxTicksLimit: 5 }
          : { color: '#666' },
        grid: { display: false },
      },
      y: {
        ticks: isSm ? { color: '#666', font: { size: 10 }, maxTicksLimit: 6 } : { color: '#666' },
        grid: { color: '#ddd' },
      },
    },
  };

  return (
    <div className="app-panel p-4">
      <h3 className="text-lg font-semibold mb-4">Chart Output</h3>

      <div
        style={{
          height: '600px',
          minHeight: '400px',
          display: 'flex',
          flexDirection: 'column',
        }}
      >
        <Chart type="line" data={data} options={options} />
      </div>
    </div>
  );
}
