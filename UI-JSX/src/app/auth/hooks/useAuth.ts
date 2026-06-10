// src/app/auth/hooks/useAuth.ts
import { useEffect } from 'react';
import { useSessionStore } from '@/app/state/sessionStore';
import { authApi } from '../api/authApi';

export function useAuth() {
  const session = useSessionStore(s => s.session);
  const setSession = useSessionStore(s => s.setSession);
  const clearSession = useSessionStore(s => s.clearSession);

  // --- Startup hydration ---
  useEffect(() => {
    const accessToken = sessionStorage.getItem('accessToken');

    if (!accessToken) {
      clearSession();
      return;
    }

    try {
      const session = {
        accessToken,
        refreshToken: sessionStorage.getItem('refreshToken'),
        id: Number(sessionStorage.getItem('id')),
        userId: sessionStorage.getItem('userId') || '',
        email: sessionStorage.getItem('email') || '',
        first: sessionStorage.getItem('first') || '',
        last: sessionStorage.getItem('last') || '',
        roles: JSON.parse(sessionStorage.getItem('roles') || '[]'),
        raw: JSON.parse(sessionStorage.getItem('raw') || '{}'),
      };

      setSession(session);

      // Optional silent refresh
      authApi.refresh(session.refreshToken!).catch(() => clearSession());
    } catch {
      clearSession();
    }
  }, [setSession, clearSession]);

  // --- Role helpers ---
  const roles = session?.roles ?? [];
  const isAuthenticated = Boolean(session?.accessToken);

  const hasRole = (role: string) => roles.includes(role);
  const isAdmin = hasRole('ROLE_ADMIN');

  return {
    session,
    isAuthenticated,
    roles,
    hasRole,
    isAdmin,
  };
}
