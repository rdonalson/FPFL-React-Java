// src/features/catalog-query/accounts/accountLedger.ts

/** One change to a card or loan's balance (API's AccountEventResponse). Purchases and interest are positive, payments negative. */
export interface AccountEvent {
  /** 'yyyy-MM-dd' */
  date: string;
  kind: 'purchase' | 'payment' | 'interest';
  /** The purchase id (purchases) or the paying credit/debit item id (payments). */
  sourceId: number | null;
  sourceName: string | null;
  percent: number | null;
  amount: number;
  balanceAfter: number;
}

/** A card or loan over the forecast window (API's AccountLedgerResponse). */
export interface AccountLedger {
  accountId: number;
  name: string;
  kind: 'creditCard' | 'loan';
  apr: number;
  creditLimit: number | null;
  openingBalance: number;
  balanceDate: string;
  startingBalance: number;
  endingBalance: number;
  totalPurchases: number;
  totalPaid: number;
  totalInterest: number;
  /** May fall after the window; null if not cleared within 30 years. */
  payoffDate: string | null;
  /** Owed at the end of each day of the window. */
  balances: number[];
  events: AccountEvent[];
}

/**
 * One color per account, in a fixed order, so an account keeps its color across the chart, the ledger
 * and the summary cards. Starts on a different hue than Debt Paydown's palette.
 */
export const ACCOUNT_COLORS = [
  '#0d9488',
  '#2563eb',
  '#d97706',
  '#7c3aed',
  '#db2777',
  '#65a30d',
] as const;

export function accountColor(index: number): string {
  return ACCOUNT_COLORS[index % ACCOUNT_COLORS.length];
}

/** "Groceries", "25% of Paycheck", "Paid from Paycheck", "Interest". */
export function describeAccountEvent(event: AccountEvent): string {
  switch (event.kind) {
    case 'purchase':
      return event.sourceName ?? 'Purchase';
    case 'payment':
      return event.percent !== null
        ? `${Number(event.percent.toFixed(2))}% of ${event.sourceName ?? 'a credit'}`
        : `Paid from ${event.sourceName ?? 'an item'}`;
    default:
      return 'Interest';
  }
}

/** Is the payoff inside the window, after it, or not in sight? */
export function accountPayoffStatus(
  account: AccountLedger,
  windowEnd: string,
): 'paid' | 'later' | 'never' {
  if (account.payoffDate === null) return 'never';
  return account.payoffDate <= windowEnd ? 'paid' : 'later';
}

/** Share of the credit limit in use at the end of the window, rounded, or null without a limit. */
export function utilization(account: AccountLedger): number | null {
  return account.creditLimit
    ? Math.round((account.endingBalance / account.creditLimit) * 100)
    : null;
}
