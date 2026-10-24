import { describe, expect, it } from 'vitest';
import { render, screen } from '@testing-library/react';
import { PaydownLedgerPanel } from './PaydownLedgerPanel';
import type { DebtPaydown } from './paydown';

const visa: DebtPaydown = {
  debtId: 1,
  itemId: 10,
  name: 'Chase Visa',
  apr: 12,
  enteredBalance: 1000,
  balanceDate: '2026-10-17',
  startingBalance: 1000,
  endingBalance: 0,
  totalPaid: 1010,
  totalInterest: 10,
  payoffDate: '2026-11-13',
  balances: [],
  events: [
    // Out of order on purpose: the ledger sorts by date
    {
      date: '2026-11-13',
      kind: 'allocation',
      sourceItemId: 20,
      sourceName: 'Paycheck',
      percent: 25,
      amount: -510,
      balanceAfter: 0,
    },
    {
      date: '2026-10-30',
      kind: 'allocation',
      sourceItemId: 20,
      sourceName: 'Paycheck',
      percent: 25,
      amount: -500,
      balanceAfter: 500,
    },
    {
      date: '2026-10-31',
      kind: 'interest',
      sourceItemId: null,
      sourceName: null,
      percent: null,
      amount: 10,
      balanceAfter: 510,
    },
  ],
};

describe('PaydownLedgerPanel', () => {
  it('lists every payment, allocation and interest charge in date order', () => {
    render(<PaydownLedgerPanel debts={[visa]} />);

    expect(screen.getByText('3 entries')).toBeInTheDocument();
    expect(screen.getAllByText('25% of Paycheck')).toHaveLength(2);
    expect(screen.getByText('Interest')).toBeInTheDocument();
    expect(screen.getByText('Paid off')).toBeInTheDocument();
    expect(screen.getByText('+$10.00')).toBeInTheDocument();

    const dates = screen.getAllByText(/^2026-\d\d-\d\d - /).map(cell => cell.textContent);
    expect(dates).toEqual(['2026-10-30 - Fri', '2026-10-31 - Sat', '2026-11-13 - Fri']);
  });

  it('says so when nothing falls in the range', () => {
    render(<PaydownLedgerPanel debts={[{ ...visa, events: [] }]} />);

    expect(screen.getByText('0 entries')).toBeInTheDocument();
    expect(screen.getByText('No payments or interest fall in this range.')).toBeInTheDocument();
  });
});
