// src/test/session.ts — sign a test user in or out of the real session store
import { useSessionStore } from '@/app/state/sessionStore';

export const TEST_USER_ID = 'user-1';

export function signIn(
  roles: string[] = ['ROLE_USER'],
  overrides: Partial<{ email: string; first: string; last: string }> = {},
) {
  sessionStorage.setItem('accessToken', 'access');
  sessionStorage.setItem('roles', JSON.stringify(roles));
  useSessionStore.getState().setSession({
    accessToken: 'access',
    refreshToken: '',
    id: 1,
    userId: TEST_USER_ID,
    email: overrides.email ?? 'rick@example.com',
    first: overrides.first ?? 'Rick',
    last: overrides.last ?? 'Donalson',
    roles,
    raw: {},
  });
}

export function signOut() {
  sessionStorage.clear();
  useSessionStore.getState().clearSession();
}
