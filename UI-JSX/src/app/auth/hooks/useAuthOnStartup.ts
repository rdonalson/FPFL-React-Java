// src/app/hooks/useAuthOnStartup.ts
import { useEffect } from 'react';
import { useSessionStore } from '@/app/state/sessionStore';
import { normalizeAuthResponse } from '@/api/utils/normalizeAuthResponse';

export function useAuthOnStartup() {
  const setSession = useSessionStore(s => s.setSession);
  const clearSession = useSessionStore(s => s.clearSession);

  useEffect(() => {
    const accessToken = sessionStorage.getItem('accessToken');

    // No stored token → logged out
    if (!accessToken) {
      clearSession();
      return;
    }

    try {
      // Rebuild the session object from sessionStorage
      const raw = JSON.parse(sessionStorage.getItem('raw') || '{}');

      const normalized = normalizeAuthResponse({
        accessToken,
        refreshToken: sessionStorage.getItem('refreshToken'),
        id: Number(sessionStorage.getItem('id')),
        userId: sessionStorage.getItem('userId'),
        email: sessionStorage.getItem('email'),
        first: sessionStorage.getItem('first'),
        last: sessionStorage.getItem('last'),
        roles: JSON.parse(sessionStorage.getItem('roles') || '[]'),
        raw,
      });

      setSession(normalized);
    } catch (err) {
      console.error('Failed to restore session:', err);
      clearSession();
    }
  }, [clearSession, setSession]);
}
