import { beforeEach, describe, expect, it, vi } from 'vitest';
import { fireEvent, render, screen, waitFor, within } from '@testing-library/react';

// Chart.js needs a real canvas; record what would be drawn instead
vi.mock('primereact/chart', () => ({
  Chart: ({
    data,
  }: {
    data: { labels: string[]; datasets: { label: string; data: number[] }[] };
  }) => (
    <div
      data-testid="chart"
      data-labels={JSON.stringify(data.labels)}
      data-datasets={JSON.stringify(data.datasets.map(d => ({ label: d.label, data: d.data })))}
    />
  ),
}));
const displayApi = vi.hoisted(() => ({
  fetchDisplayLedger: vi.fn(),
  fetchDisplayPaydown: vi.fn(),
}));
vi.mock('./api/displayApi', () => displayApi);
const ledgerExport = vi.hoisted(() => ({ exportLedgerToExcel: vi.fn() }));
vi.mock('./export/ledgerExport', () => ledgerExport);
vi.mock('@/app/state/sessionHelpers', () => ({ getSessionUserId: () => 'user-1' }));

import { ChartPanel } from './chart/ChartPanel';
import { LedgerPanel } from './ledger/LedgerPanel';
import { DateRangePanel } from './range/DateRangePanel';
import { PaydownChartPanel } from './paydown/PaydownChartPanel';
import DisplayPage from './display/DisplayPage';
import { LEDGER, PAYDOWN } from './test-ledger';
import { renderWithProviders } from '@/test/render';

const chartData = () => {
  const el = screen.getByTestId('chart');
  return { labels: JSON.parse(el.dataset.labels!), datasets: JSON.parse(el.dataset.datasets!) };
};

beforeEach(() => vi.clearAllMocks());

describe('ChartPanel', () => {
  it('asks for a calculation first', () => {
    render(<ChartPanel ledger={null} />);
    expect(screen.getByText('No data available. Run calculation first.')).toBeInTheDocument();
  });

  it('plots the running total, debits and credits per day', () => {
    render(<ChartPanel ledger={LEDGER} />);
    const { labels, datasets } = chartData();
    expect(labels).toEqual(['2026-10-30', '2026-10-31']);
    expect(datasets).toEqual([
      { label: 'Running Total', data: [1600, 1600] },
      { label: 'Debit Summary', data: [-500, 0] },
      { label: 'Credit Summary', data: [2000, 0] },
    ]);
  });
});

describe('LedgerPanel', () => {
  it('says when there is nothing to show', () => {
    render(<LedgerPanel ledger={[]} />);
    expect(screen.getByText('No ledger data available.')).toBeInTheDocument();
  });

  it('shows one row per day with money formatted and quiet days as --', () => {
    render(<LedgerPanel ledger={LEDGER} />);
    expect(screen.getByText('2026-10-30 - Fri')).toBeInTheDocument();
    expect(screen.getByText('2026-10-31 - Sat')).toBeInTheDocument();
    expect(screen.getByText('$2,000.00')).toBeInTheDocument();
    expect(screen.getByText('-$500.00')).toBeInTheDocument();
    expect(screen.getAllByText('$1,600.00')).toHaveLength(2);
    expect(screen.getAllByText('--').length).toBeGreaterThanOrEqual(3);
  });

  it("expands a day to its entries, including Debt Paydown's", () => {
    render(<LedgerPanel ledger={LEDGER} />);
    const toggle = screen.getByRole('button', { expanded: false });
    fireEvent.click(toggle);

    const details = screen.getByRole('region', { name: 'Details for 1' });
    expect(within(details).getByText('Paycheck')).toBeInTheDocument();
    expect(within(details).getByText('Debt Paydown')).toBeInTheDocument();
    expect(within(details).getByText('25% of Paycheck')).toBeInTheDocument();

    fireEvent.click(screen.getByRole('button', { expanded: true }));
    expect(screen.queryByRole('region', { name: 'Details for 1' })).not.toBeInTheDocument();
  });

  it('only days with entries can be expanded', () => {
    render(<LedgerPanel ledger={LEDGER} />);
    expect(screen.getAllByRole('button', { expanded: false })).toHaveLength(1);
  });
});

describe('DateRangePanel', () => {
  it('shows the range and runs the calculation', () => {
    const onCalculate = vi.fn();
    render(
      <DateRangePanel
        criteria={{
          userId: 'u',
          ledgerStartDate: new Date(2026, 9, 1),
          ledgerEndDate: new Date(2026, 11, 31),
          includeGrouping: false,
        }}
        updateStart={vi.fn()}
        updateEnd={vi.fn()}
        updateGrouping={vi.fn()}
        onCalculate={onCalculate}
      />,
    );

    expect(screen.getByLabelText('Start Date')).toHaveValue('10/01/2026');
    expect(screen.getByLabelText('End Date')).toHaveValue('12/31/2026');
    expect(screen.queryByText('Grouping?')).not.toBeInTheDocument(); // hidden until grouping exists
    fireEvent.click(screen.getByRole('button', { name: 'Calculate' }));
    expect(onCalculate).toHaveBeenCalled();
  });
});

