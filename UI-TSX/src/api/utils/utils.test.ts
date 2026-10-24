import { afterEach, describe, expect, it, vi } from 'vitest';
import { todayLocalIsoDate } from './currentDate';
import { normalizeAuthResponse } from './normalizeAuthResponse';
import { getSessionUserId } from './userId';
import { useSessionStore } from '@/app/state/sessionStore';

afterEach(() => {
  vi.useRealTimers();
  useSessionStore.setState({ userId: null });
  sessionStorage.clear();
});

describe('todayLocalIsoDate', () => {
  it('formats the local date as yyyy-MM-dd with zero padding', () => {
    vi.useFakeTimers();
    vi.setSystemTime(new Date(2026, 0, 5, 23, 59));
    expect(todayLocalIsoDate()).toBe('2026-01-05');
  });
});

describe('normalizeAuthResponse', () => {
  it('picks the session fields and keeps the raw response', () => {
    const raw = {
      accessToken: 'a',
      refreshToken: 'r',
      id: 3,
      userId: 'u-1',
      email: 'e@x.com',
      first: 'Rick',
      last: 'D',
      roles: ['ROLE_USER'],
      extra: true,
    };
    const n = normalizeAuthResponse(raw);
    expect(n).toMatchObject({
      accessToken: 'a',
      refreshToken: 'r',
      id: 3,
      userId: 'u-1',
      roles: ['ROLE_USER'],
    });
    expect(n.raw).toBe(raw);
    expect(n).not.toHaveProperty('extra');
  });
});

describe('getSessionUserId (api/utils)', () => {
  it('prefers the session store, then sessionStorage, then empty', () => {
    expect(getSessionUserId()).toBe('');

    sessionStorage.setItem('userId', 'from-storage');
    expect(getSessionUserId()).toBe('from-storage');

    useSessionStore.setState({ userId: 'from-store' });
    expect(getSessionUserId()).toBe('from-store');
  });
});
