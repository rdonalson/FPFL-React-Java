// src/features/accounts/types/Account.ts
import type { Item } from '@/features/catalog-command/transactions/types/Item';
import {
  formatDate,
  formatPercent,
  fromIsoDate,
  toIsoDate,
} from '@/features/debt-paydown/types/Debt';

/** Cards & Loans: shapes and form helpers, ported from the Angular/.NET version (UI-NG). */

export type AccountKind = 'creditCard' | 'loan';

export const CREDIT = 1;
export const DEBIT = 2;

/** Purchase schedules supported for now (same ids as time periods). */
export const ONE_TIME = 1;
export const DAILY = 2;
export const WEEKLY = 3;

/** One of the user's items paying an account (API's AccountPaymentResponse). */
export interface AccountPayment {
  id: number;
  itemId: number;
  /** 1 = credit, 2 = debit. */
  itemTypeId: number | null;
  itemName: string | null;
  itemAmount: number | null;
  itemPeriod: string | null;
  /** A credit's fixed amount, or null. */
  amount: number | null;
  /** A credit's percentage (a debit is stored as 100), or null. */
  percent: number | null;
  /** Dollars each occurrence sends. */
  perOccurrence: number;
}

/** A purchase charged to an account (API's PurchaseResponse). */
export interface Purchase {
  id: number;
  accountId: number;
  name: string;
  amount: number;
  /** 1 = One Time, 2 = Daily, 3 = Weekly. */
  fkPeriod: number;
  period: string | null;
  beginDate: string | null;
  endDate: string | null;
  weeklyDow: number | null;
  dateRangeReq: boolean;
}

/** A credit card or loan (API's AccountResponse). */
export interface Account {
  id: number;
  name: string;
  kind: AccountKind;
  openingBalance: number;
  /** 'yyyy-MM-dd': the day `openingBalance` was true. */
  balanceDate: string;
  apr: number;
  creditLimit: number | null;
  purchases: Purchase[];
  payments: AccountPayment[];
}

/** Body of POST /accounts and PUT /accounts/{id}. The API takes the owner from the access token. */
export interface AccountRequest {
  name: string;
  kind: AccountKind;
  openingBalance: number | null;
  balanceDate: string | null;
  apr: number;
  creditLimit: number | null;
  payments: { itemId: number; amount: number | null; percent: number | null }[];
}

/** Body of POST /accounts/{id}/purchases and PUT /accounts/{id}/purchases/{purchaseId}. */
export interface PurchaseRequest {
  name: string;
  amount: number | null;
  fkPeriod: number;
  beginDate: string | null;
  endDate: string | null;
  weeklyDow: number | null;
  dateRangeReq: boolean;
}

// ---- limits (same as the API's CreditAccountRules / PurchaseRules) ---------------------------

export const MAX_MONEY = 1_000_000_000;
export const MAX_PAYMENTS = 20;
export const MAX_NAME = 150;

export const KIND_OPTIONS: readonly { label: string; value: AccountKind; icon: string }[] = [
  { label: 'Credit card', value: 'creditCard', icon: 'pi pi-credit-card' },
  { label: 'Loan', value: 'loan', icon: 'pi pi-building-columns' },
];

export const PURCHASE_PERIODS: readonly { label: string; value: number }[] = [
  { label: 'One Time', value: ONE_TIME },
  { label: 'Daily', value: DAILY },
  { label: 'Weekly', value: WEEKLY },
];

export const WEEKDAYS = [
  'Sunday',
  'Monday',
  'Tuesday',
  'Wednesday',
  'Thursday',
  'Friday',
  'Saturday',
] as const;

export function kindLabel(kind: AccountKind): string {
  return kind === 'loan' ? 'Loan' : 'Credit card';
}

export function kindIcon(kind: AccountKind): string {
  return kind === 'loan' ? 'pi pi-building-columns' : 'pi pi-credit-card';
}

export function itemTypeOf(item: Item | null | undefined): number | null {
  return item?.ItemType?.Id ?? null;
}

// ---- account form ----------------------------------------------------------------------------

/** How a payment row sends money: a credit's fixed amount or percentage, or a debit's whole amount. */
export type PaymentMode = 'amount' | 'percent' | 'whole';

export interface PaymentForm {
  /** Stable React key while rows are added and removed. */
  key: number;
  itemId: number | null;
  mode: PaymentMode;
  amount: number | null;
  percent: number | null;
}

export interface AccountForm {
  name: string;
  kind: AccountKind;
  openingBalance: number | null;
  balanceDate: Date | null;
  apr: number | null;
  creditLimit: number | null;
  payments: PaymentForm[];
}

