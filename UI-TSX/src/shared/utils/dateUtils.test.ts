import { describe, expect, it } from 'vitest';
import { parseDateOnlyString, toDateOnlyString } from './dateUtils';

describe('dateUtils', () => {
  it('parses yyyy-MM-dd as a local date (no UTC shift)', () => {
    const d = parseDateOnlyString('2026-03-01');
    expect(d?.getFullYear()).toBe(2026);
    expect(d?.getMonth()).toBe(2);
    expect(d?.getDate()).toBe(1);
    expect(d?.getHours()).toBe(0);
  });

  it('rejects empty or malformed strings', () => {
    expect(parseDateOnlyString(null)).toBeNull();
    expect(parseDateOnlyString('')).toBeNull();
    expect(parseDateOnlyString('not-a-date')).toBeNull();
  });

  it('formats a date back to yyyy-MM-dd', () => {
    expect(toDateOnlyString(new Date(2026, 11, 9, 23, 30))).toBe('2026-12-09');
    expect(toDateOnlyString(null)).toBeNull();
  });

  it('round-trips', () => {
    expect(toDateOnlyString(parseDateOnlyString('2024-02-29'))).toBe('2024-02-29');
  });
});
