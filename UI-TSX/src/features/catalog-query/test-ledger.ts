// Sample ledger rows shared by the catalog-query tests
import type { LedgerDto } from './types/DispayResponse';
import type { DebtPaydown } from './paydown/paydown';

export const LEDGER: LedgerDto[] = [
  {
    rollupKey: 1,
    year: 2026,
    wDate: '2026-10-30',
    creditSummary: 2000,
    debitSummary: -500,
    net: 1500,
    runningTotal: 1600,
    items: [
      {
        itemKey: 1,
        fkItemType: 1,
        itemType: 'Credit',
        name: 'Paycheck',
        amount: 2000,
        period: 'Every Two Weeks',
        occurrenceDate: '2026-10-30',
        rollupKey: null,
        year: null,
      },
      {
        itemKey: 2,
        fkItemType: 2,
        itemType: 'Debt Paydown',
        name: 'Chase Visa paydown',
        amount: -500,
        period: '25% of Paycheck',
        occurrenceDate: '2026-10-30',
        rollupKey: null,
        year: null,
      },
    ],
  },
  {
    rollupKey: 2,
    year: 2026,
    wDate: '2026-10-31',
    creditSummary: 0,
    debitSummary: 0,
    net: 0,
    runningTotal: 1600,
    items: [],
  },
];

export const PAYDOWN: DebtPaydown[] = [
  {
    debtId: 1,
    itemId: 10,
    name: 'Chase Visa',
    apr: 0,
    enteredBalance: 1000,
    balanceDate: '2026-10-17',
    startingBalance: 1000,
    endingBalance: 500,
    totalPaid: 500,
    totalInterest: 0,
    payoffDate: '2026-11-13',
    balances: [500, 500],
    events: [
      {
        date: '2026-10-30',
        kind: 'allocation',
        sourceItemId: 20,
        sourceName: 'Paycheck',
        percent: 25,
        amount: -500,
        balanceAfter: 500,
      },
    ],
  },
];
