// src/shared/utils/dateUtils.ts

/**
 * Parse a YYYY-MM-DD string as a *local* date (no UTC shift).
 */
function parseDateOnlyString(s: string | null | undefined): Date | null {
  if (!s) return null;
  const [yearStr, monthStr, dayStr] = s.split('-');
  const year = Number(yearStr);
  const month = Number(monthStr);
  const day = Number(dayStr);
  if (Number.isNaN(year) || Number.isNaN(month) || Number.isNaN(day)) return null;
  return new Date(year, month - 1, day);
}

// Convert Date → "YYYY-MM-DD" (local, no timezone)
function toDateOnlyString(d: Date | null): string | null {
  if (!d) return null;
  const year = d.getFullYear();
  const month = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}

export { parseDateOnlyString, toDateOnlyString };
