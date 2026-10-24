import { describe, expect, it } from 'vitest';
import {
  allocatedElsewhere,
  emptyDebtForm,
  formatPercent,
  fromIsoDate,
  newAllocation,
  perOccurrence,
  toDebtForm,
  toDebtRequest,
  toIsoDate,
  validateDebtForm,
  type Debt,
  type DebtForm,
} from './Debt';

const visa: Debt = {
  id: 1,
  itemId: 10,
  name: 'Chase Visa',
  payment: 200,
  period: 'Monthly',
  balance: 1000,
  apr: 24.99,
  balanceDate: '2026-10-17',
  allocations: [
    {
      id: 5,
      creditItemId: 20,
      creditName: 'Paycheck',
      creditAmount: 2000,
      creditPeriod: 'Every Two Weeks',
      percent: 60,
      perOccurrence: 1200,
    },
  ],
};

const names = (id: number) => (id === 20 ? 'Paycheck' : 'Bonus');

function form(changes: Partial<DebtForm> = {}): DebtForm {
  return { ...emptyDebtForm(new Date(2026, 9, 17)), itemId: 11, balance: 500, ...changes };
}

describe('dates', () => {
  it('round-trips yyyy-MM-dd without a timezone shift', () => {
    const date = fromIsoDate('2026-01-01');
    expect(date?.getFullYear()).toBe(2026);
    expect(date?.getMonth()).toBe(0);
    expect(date?.getDate()).toBe(1);
    expect(toIsoDate(date)).toBe('2026-01-01');
    expect(fromIsoDate(null)).toBeNull();
    expect(toIsoDate(null)).toBeNull();
  });
});

describe('form <-> request', () => {
  it('turns a debt into a form and back into the same request', () => {
    expect(toDebtRequest(toDebtForm(visa))).toEqual({
      itemId: 10,
      balance: 1000,
      apr: 24.99,
      balanceDate: '2026-10-17',
      allocations: [{ creditItemId: 20, percent: 60 }],
    });
  });

  it('drops half-filled allocation rows and defaults APR to 0', () => {
    const request = toDebtRequest(
      form({ apr: null, allocations: [newAllocation(20, null), newAllocation(null, 10)] }),
    );
    expect(request.apr).toBe(0);
    expect(request.allocations).toEqual([]);
  });

  it('gives every allocation row its own key', () => {
    expect(newAllocation().key).not.toBe(newAllocation().key);
  });
});

describe('perOccurrence and formatPercent', () => {
  it('rounds the share of a credit to cents', () => {
    expect(perOccurrence(2000, 25)).toBe(500);
    expect(perOccurrence(1234.56, 33.33)).toBe(411.48);
    expect(perOccurrence(null, 25)).toBe(0);
    expect(perOccurrence(2000, null)).toBe(0);
  });

  it('prints percentages without trailing zeros', () => {
    expect(formatPercent(25)).toBe('25%');
    expect(formatPercent(12.5)).toBe('12.5%');
    expect(formatPercent(33.333)).toBe('33.33%');
  });
});

describe('allocatedElsewhere', () => {
  it('sums the other debts and ignores the one being edited', () => {
    const car: Debt = {
      ...visa,
      id: 2,
      itemId: 11,
      allocations: [{ ...visa.allocations[0], id: 6, percent: 15 }],
    };
    expect(allocatedElsewhere([visa, car], null).get(20)).toBe(75);
    expect(allocatedElsewhere([visa, car], 1).get(20)).toBe(15);
  });
});

describe('validateDebtForm (mirrors the API rules)', () => {
  const none = new Map<number, number>();

  it('accepts a complete form', () => {
    expect(
      validateDebtForm(form({ allocations: [newAllocation(20, 25)] }), none, names),
    ).toBeNull();
  });

  it('needs a debit and a balance in range', () => {
    expect(validateDebtForm(form({ itemId: null }), none, names)).toMatch(/Choose the debit/);
    expect(validateDebtForm(form({ balance: null }), none, names)).toMatch(/balance owed/);
    expect(validateDebtForm(form({ balance: -1 }), none, names)).toMatch(/balance owed/);
    expect(validateDebtForm(form({ balance: 1_000_000_001 }), none, names)).toMatch(/balance owed/);
  });

  it('keeps APR between 0 and 100', () => {
    expect(validateDebtForm(form({ apr: 101 }), none, names)).toMatch(/APR/);
    expect(validateDebtForm(form({ apr: -1 }), none, names)).toMatch(/APR/);
  });

  it('needs a credit and a sensible percentage on every row', () => {
    expect(validateDebtForm(form({ allocations: [newAllocation(null, 10)] }), none, names)).toMatch(
      /Choose a credit/,
    );
    expect(validateDebtForm(form({ allocations: [newAllocation(20, 0)] }), none, names)).toMatch(
      /above 0%/,
    );
    expect(
      validateDebtForm(form({ allocations: [newAllocation(20, 100.5)] }), none, names),
    ).toMatch(/above 0%/);
  });

  it('lists a credit only once', () => {
    const rows = [newAllocation(20, 10), newAllocation(20, 5)];
    expect(validateDebtForm(form({ allocations: rows }), none, names)).toBe(
      'Paycheck is listed more than once.',
    );
  });

  it("won't allocate more than 100% of a credit across debts", () => {
    const elsewhere = new Map([[20, 60]]);
    expect(validateDebtForm(form({ allocations: [newAllocation(20, 50)] }), elsewhere, names)).toBe(
      'Paycheck is already 60% allocated to other debts; at most 40% is left.',
    );
    expect(
      validateDebtForm(form({ allocations: [newAllocation(20, 40)] }), elsewhere, names),
    ).toBeNull();
  });
});