let nextKey = 1;
export function newPayment(changes: Partial<Omit<PaymentForm, 'key'>> = {}): PaymentForm {
  return { key: nextKey++, itemId: null, mode: 'percent', amount: null, percent: null, ...changes };
}

export function emptyAccountForm(today: Date): AccountForm {
  return {
    name: '',
    kind: 'creditCard',
    openingBalance: null,
    balanceDate: today,
    apr: 0,
    creditLimit: null,
    payments: [],
  };
}

export function toAccountForm(account: Account): AccountForm {
  return {
    name: account.name,
    kind: account.kind,
    openingBalance: account.openingBalance,
    balanceDate: fromIsoDate(account.balanceDate),
    apr: account.apr,
    creditLimit: account.creditLimit,
    payments: account.payments.map(p =>
      newPayment({
        itemId: p.itemId,
        mode: p.itemTypeId === DEBIT ? 'whole' : p.amount !== null ? 'amount' : 'percent',
        amount: p.amount,
        percent: p.itemTypeId === DEBIT ? null : p.percent,
      }),
    ),
  };
}

export function toAccountRequest(form: AccountForm): AccountRequest {
  return {
    name: form.name.trim(),
    kind: form.kind,
    openingBalance: form.openingBalance,
    balanceDate: toIsoDate(form.balanceDate),
    apr: form.apr ?? 0,
    creditLimit: form.kind === 'creditCard' ? form.creditLimit : null,
    payments: form.payments
      .filter((p): p is PaymentForm & { itemId: number } => p.itemId !== null)
      .map(p => ({
        itemId: p.itemId,
        amount: p.mode === 'amount' ? p.amount : null,
        percent: p.mode === 'percent' ? p.percent : null,
      })),
  };
}

/** Dollars one occurrence of a payment row sends, rounded to cents (as the API does). */
export function paymentShare(payment: PaymentForm, item: Item | null | undefined): number {
  if (!item) return 0;
  switch (payment.mode) {
    case 'whole':
      return item.amount;
    case 'amount':
      return payment.amount ?? 0;
    default:
      return payment.percent ? Math.round(item.amount * payment.percent) / 100 : 0;
  }
}

/** How much of each item the user's *other* accounts already take as a percentage, by item id. */
export function takenElsewhere(
  accounts: readonly Account[],
  editingId: number | null,
): Map<number, number> {
  const taken = new Map<number, number>();
  for (const account of accounts) {
    if (account.id === editingId) continue;
    for (const payment of account.payments) {
      if (payment.percent !== null) {
        taken.set(payment.itemId, (taken.get(payment.itemId) ?? 0) + payment.percent);
      }
    }
  }
  return taken;
}

/** Null when the form can be saved, otherwise the first problem (mirrors the API's CreditAccountRules). */
export function validateAccountForm(
  form: AccountForm,
  accounts: readonly Account[],
  editingId: number | null,
  item: (id: number) => Item | undefined,
): string | null {
  const name = form.name.trim();
  if (!name) return 'Enter a name for the card or loan.';
  if (name.length > MAX_NAME) return `The name must be at most ${MAX_NAME} characters.`;
  if (accounts.some(a => a.id !== editingId && a.name.toLowerCase() === name.toLowerCase())) {
    return `You already have an account named "${name}".`;
  }
  if (form.openingBalance === null || form.openingBalance < 0 || form.openingBalance > MAX_MONEY) {
    return 'Enter the balance owed ($0 or more).';
  }
  if (form.apr !== null && (form.apr < 0 || form.apr > 100)) {
    return 'APR must be between 0% and 100%.';
  }
  if (
    form.kind === 'creditCard' &&
    form.creditLimit !== null &&
    (form.creditLimit <= 0 || form.creditLimit > MAX_MONEY)
  ) {
    return 'The credit limit must be more than $0.';
  }
  if (form.payments.length > MAX_PAYMENTS) {
    return `An account can have at most ${MAX_PAYMENTS} payments.`;
  }

  const elsewhere = takenElsewhere(accounts, editingId);
  const seen = new Set<number>();
  for (const payment of form.payments) {
    if (payment.itemId === null) {
      return 'Choose a credit or debit for each payment, or remove the empty row.';
    }
    const source = item(payment.itemId);
    const label = source?.name ?? 'That item';
    if (seen.has(payment.itemId)) return `${label} is listed more than once.`;
    seen.add(payment.itemId);

    const taken = elsewhere.get(payment.itemId) ?? 0;
    if (payment.mode === 'whole') {
      if (taken > 0) return `${label} already pays another account.`;
      continue;
    }
    if (payment.mode === 'amount') {
      if (
        payment.amount === null ||
        payment.amount <= 0 ||
        (source && payment.amount > source.amount)
      ) {
        return `Enter an amount from ${label} above $0 and up to its ${source ? `$${source.amount.toFixed(2)}` : 'amount'}.`;
      }
      continue;
    }
    if (payment.percent === null || payment.percent <= 0 || payment.percent > 100) {
      return `Enter a percentage of ${label} above 0% and up to 100%.`;
    }
    if (taken + payment.percent > 100) {
      return `${label} already sends ${formatPercent(taken)} to other accounts; at most ${formatPercent(100 - taken)} is left.`;
    }
  }
  return null;
}

