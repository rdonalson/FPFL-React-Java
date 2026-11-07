import { describe, expect, it } from 'vitest';
import type { Item } from '@/features/catalog-command/transactions/types/Item';
import {
  DAILY,
  ONE_TIME,
  WEEKLY,
  describePurchase,
  emptyAccountForm,
  emptyPurchaseForm,
  newPayment,
  paymentShare,
  toAccountForm,
  toAccountRequest,
  toPurchaseRequest,
  validateAccountForm,
  validatePurchaseForm,
  type Account,
} from './Account';

const item = (id: number, name: string, type: number, amount: number): Item => ({
  id,
  userId: 'user-1',
  name,
  amount,
  ItemType: { Id: type },
  TimePeriod: { Id: 6, Name: 'Monthly' },
  dateRangeReq: false,
});

const paycheck = item(20, 'Paycheck', 1, 2000);
const visaPayment = item(10, 'Visa payment', 2, 200);
const items = new Map([
  [20, paycheck],
  [10, visaPayment],
]);
const lookup = (id: number) => items.get(id);

const carLoan: Account = {
  id: 2,
  name: 'Car Loan',
  kind: 'loan',
  openingBalance: 8000,
  balanceDate: '2026-11-01',
  apr: 6,
  creditLimit: null,
  purchases: [],
  payments: [
    {
      id: 1,
      itemId: 20,
      itemTypeId: 1,
      itemName: 'Paycheck',
      itemAmount: 2000,
      itemPeriod: 'Monthly',
      amount: null,
      percent: 80,
      perOccurrence: 1600,
    },
  ],
};

const form = (changes = {}) => ({
  ...emptyAccountForm(new Date(2026, 10, 5)),
  name: 'Visa',
  openingBalance: 1000,
  ...changes,
});

describe('account form', () => {
  it('sends each payment the way its row says, and no limit for a loan', () => {
    const request = toAccountRequest(
      form({
        kind: 'loan',
        creditLimit: 500,
        payments: [
          newPayment({ itemId: 20, mode: 'amount', amount: 300, percent: 25 }),
          newPayment({ itemId: 10, mode: 'whole' }),
          newPayment(), // an empty row is dropped
        ],
      }),
    );

    expect(request.creditLimit).toBeNull();
    expect(request.balanceDate).toBe('2026-11-05');
    expect(request.payments).toEqual([
      { itemId: 20, amount: 300, percent: null },
      { itemId: 10, amount: null, percent: null },
    ]);
  });

  it('reads a saved debit payment back as its whole amount', () => {
    const edited = toAccountForm({
      ...carLoan,
      payments: [
        {
          ...carLoan.payments[0],
          itemId: 10,
          itemTypeId: 2,
          itemName: 'Visa payment',
          percent: 100,
        },
      ],
    });
    expect(edited.payments[0]).toMatchObject({ itemId: 10, mode: 'whole', percent: null });
  });

  it('works out what one occurrence sends', () => {
    expect(paymentShare(newPayment({ itemId: 20, percent: 25 }), paycheck)).toBe(500);
    expect(paymentShare(newPayment({ itemId: 20, mode: 'amount', amount: 300 }), paycheck)).toBe(
      300,
    );
    expect(paymentShare(newPayment({ itemId: 10, mode: 'whole' }), visaPayment)).toBe(200);
  });

  it('mirrors the API rules', () => {
    expect(validateAccountForm(form(), [carLoan], null, lookup)).toBeNull();
    expect(validateAccountForm(form({ name: ' car loan ' }), [carLoan], null, lookup)).toMatch(
      /already have an account/,
    );
    expect(validateAccountForm(form({ openingBalance: null }), [], null, lookup)).toMatch(
      /balance owed/,
    );
    expect(
      validateAccountForm(
        form({ payments: [newPayment({ itemId: 20, percent: 30 })] }),
        [carLoan],
        null,
        lookup,
      ),
    ).toMatch(/already sends 80% to other accounts; at most 20% is left/);
    expect(
      validateAccountForm(
        form({ payments: [newPayment({ itemId: 20, mode: 'amount', amount: 2500 })] }),
        [],
        null,
        lookup,
      ),
    ).toMatch(/up to its \$2000.00/);
    // Editing the loan itself doesn't count its own share
    expect(
      validateAccountForm(
        { ...toAccountForm(carLoan), name: 'Car Loan' },
        [carLoan],
        carLoan.id,
        lookup,
      ),
    ).toBeNull();
  });
});

describe('purchase form', () => {
  const today = new Date(2026, 10, 5);

  it('keeps only the fields of the chosen schedule', () => {
    expect(toPurchaseRequest({ ...emptyPurchaseForm(today), name: ' TV ', amount: 500 })).toEqual({
      name: 'TV',
      amount: 500,
      fkPeriod: ONE_TIME,
      beginDate: '2026-11-05',
      endDate: null,
      weeklyDow: null,
      dateRangeReq: true,
    });

    const weekly = toPurchaseRequest({
      ...emptyPurchaseForm(today),
      name: 'Gas',
      amount: 40,
      fkPeriod: WEEKLY,
      weeklyDow: 5,
    });
    expect(weekly).toMatchObject({ beginDate: null, weeklyDow: 5, dateRangeReq: false });
  });

  it('mirrors the API rules', () => {
    const base = { ...emptyPurchaseForm(today), name: 'Gas', amount: 40 };
    expect(validatePurchaseForm(base)).toBeNull();
    expect(validatePurchaseForm({ ...base, beginDate: null })).toMatch(/date of the purchase/);
    expect(validatePurchaseForm({ ...base, fkPeriod: WEEKLY })).toMatch(/day of the week/);
    expect(
      validatePurchaseForm({ ...base, fkPeriod: DAILY, dateRangeReq: true, endDate: null }),
    ).toMatch(/both the first and last/);
  });

  it('describes the schedule', () => {
    const plain = { beginDate: null, endDate: null, weeklyDow: null, dateRangeReq: false };
    expect(describePurchase({ ...plain, fkPeriod: ONE_TIME, beginDate: '2026-11-05' })).toBe(
      'Once, on Nov 5, 2026',
    );
    expect(describePurchase({ ...plain, fkPeriod: WEEKLY, weeklyDow: 5 })).toBe('Every Friday');
    expect(
      describePurchase({
        fkPeriod: DAILY,
        beginDate: '2026-11-01',
        endDate: '2026-11-30',
        weeklyDow: null,
        dateRangeReq: true,
      }),
    ).toBe('Every day, Nov 1, 2026 – Nov 30, 2026');
  });
});
