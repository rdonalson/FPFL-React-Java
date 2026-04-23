// src/app/hooks/useAuthOnStartup.ts
import {
  GUEST_USER_ID,
  GUEST_TOKEN,
  SESSION_USERID_KEY,
  SESSION_TOKEN_KEY,
} from '@/app/auth/constants';
import { useEffect, useState } from 'react';
import { useQueryClient } from '@tanstack/react-query';
import { useSessionStore } from '@/app/state/sessionStore';
import * as authApi from '@/api/authApi';
import { apiClient } from '@/api/client';

/**
 * Helper to defensively unwrap either an AxiosResponse-like object or a plain payload.
 */
function unwrap<T = any>(maybeResponse: any): T | null {
  if (!maybeResponse) return null;
  if (maybeResponse && typeof maybeResponse === 'object' && 'data' in maybeResponse) {
    return (maybeResponse as any).data ?? null;
  }
  return maybeResponse as T;
}

/**
 * Restores session on app startup:
 * - reads token and optional userId from sessionStorage
 * - handles guest token shortcut
 * - validates non-guest token via /auth/me
 * - prefetches important queries for authenticated users
 *
 * Returns { isChecking } so callers can avoid rendering protected UI until startup completes.
 */
export function useAuthOnStartup() {
  const setUserId = useSessionStore(s => s.setUserId);
  const setToken = useSessionStore(s => s.setToken);
  const clearSession = useSessionStore(s => s.clearSession);
  const queryClient = useQueryClient();
  const [isChecking, setIsChecking] = useState(true);

  useEffect(() => {
    let mounted = true;

    async function restore() {
      try {
        // Read persisted token and optional persisted userId
        const token = sessionStorage.getItem(SESSION_TOKEN_KEY);
        const persistedUserId = sessionStorage.getItem(SESSION_USERID_KEY);

        // If no token, nothing to restore
        if (!token) {
          if (mounted) setIsChecking(false);
          return;
        }

        // If this is the guest token, set guest userId and skip backend calls
        if (token === GUEST_TOKEN || persistedUserId === GUEST_USER_ID) {
          if (mounted) {
            setToken(GUEST_TOKEN);
            setUserId(GUEST_USER_ID);
          }
          if (mounted) setIsChecking(false);
          return;
        }

        // For non-guest tokens: validate token by calling /auth/me
        // authApi.meApi is defensive but we unwrap just in case
        const meRaw = await authApi.meApi();
        const me = unwrap(meRaw) ?? meRaw;

        if (mounted) {
          setToken(token);
          if (me?.userId) {
            setUserId(String(me.userId));
            // Persist userId for future reloads
            sessionStorage.setItem(SESSION_USERID_KEY, String(me.userId));
          }
        }

        // Prefetch important queries so UI is ready (only for real authenticated users)
        await Promise.all([
          queryClient.prefetchQuery({
            queryKey: ['initialAmount'],
            queryFn: async () => {
              const res = await apiClient.get('/items?itemType=3');
              return unwrap(res) ?? res;
            },
          }),
        ]);
      } catch (err) {
        // validation failed — clear session and fall back to logged-out state
        console.error('useAuthOnStartup restore error', err);
        clearSession();
      } finally {
        if (mounted) setIsChecking(false);
      }
    }

    // Run restore and ensure any top-level rejection is caught
    restore().catch(e => {
      console.error('useAuthOnStartup top-level error', e);
      if (mounted) {
        clearSession();
        setIsChecking(false);
      }
    });

    return () => {
      mounted = false;
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [setUserId, setToken, clearSession, queryClient]);

  return { isChecking };
}
