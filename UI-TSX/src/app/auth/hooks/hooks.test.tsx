import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { act, renderHook, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import type { ReactNode } from 'react';

const api = vi.hoisted(() => ({ login: vi.fn(), refresh: vi.fn(), register: vi.fn() }));
vi.mock('@/app/auth/api/authApi', () => ({ authApi: api }));
const navigate = vi.hoisted(() => vi.fn());
vi.mock('react-router-dom', async importOriginal => ({
  ...(await importOriginal<typeof import('react-router-dom')>()),
  useNavigate: () => navigate,
}));

import { useAuth } from './useAuth';
import { DEMO_EMAIL, useGuestLogin } from './useGuestLogin';
import { useTokenWatcher } from './useTokenWatcher';
import { useSessionStore } from '@/app/state/sessionStore';

const session = {
  accessToken: 'access',
  refreshToken: 'refresh',
  id: 7,
  userId: 'user-7',
  email: 'rick@example.com',
  first: 'Rick',
  last: 'D',
  roles: ['ROLE_USER'],
  raw: {},
};

const wrapper = ({ children }: { children: ReactNode }) => <MemoryRouter>{children}</MemoryRouter>;

beforeEach(() => {
  vi.clearAllMocks();
  sessionStorage.clear();
  useSessionStore.getState().clearSession();
});
afterEach(() => vi.useRealTimers());

describe('useAuth', () => {
  it('is signed out when nothing is stored', () => {
    const { result } = renderHook(() => useAuth());
    expect(result.current.isAuthenticated).toBe(false);
    expect(result.current.roles).toEqual([]);
    expect(result.current.isAdmin).toBe(false);
    expect(api.refresh).not.toHaveBeenCalled();
  });

  it('restores a stored session, answers role questions and refreshes silently', async () => {
    api.refresh.mockResolvedValue(null);
    sessionStorage.setItem('accessToken', 'access');
    sessionStorage.setItem('refreshToken', 'refresh');
    sessionStorage.setItem('id', '7');
    sessionStorage.setItem('userId', 'user-7');
    sessionStorage.setItem('roles', '["ROLE_USER","ROLE_ADMIN"]');

    const { result } = renderHook(() => useAuth());

    await waitFor(() => expect(result.current.isAuthenticated).toBe(true));
    expect(result.current.hasRole('ROLE_USER')).toBe(true);
    expect(result.current.isAdmin).toBe(true);
    expect(api.refresh).toHaveBeenCalledWith('refresh');
  });

  it('signs out when the silent refresh fails', async () => {
    api.refresh.mockRejectedValue(new Error('expired'));
    sessionStorage.setItem('accessToken', 'access');
    sessionStorage.setItem('refreshToken', 'refresh');

    const { result } = renderHook(() => useAuth());

    await waitFor(() => expect(api.refresh).toHaveBeenCalled());
    await waitFor(() => expect(result.current.isAuthenticated).toBe(false));
  });
});

describe('useGuestLogin', () => {
  it('signs in with the demo account and goes home', async () => {
    api.login.mockResolvedValue(session);
    const { result } = renderHook(() => useGuestLogin(), { wrapper });

    await act(() => result.current.loginAsGuest());

    expect(api.login).toHaveBeenCalledWith(DEMO_EMAIL, expect.any(String));
    expect(useSessionStore.getState().userId).toBe('user-7');
    expect(navigate).toHaveBeenCalledWith('/');
    expect(result.current.loading).toBe(false);
  });

  it('stays put when the demo login fails', async () => {
    vi.spyOn(console, 'error').mockImplementation(() => {});
    api.login.mockRejectedValue(new Error('down'));
    const { result } = renderHook(() => useGuestLogin(), { wrapper });

    await act(() => result.current.loginAsGuest());

    expect(navigate).not.toHaveBeenCalled();
    expect(useSessionStore.getState().isAuthenticated).toBe(false);
    expect(result.current.loading).toBe(false);
  });
});

describe('useTokenWatcher', () => {
  it('warns before the token expires, then signs out', () => {
    vi.useFakeTimers();
    vi.setSystemTime(new Date('2026-10-24T12:00:00Z'));
    useSessionStore.getState().setSession(session);
    useSessionStore.setState({
      sessionRestored: true,
      expiresAt: Date.now() + 10 * 60_000,
      warningBeforeMs: 60_000,
      gracePeriodMs: 0,
    });
    const expiring = vi.fn();
    const expired = vi.fn();
    window.addEventListener('session-expiring', expiring);
    window.addEventListener('session-expired', expired);

    const { result } = renderHook(() => useTokenWatcher());
    expect(result.current.showDialog).toBe(false);

    act(() => vi.advanceTimersByTime(9 * 60_000));
    expect(result.current.showDialog).toBe(true);
    expect(expiring).toHaveBeenCalledTimes(1);

    act(() => vi.advanceTimersByTime(60_000));
    expect(result.current.showDialog).toBe(false);
    expect(expired).toHaveBeenCalledTimes(1);
    expect(useSessionStore.getState().isAuthenticated).toBe(false);

    window.removeEventListener('session-expiring', expiring);
    window.removeEventListener('session-expired', expired);
  });

  it("doesn't schedule anything until the session is restored", () => {
    vi.useFakeTimers();
    useSessionStore.getState().setSession(session);
    useSessionStore.setState({ sessionRestored: false, expiresAt: Date.now() + 1000 });

    const { result } = renderHook(() => useTokenWatcher());
    act(() => vi.advanceTimersByTime(5000));

    expect(result.current.showDialog).toBe(false);
    expect(useSessionStore.getState().isAuthenticated).toBe(true);
  });

  it('hides the warning when told to', () => {
    vi.useFakeTimers();
    useSessionStore.getState().setSession(session);
    useSessionStore.setState({
      sessionRestored: true,
      expiresAt: Date.now() + 120_000,
      warningBeforeMs: 60_000,
    });
    const { result } = renderHook(() => useTokenWatcher());

    act(() => vi.advanceTimersByTime(61_000));
    expect(result.current.showDialog).toBe(true);

    act(() => {
      window.dispatchEvent(new Event('session-expiring-hide'));
    });
    expect(result.current.showDialog).toBe(false);
  });
});
