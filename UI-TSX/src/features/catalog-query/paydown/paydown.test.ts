import { describe, expect, it } from 'vitest';
import {
  PAYDOWN_COLORS,
  describeEvent,
  paydownColor,
  payoffStatus,
  type DebtPaydown,
  type PaydownEvent,
} from './paydown';

const event = (changes: Partial<PaydownEvent>): PaydownEvent => ({
  date: '2026-10-30',
  kind: 'payment',
  sourceItemId: 10,
  sourceName: 'Chase Visa',
  percent: null,
  amount: -200,
  balanceAfter: 800,
  ...changes,
});

const debt = (payoffDate: string | null): DebtPaydown => ({
  debtId: 1,
  itemId: 10,
  name: 'Chase Visa',
  apr: 0,
  enteredBalance: 1000,
  balanceDate: '2026-10-17',
  startingBalance: 1000,
  endingBalance: 0,
  totalPaid: 1000,
  totalInterest: 0,
  payoffDate,
  balances: [],
  events: [],
});

describe('describeEvent', () => {
  it('names each kind of balance change', () => {
    expect(describeEvent(event({ kind: 'payment' }))).toBe('Scheduled payment');
    expect(describeEvent(event({ kind: 'interest', sourceName: null, amount: 12 }))).toBe(
      'Interest',
    );
    expect(describeEvent(event({ kind: 'allocation', percent: 25, sourceName: 'Paycheck' }))).toBe(
      '25% of Paycheck',
    );
    expect(describeEvent(event({ kind: 'allocation', percent: 12.5, sourceName: 'Bonus' }))).toBe(
      '12.5% of Bonus',
    );
    expect(describeEvent(event({ kind: 'allocation', percent: null, sourceName: null }))).toBe(
      'a credit',
    );
  });
});

describe('payoffStatus', () => {
  it('says whether the payoff falls in the range, after it, or never', () => {
    expect(payoffStatus(debt('2026-11-13'), '2026-12-31')).toBe('paid');
    expect(payoffStatus(debt('2026-12-31'), '2026-12-31')).toBe('paid');
    expect(payoffStatus(debt('2028-01-15'), '2026-12-31')).toBe('later');
    expect(payoffStatus(debt(null), '2026-12-31')).toBe('never');
  });
});

describe('paydownColor', () => {
  it('keeps a fixed color per debt and wraps around', () => {
    expect(paydownColor(0)).toBe(PAYDOWN_COLORS[0]);
    expect(paydownColor(PAYDOWN_COLORS.length)).toBe(PAYDOWN_COLORS[0]);
    expect(new Set(PAYDOWN_COLORS).size).toBe(PAYDOWN_COLORS.length);
  });
});
