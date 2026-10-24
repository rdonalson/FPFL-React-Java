// src/features/debt-paydown/types/Debt.ts

/** A share of a credit sent to a debt (API's DebtAllocationResponse). */
export interface DebtAllocation {
  id: number;
  creditItemId: number;
  creditName: string | null;
  creditAmount: number | null;
  creditPeriod: string | null;
  percent: number;
  /** Dollars each occurrence of the credit sends. */
  perOccurrence: number;
}

/** A tracked debt (API's DebtResponse): the debit being paid down and what's owed on it. */
export interface Debt {
  id: number;
  itemId: number;
  name: string | null;
  /** The debit's own scheduled payment. */
  payment: number | null;
  period: string | null;
  balance: number;
  apr: number;
  /** 'yyyy-MM-dd': the day `balance` was true. */
  balanceDate: string;
  allocations: DebtAllocation[];
}

/** Body of POST /debts and PUT /debts/{id}. The API takes the owner from the access token. */
export interface DebtRequest {
  itemId: number | null;
  balance: number | null;
  apr: number | null;
  balanceDate: string | null;
  allocations: { creditItemId: number; percent: number }[];
}

export interface AllocationForm {
  /** Stable React key while rows are added and removed. */
  key: number;
  creditItemId: number | null;
  percent: number | null;
}

export interface DebtForm {
  itemId: number | null;
  balance: number | null;
  apr: number | null;
  balanceDate: Date | null;
  allocations: AllocationForm[];
}

/** Same limits as the API's DebtRules. */
export const MAX_BALANCE = 1_000_000_000;
export const MAX_ALLOCATIONS = 20;

let nextKey = 1;
export function newAllocation(
  creditItemId: number | null = null,
  percent: number | null = null,
): AllocationForm {
  return { key: nextKey++, creditItemId, percent };
}

export function emptyDebtForm(today: Date): DebtForm {
  return { itemId: null, balance: null, apr: 0, balanceDate: today, allocations: [] };
}

/** 'yyyy-MM-dd' → local Date (no timezone shift). */
export function fromIsoDate(iso: string | null | undefined): Date | null {
  if (!iso) return null;
  const [y, m, d] = iso.slice(0, 10).split('-').map(Number);
  return new Date(y, m - 1, d);
}

/** Local Date → 'yyyy-MM-dd' (no timezone shift). */
export function toIsoDate(date: Date | null | undefined): string | null {
  if (!date) return null;
  const mm = String(date.getMonth() + 1).padStart(2, '0');
  const dd = String(date.getDate()).padStart(2, '0');
  return `${date.getFullYear()}-${mm}-${dd}`;
}

/** 'yyyy-MM-dd' → "Oct 17, 2026". */
export function formatDate(iso: string | null | undefined): string {
  const date = fromIsoDate(iso);
  return date
    ? date.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })
    : '';
}

export function formatMoney(value: number | null | undefined): string {
  return (value ?? 0).toLocaleString('en-US', { style: 'currency', currency: 'USD' });
}

/** 33.33 → "33.33%", 25 → "25%". */
export function formatPercent(value: number): string {
  return `${Number(value.toFixed(2))}%`;
}

export function toDebtForm(debt: Debt): DebtForm {
  return {
    itemId: debt.itemId,
    balance: debt.balance,
    apr: debt.apr,
    balanceDate: fromIsoDate(debt.balanceDate),
    allocations: debt.allocations.map(a => newAllocation(a.creditItemId, a.percent)),
  };
}

export function toDebtRequest(form: DebtForm): DebtRequest {
  return {
    itemId: form.itemId,
    balance: form.balance,
    apr: form.apr ?? 0,
    balanceDate: toIsoDate(form.balanceDate),
    allocations: form.allocations
      .filter(
        (a): a is AllocationForm & { creditItemId: number; percent: number } =>
          a.creditItemId !== null && a.percent !== null,
      )
      .map(a => ({ creditItemId: a.creditItemId, percent: a.percent })),
  };
}

/** Dollars one occurrence of a credit sends at `percent`, rounded to cents (as the API does). */
export function perOccurrence(
  creditAmount: number | null | undefined,
  percent: number | null | undefined,
): number {
  if (!creditAmount || !percent) return 0;
  return Math.round(creditAmount * percent) / 100;
}

/** How much of each credit the user's *other* debts already take, by credit id. */
export function allocatedElsewhere(
  debts: readonly Debt[],
  editingDebtId: number | null,
): Map<number, number> {
  const taken = new Map<number, number>();
  for (const debt of debts) {
    if (debt.id === editingDebtId) continue;
    for (const a of debt.allocations) {
      taken.set(a.creditItemId, (taken.get(a.creditItemId) ?? 0) + a.percent);
    }
  }
  return taken;
}

/** Null when the form can be saved, otherwise the first problem (mirrors the API's DebtRules). */
export function validateDebtForm(
  form: DebtForm,
  elsewhere: ReadonlyMap<number, number>,
  creditName: (id: number) => string,
): string | null {
  if (form.itemId === null) return 'Choose the debit you are paying down.';
  if (form.balance === null || form.balance < 0 || form.balance > MAX_BALANCE) {
    return 'Enter the balance owed ($0 or more).';
  }
  if (form.apr !== null && (form.apr < 0 || form.apr > 100)) {
    return 'APR must be between 0% and 100%.';
  }
  if (form.allocations.length > MAX_ALLOCATIONS) {
    return `A debt can have at most ${MAX_ALLOCATIONS} allocations.`;
  }

  const seen = new Set<number>();
  for (const a of form.allocations) {
    if (a.creditItemId === null) {
      return 'Choose a credit for each allocation, or remove the empty row.';
    }
    const name = creditName(a.creditItemId);
    if (seen.has(a.creditItemId)) return `${name} is listed more than once.`;
    seen.add(a.creditItemId);

    if (a.percent === null || a.percent <= 0 || a.percent > 100) {
      return `Enter a percentage of ${name} above 0% and up to 100%.`;
    }
    const taken = elsewhere.get(a.creditItemId) ?? 0;
    if (taken + a.percent > 100) {
      return `${name} is already ${formatPercent(taken)} allocated to other debts; at most ${formatPercent(100 - taken)} is left.`;
    }
  }
  return null;
}
