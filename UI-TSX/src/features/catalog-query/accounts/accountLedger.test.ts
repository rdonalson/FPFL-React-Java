import { describe, expect, it } from 'vitest';
import {
  accountColor,
  accountPayoffStatus,
  describeAccountEvent,
  utilization,
  type AccountEvent,
  type AccountLedger,
} from './accountLedger';

const event = (changes: Partial<AccountEvent>): AccountEvent => ({
  date: '2026-11-13',
  kind: 'payment',
  sourceId: 20,
  sourceName: 'Paycheck',
  percent: null,
  amount: -500,
  balanceAfter: 500,
  ...changes,
});

const visa: AccountLedger = {
  accountId: 1,
  name: 'Visa',
  kind: 'creditCard',
  apr: 0,
  creditLimit: 4000,
  openingBalance: 1000,
  balanceDate: '2026-10-17',
  startingBalance: 1000,
  endingBalance: 1000,
  totalPurchases: 0,
  totalPaid: 0,
  totalInterest: 0,
  payoffDate: null,
  balances: [],
  events: [],
};

describe('account ledger helpers', () => {
  it('describes each kind of event', () => {
    expect(describeAccountEvent(event({ percent: 25 }))).toBe('25% of Paycheck');
    expect(describeAccountEvent(event({}))).toBe('Paid from Paycheck');
    expect(describeAccountEvent(event({ kind: 'purchase', sourceName: 'Groceries' }))).toBe(
      'Groceries',
    );
    expect(describeAccountEvent(event({ kind: 'interest', sourceName: null }))).toBe('Interest');
  });

  it('places the payoff inside, after or beyond the window', () => {
    expect(accountPayoffStatus({ ...visa, payoffDate: '2026-11-13' }, '2026-12-31')).toBe('paid');
    expect(accountPayoffStatus({ ...visa, payoffDate: '2027-01-15' }, '2026-12-31')).toBe('later');
    expect(accountPayoffStatus(visa, '2026-12-31')).toBe('never');
  });

  it('shows limit use only for a card with a limit, and cycles colors', () => {
    expect(utilization(visa)).toBe(25);
    expect(utilization({ ...visa, creditLimit: null })).toBeNull();
    expect(accountColor(6)).toBe(accountColor(0));
  });
});
