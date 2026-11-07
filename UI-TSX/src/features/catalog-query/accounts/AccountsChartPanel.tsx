// src/features/catalog-query/accounts/AccountsChartPanel.tsx
import type { CSSProperties } from 'react';
import { Chart } from 'primereact/chart';
import { useIsSmallScreen } from '@/shared/hooks/useIsSmallScreen';
import { formatDate, formatMoney } from '@/features/debt-paydown/types/Debt';
import type { LedgerDto } from '../types/DispayResponse';
import {
  accountColor,
  accountPayoffStatus,
  utilization,
  type AccountLedger,
} from './accountLedger';

interface AccountsChartPanelProps {
  accounts: AccountLedger[];
  ledger: LedgerDto[] | null;
}

const compactMoney = new Intl.NumberFormat('en-US', {
  style: 'currency',
  currency: 'USD',
  notation: 'compact',
  maximumFractionDigits: 1,
});

/**
 * The Cards & loans chart tab: a card per account (owed, purchases, paid, interest, payoff) and every
 * account's balance over time.
 */
export function AccountsChartPanel({ accounts, ledger }: AccountsChartPanelProps) {
  const isSm = useIsSmallScreen();
  const days = ledger ?? [];
  const windowEnd = days.length > 0 ? days[days.length - 1].wDate.slice(0, 10) : '';

  const data = {
    labels: days.map(row => row.wDate.slice(0, 10)),
    datasets: accounts.map((account, i) => ({
      label: account.name,
      data: account.balances,
      borderColor: accountColor(i),
      backgroundColor: accountColor(i),
      borderWidth: 2,
      pointRadius: 0,
      pointHoverRadius: 5,
      stepped: 'after' as const, // balances change on purchase and payment days and stay flat between them
      fill: false,
    })),
  };

  const options = {
    responsive: true,
    maintainAspectRatio: false,
    animation: false,
    interaction: { mode: 'index', intersect: false },
    plugins: {
      legend: {
        display: accounts.length > 1,
        position: 'top',
        labels: isSm ? { boxWidth: 12, font: { size: 10 } } : { boxWidth: 12 },
      },
      tooltip: {
        callbacks: {
          label: (item: any) => ` ${item.dataset.label}: ${formatMoney(Number(item.raw))} owed`,
        },
      },
    },
    scales: {
      x: {
        grid: { display: false },
        ticks: { color: '#666', autoSkip: true, maxTicksLimit: isSm ? 5 : 8, maxRotation: 0 },
      },
      y: {
        beginAtZero: true,
        grid: { color: '#ddd' },
        ticks: {
          color: '#666',
          maxTicksLimit: 6,
          callback: (value: number | string) => compactMoney.format(Number(value)),
        },
      },
    },
  };

  return (
    <div className="app-panel p-4">
      <h3 className="text-lg font-semibold mb-4">Cards &amp; Loans</h3>

      <div className="paydown-cards">
        {accounts.map((account, i) => {
          const status = accountPayoffStatus(account, windowEnd);
          const used = utilization(account);
          return (
            <article
              key={account.accountId}
              className="paydown-card"
              aria-label={account.name}
              style={{ '--debt-color': accountColor(i) } as CSSProperties}
            >
              <h4 className="paydown-card__name">
                <span className="paydown-card__swatch" aria-hidden="true" />
                {account.name}
              </h4>
              <dl className="paydown-card__figures">
                <div>
                  <dt>Owed</dt>
                  <dd>
                    {formatMoney(account.startingBalance)} →{' '}
                    <strong>{formatMoney(account.endingBalance)}</strong>
                  </dd>
                </div>
                {account.totalPurchases > 0 && (
                  <div>
                    <dt>Purchases</dt>
                    <dd className="paydown-card__interest">
                      {formatMoney(account.totalPurchases)}
                    </dd>
                  </div>
                )}
                <div>
                  <dt>Paid</dt>
                  <dd className="paydown-card__paid">{formatMoney(account.totalPaid)}</dd>
                </div>
                {account.totalInterest > 0 && (
                  <div>
                    <dt>Interest</dt>
                    <dd className="paydown-card__interest">{formatMoney(account.totalInterest)}</dd>
                  </div>
                )}
                {used !== null && (
                  <div>
                    <dt>Limit used</dt>
                    <dd>{used}%</dd>
                  </div>
                )}
              </dl>
              <p
                className={`paydown-card__payoff ${status === 'never' ? 'paydown-card__payoff--never' : ''}`}
              >
                {status === 'paid' && (
                  <>
                    <i className="pi pi-check-circle" aria-hidden="true" /> Paid off{' '}
                    {formatDate(account.payoffDate)}
                  </>
                )}
                {status === 'later' && (
                  <>
                    <i className="pi pi-calendar" aria-hidden="true" /> Paid off{' '}
                    {formatDate(account.payoffDate)} (after this range)
                  </>
                )}
                {status === 'never' && (
                  <>
                    <i className="pi pi-exclamation-triangle" aria-hidden="true" /> Not paid off
                    within 30 years at this rate
                  </>
                )}
              </p>
            </article>
          );
        })}
      </div>

      <div
        style={{ height: '420px', minHeight: '320px', display: 'flex', flexDirection: 'column' }}
      >
        <Chart
          type="line"
          data={data}
          options={options}
          aria-label="Balance owed on each card or loan by day"
        />
      </div>

      <p className="text-sm text-color-secondary mt-3 mb-0">
        Hover the chart for each day&apos;s balances. The Cards &amp; loans ledger tab lists every
        purchase and payment.
      </p>
    </div>
  );
}
