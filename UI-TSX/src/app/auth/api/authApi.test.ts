import { beforeEach, describe, expect, it, vi } from 'vitest';

const client = vi.hoisted(() => ({
  login: vi.fn(),
  register: vi.fn(),
  refresh: vi.fn(),
  changePassword: vi.fn(),
}));
vi.mock('@/api/generated/AuthClient', () => ({ AuthClient: client }));

import { authApi } from './authApi';
import { useSessionStore } from '@/app/state/sessionStore';

/** A JWT whose payload carries `exp` (seconds). */
const jwt = (exp: number) => `x.${btoa(JSON.stringify({ exp }))}.y`;

const auth = {
  accessToken: jwt(1_800_000_000),
  refreshToken: 'refresh-1',
  id: 7,
  userId: 'user-7',
  email: 'rick@example.com',
  first: 'Rick',
  last: 'D',
  roles: ['ROLE_USER'],
};

beforeEach(() => {
  vi.clearAllMocks();
  sessionStorage.clear();
  useSessionStore.getState().clearSession();
});

describe('authApi', () => {
  it('login stores the session and the JWT expiry', async () => {
    client.login.mockResolvedValue({ status: 200, data: auth });

    const result = await authApi.login('rick@example.com', 'secret');

    expect(client.login).toHaveBeenCalledWith({ email: 'rick@example.com', password: 'secret' });
    expect(result.userId).toBe('user-7');
    const state = useSessionStore.getState();
    expect(state.isAuthenticated).toBe(true);
    expect(state.roles).toEqual(['ROLE_USER']);
    expect(state.expiresAt).toBe(1_800_000_000_000);
    expect(state.sessionRestored).toBe(true);
  });

  it('login fails loudly when the API returns nothing', async () => {
    client.login.mockResolvedValue({ status: 401, message: 'Invalid credentials', data: null });
    await expect(authApi.login('x', 'y')).rejects.toThrow('Invalid credentials');
    expect(useSessionStore.getState().isAuthenticated).toBe(false);
  });

  it('register signs the new user in', async () => {
    client.register.mockResolvedValue({ status: 201, data: { ...auth, accessToken: 'not-a-jwt' } });

    await authApi.register('Rick', 'D', 'rick@example.com', 'secret');

    expect(client.register).toHaveBeenCalledWith({
      first: 'Rick',
      last: 'D',
      email: 'rick@example.com',
      password: 'secret',
    });
    expect(useSessionStore.getState().isAuthenticated).toBe(true);
    expect(useSessionStore.getState().expiresAt).toBeNull(); // undecodable token → no expiry
  });

  it('refresh swaps the tokens but keeps who you are', async () => {
    client.login.mockResolvedValue({ status: 200, data: auth });
    await authApi.login('rick@example.com', 'secret');
    client.refresh.mockResolvedValue({
      status: 200,
      data: { accessToken: jwt(1_900_000_000), refreshToken: 'refresh-2' },
    });

    const data = await authApi.refresh('refresh-1');

    expect(data?.refreshToken).toBe('refresh-2');
    const state = useSessionStore.getState();
    expect(state.refreshToken).toBe('refresh-2');
    expect(state.email).toBe('rick@example.com');
    expect(state.expiresAt).toBe(1_900_000_000_000);
  });

  it('refresh does nothing without a refresh token', async () => {
    expect(await authApi.refresh('')).toBeNull();
    expect(client.refresh).not.toHaveBeenCalled();
  });
});
