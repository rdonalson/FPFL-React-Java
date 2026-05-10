// src/app/hooks/useAuthOnStartup.ts
import { useEffect } from 'react';
import { useSessionStore } from '@/app/state/sessionStore';
import { normalizeAuthResponse } from '@/api/utils/normalizeAuthResponse';

export function useAuthOnStartup() {
  const setSession = useSessionStore(s => s.setSession);
  const clearSession = useSessionStore(s => s.clearSession);

  useEffect(() => {
    const stored = sessionStorage.getItem('raw');

    // No stored session → nothing to restore
    if (!stored) {
      clearSession();
      return;
    }

    try {
      const parsed = JSON.parse(stored);

      // If no accessToken → treat as logged out
      if (!parsed.accessToken) {
        clearSession();
        return;
      }

      // Normalize the stored raw session into the new session shape
      const normalized = normalizeAuthResponse(parsed);

      // Hydrate Zustand + sessionStorage
      setSession(normalized);
    } catch (err) {
      console.error('Failed to restore session:', err);
      clearSession();
    }
  }, []);
}