// ---- purchase form ---------------------------------------------------------------------------

export interface PurchaseForm {
  name: string;
  amount: number | null;
  fkPeriod: number;
  beginDate: Date | null;
  endDate: Date | null;
  weeklyDow: number | null;
  dateRangeReq: boolean;
}

export function emptyPurchaseForm(today: Date): PurchaseForm {
  return {
    name: '',
    amount: null,
    fkPeriod: ONE_TIME,
    beginDate: today,
    endDate: null,
    weeklyDow: null,
    dateRangeReq: false,
  };
}

export function toPurchaseForm(purchase: Purchase): PurchaseForm {
  return {
    name: purchase.name,
    amount: purchase.amount,
    fkPeriod: purchase.fkPeriod,
    beginDate: fromIsoDate(purchase.beginDate),
    endDate: fromIsoDate(purchase.endDate),
    weeklyDow: purchase.weeklyDow,
    dateRangeReq: purchase.fkPeriod !== ONE_TIME && purchase.dateRangeReq,
  };
}

/** Only the fields belonging to the chosen period are sent (the API clears the rest anyway). */
export function toPurchaseRequest(form: PurchaseForm): PurchaseRequest {
  const oneTime = form.fkPeriod === ONE_TIME;
  const ranged = !oneTime && form.dateRangeReq;
  return {
    name: form.name.trim(),
    amount: form.amount,
    fkPeriod: form.fkPeriod,
    beginDate: oneTime || ranged ? toIsoDate(form.beginDate) : null,
    endDate: ranged ? toIsoDate(form.endDate) : null,
    weeklyDow: form.fkPeriod === WEEKLY ? form.weeklyDow : null,
    dateRangeReq: oneTime || ranged,
  };
}

/** Null when the purchase can be saved, otherwise the first problem (mirrors the API's PurchaseRules). */
export function validatePurchaseForm(form: PurchaseForm): string | null {
  const name = form.name.trim();
  if (!name) return 'Enter what the purchase is for.';
  if (name.length > MAX_NAME) return `The name must be at most ${MAX_NAME} characters.`;
  if (form.amount === null || form.amount <= 0 || form.amount > MAX_MONEY) {
    return 'Enter an amount above $0.';
  }
  if (form.fkPeriod === ONE_TIME) {
    return form.beginDate ? null : 'Choose the date of the purchase.';
  }
  if (form.fkPeriod === WEEKLY && form.weeklyDow === null) return 'Choose the day of the week.';
  if (form.dateRangeReq) {
    if (!form.beginDate || !form.endDate) return 'Choose both the first and last dates.';
    if (form.beginDate > form.endDate) return 'The first date must be on or before the last date.';
  }
  return null;
}

/** "Once, on Nov 5, 2026", "Every day", "Every Friday", plus the date range when there is one. */
export function describePurchase(
  purchase: Pick<Purchase, 'fkPeriod' | 'beginDate' | 'endDate' | 'weeklyDow' | 'dateRangeReq'>,
): string {
  switch (purchase.fkPeriod) {
    case ONE_TIME:
      return purchase.beginDate ? `Once, on ${formatDate(purchase.beginDate)}` : 'Once';
    case DAILY:
      return `Every day${range(purchase)}`;
    case WEEKLY:
      return `${purchase.weeklyDow !== null ? `Every ${WEEKDAYS[purchase.weeklyDow]}` : 'Weekly'}${range(purchase)}`;
    default:
      return '';
  }
}

function range(purchase: Pick<Purchase, 'beginDate' | 'endDate' | 'dateRangeReq'>): string {
  if (!purchase.dateRangeReq || !purchase.beginDate) return '';
  return purchase.endDate
    ? `, ${formatDate(purchase.beginDate)} – ${formatDate(purchase.endDate)}`
    : `, from ${formatDate(purchase.beginDate)}`;
}

/** "25% of Paycheck · $500.00 each", "$200.00 from Paycheck", "Visa payment (whole amount)". */
export function describePayment(payment: AccountPayment): string {
  const name = payment.itemName ?? 'an item';
  if (payment.itemTypeId === DEBIT) return `${name} (its whole payment)`;
  if (payment.amount !== null) return `${name}: a fixed amount`;
  return payment.percent !== null ? `${formatPercent(payment.percent)} of ${name}` : name;
}
