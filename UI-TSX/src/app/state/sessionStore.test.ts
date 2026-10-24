import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';

const session = {
  accessToken: 'access',
  refreshToken: 'refresh',
  id: 7,
  userId: 'user-7',
  email: 'rick@example.com',
  first: 'Rick',
  last: 'D',
  roles: ['ROLE_USER'],
  raw: { expiresIn: 900 },
};

// The store hydrates from sessionStorage when it's created, so each test gets a fresh module
async function freshStore() {
  vi.resetModules();
  return (await import('./sessionStore')).useSessionStore;
}

beforeEach(() => sessionStorage.clear());
afterEach(() => vi.useRealTimers());

describe('sessionStore', () => {
  it('starts signed out with an empty sessionStorage', async () => {
    const store = await freshStore();
    expect(store.getState().isAuthenticated).toBe(false);
    expect(store.getState().userId).toBeNull();
    expect(store.getState().roles).toBeNull();
  });

  it('hydrates a session that survived a page reload', async () => {
    sessionStorage.setItem('accessToken', 'token');
    sessionStorage.setItem('userId', 'u-1');
    sessionStorage.setItem('id', '4');
    sessionStorage.setItem('roles', '["ROLE_ADMIN"]');
    sessionStorage.setItem('raw', '{not json');
    sessionStorage.setItem('expiresAt', '1800000000000');
    sessionStorage.setItem('sessionRestored', '1');

    const state = (await freshStore()).getState();
    expect(state.isAuthenticated).toBe(true);
    expect(state.userId).toBe('u-1');
    expect(state.id).toBe(4);
    expect(state.roles).toEqual(['ROLE_ADMIN']);
    expect(state.raw).toBeNull(); // unparseable JSON is ignored
    expect(state.expiresAt).toBe(1800000000000);
    expect(state.sessionRestored).toBe(true);
  });

  it('setSession stores everything, works out the expiry and announces the login', async () => {
    vi.useFakeTimers();
    vi.setSystemTime(new Date('2026-10-24T12:00:00Z'));
    const store = await freshStore();
    const loggedIn = vi.fn();
    window.addEventListener('session-logged-in', loggedIn);

    store.getState().setSession(session);

    const state = store.getState();
    expect(state.isAuthenticated).toBe(true);
    expect(state.session?.email).toBe('rick@example.com');
    expect(state.expiresAt).toBe(Date.now() + 900_000);
    expect(sessionStorage.getItem('accessToken')).toBe('access');
    expect(sessionStorage.getItem('roles')).toBe('["ROLE_USER"]');
    expect(loggedIn).toHaveBeenCalledTimes(1);
    window.removeEventListener('session-logged-in', loggedIn);
  });

  it('reads expiresAt given in seconds or milliseconds', async () => {
    const store = await freshStore();
    store.getState().setSession({ ...session, raw: { expiresAt: 1_800_000_000 } });
    expect(store.getState().expiresAt).toBe(1_800_000_000_000);

    store.getState().setSession({ ...session, raw: { tokenExpiresAt: 1_800_000_000_123 } });
    expect(store.getState().expiresAt).toBe(1_800_000_000_123);
  });

  it('clearSession wipes storage and state and announces the logout', async () => {
    const store = await freshStore();
    store.getState().setSession(session);
    const loggedOut = vi.fn();
    const hide = vi.fn();
    window.addEventListener('session-logged-out', loggedOut);
    window.addEventListener('session-expiring-hide', hide);

    store.getState().clearSession();

    expect(store.getState().isAuthenticated).toBe(false);
    expect(store.getState().session).toBeNull();
    expect(sessionStorage.getItem('accessToken')).toBeNull();
    expect(sessionStorage.getItem('userId')).toBeNull();
    expect(loggedOut).toHaveBeenCalledTimes(1);
    expect(hide).toHaveBeenCalledTimes(1);
    window.removeEventListener('session-logged-out', loggedOut);
    window.removeEventListener('session-expiring-hide', hide);
  });

  it('watcher helpers persist their values', async () => {
    const store = await freshStore();

    store.getState().setExpiresAt(123);
    expect(sessionStorage.getItem('expiresAt')).toBe('123');
    store.getState().setExpiresAt(null);
    expect(sessionStorage.getItem('expiresAt')).toBeNull();

    store.getState().setSessionRestored(true);
    expect(sessionStorage.getItem('sessionRestored')).toBe('1');
    store.getState().setSessionRestored(false);
    expect(store.getState().sessionRestored).toBe(false);

    store.getState().setWatcherConfig({ warningBeforeMs: 60_000, debug: true });
    expect(store.getState().warningBeforeMs).toBe(60_000);
    expect(store.getState().sessionDebug).toBe(true);
  });
});
