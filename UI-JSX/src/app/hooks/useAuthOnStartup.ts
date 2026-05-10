// src/app/hooks/useAuthOnStartup.ts
import { useEffect, useState } from 'react';
import { useSessionStore } from '@/app/state/sessionStore';

/**
 * Restores session on app startup:
 * - reads token, refreshToken, userId, email, roles, raw from sessionStorage
 * - if token exists, restores the full session into Zustand
 * - if no token, leaves user logged out
 *
 * Returns { isChecking } so AuthGate can avoid rendering protected UI
 * until the session is restored.
 */
export function useAuthOnStartup() {
  const setSession = useSessionStore(s => s.setSession);
  const clearSession = useSessionStore(s => s.clearSession);

  const [isChecking, setIsChecking] = useState(true);

  useEffect(() => {
    let mounted = true;

    function restore() {
      try {
        const token = sessionStorage.getItem('token');

        // No token → no session
        if (!token) {
          if (mounted) setIsChecking(false);
          return;
        }

        // Restore everything saved during login
        const refreshToken = sessionStorage.getItem('refreshToken');
        const userId = sessionStorage.getItem('userId') || ''; // <- force string
        const email = sessionStorage.getItem('email') || ''; // <- force string
        const roles = JSON.parse(sessionStorage.getItem('roles') || '[]');
        const raw = JSON.parse(sessionStorage.getItem('raw') || '{}');

        setSession({
          token,
          refreshToken,
          userId,
          email,
          roles,
          raw,
        });
      } catch (err) {
        console.error('useAuthOnStartup restore error', err);
        clearSession();
      } finally {
        if (mounted) setIsChecking(false);
      }
    }

    restore();

    return () => {
      mounted = false;
    };
  }, [setSession, clearSession]);

  return { isChecking };
}
