// src/app/auth/hooks/useAuth.ts
import { useEffect } from 'react';
import { useSessionStore } from '@/app/state/sessionStore';
import { authApi } from '../api/authApi';

export function useAuth() {
  const setSession = useSessionStore(s => s.setSession);
  const clearSession = useSessionStore(s => s.clearSession);

  useEffect(() => {
    const accessToken = sessionStorage.getItem('accessToken');

    if (!accessToken) {
      clearSession();
      return;
    }

    // Rehydrate from sessionStorage
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

      // Optional: attempt silent refresh
      authApi.refresh(session.refreshToken!).catch(() => clearSession());
    } catch {
      clearSession();
    }
  }, [setSession, clearSession]);
}