describe('PaydownChartPanel', () => {
  it('summarizes each debt and charts its balance', () => {
    render(<PaydownChartPanel debts={PAYDOWN} ledger={LEDGER} />);

    expect(screen.getByRole('heading', { name: 'Chase Visa' })).toBeInTheDocument();
    expect(screen.getByText(/Paid off Nov 13, 2026/)).toBeInTheDocument();
    expect(screen.getByText(/\(after this range\)/)).toBeInTheDocument();
    expect(chartData().datasets).toEqual([{ label: 'Chase Visa', data: [500, 500] }]);
  });

  it('warns when a debt is never paid off', () => {
    render(
      <PaydownChartPanel
        debts={[{ ...PAYDOWN[0], payoffDate: null, totalInterest: 12 }]}
        ledger={LEDGER}
      />,
    );
    expect(screen.getByText(/Not paid off within 30 years/)).toBeInTheDocument();
    expect(screen.getByText('Interest')).toBeInTheDocument();
  });
});

describe('DisplayPage', () => {
  const tab = (name: string) => screen.queryByRole('tab', { name });

  it('calculates and shows the chart, adding the paydown tabs when a debt is tracked', async () => {
    displayApi.fetchDisplayLedger.mockResolvedValue(LEDGER);
    displayApi.fetchDisplayPaydown.mockResolvedValue(PAYDOWN);
    renderWithProviders(<DisplayPage />);

    expect(tab('Paydown Chart')).not.toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: 'Calculate' }));

    await waitFor(() => expect(tab('Chart')).toHaveAttribute('aria-selected', 'true'));
    expect(displayApi.fetchDisplayLedger.mock.calls[0][0]).toMatchObject({ userId: 'user-1' });
    expect(tab('Paydown Chart')).toBeInTheDocument();
    expect(tab('Paydown Ledger')).toBeInTheDocument();
  });

  it('works without debts, and when the paydown call fails', async () => {
    vi.spyOn(console, 'error').mockImplementation(() => {});
    displayApi.fetchDisplayLedger.mockResolvedValue(LEDGER);
    displayApi.fetchDisplayPaydown.mockRejectedValue(new Error('down'));
    renderWithProviders(<DisplayPage />);

    fireEvent.click(screen.getByRole('button', { name: 'Calculate' }));

    await waitFor(() => expect(tab('Chart')).toHaveAttribute('aria-selected', 'true'));
    expect(tab('Paydown Chart')).not.toBeInTheDocument();
  });

  it('exports what is on screen to Excel once calculated', async () => {
    displayApi.fetchDisplayLedger.mockResolvedValue(LEDGER);
    displayApi.fetchDisplayPaydown.mockResolvedValue(PAYDOWN);
    ledgerExport.exportLedgerToExcel.mockResolvedValue(undefined);
    renderWithProviders(<DisplayPage />);

    const exportButton = () => screen.getByRole('button', { name: /Export to Excel/ });
    expect(exportButton()).toBeDisabled();

    fireEvent.click(screen.getByRole('button', { name: 'Calculate' }));
    await waitFor(() => expect(exportButton()).toBeEnabled());
    fireEvent.click(exportButton());

    await waitFor(() => expect(ledgerExport.exportLedgerToExcel).toHaveBeenCalledTimes(1));
    const [days, options] = ledgerExport.exportLedgerToExcel.mock.calls[0];
    expect(days).toBe(LEDGER);
    expect(options.paydown).toBe(PAYDOWN);
    expect(options.start).toMatch(/^\d{4}-\d{2}-\d{2}$/);
    expect(options.end).toMatch(/^\d{4}-\d{2}-\d{2}$/);
  });

  it('keeps the page working when the export fails', async () => {
    vi.spyOn(console, 'error').mockImplementation(() => {});
    displayApi.fetchDisplayLedger.mockResolvedValue(LEDGER);
    displayApi.fetchDisplayPaydown.mockResolvedValue([]);
    ledgerExport.exportLedgerToExcel.mockRejectedValue(new Error('disk full'));
    renderWithProviders(<DisplayPage />);

    fireEvent.click(screen.getByRole('button', { name: 'Calculate' }));
    const exportButton = () => screen.getByRole('button', { name: /Export to Excel/ });
    await waitFor(() => expect(exportButton()).toBeEnabled());
    fireEvent.click(exportButton());

    await waitFor(() =>
      expect(console.error).toHaveBeenCalledWith('Excel export error:', expect.any(Error)),
    );
    await waitFor(() => expect(exportButton()).toBeEnabled());
  });

  it('starts on a 90-day range from today', () => {
    renderWithProviders(<DisplayPage />);
    const start = new Date((screen.getByLabelText('Start Date') as HTMLInputElement).value);
    const end = new Date((screen.getByLabelText('End Date') as HTMLInputElement).value);
    expect(Math.round((end.getTime() - start.getTime()) / 86_400_000)).toBe(90);
  });
});
