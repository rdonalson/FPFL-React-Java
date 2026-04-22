/* eslint-disable @typescript-eslint/no-unused-vars */
// src/hooks/useAuthOnStartup.ts
import {
  GUEST_USER_ID,
  GUEST_TOKEN,
  SESSION_USERID_KEY,
  SESSION_TOKEN_KEY,
} from '@/app/auth/constants';
import { useEffect, useState } from 'react';
import { useQueryClient } from '@tanstack/react-query';
import { useSessionStore } from '../state/sessionStore';
import * as authApi from '../../api/authApi';
import { apiClient } from '../../api/client';

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
        const meRaw = await authApi.meApi();
        const me: any = meRaw && (meRaw as any).data ? (meRaw as any).data : meRaw;

        if (mounted) {
          setToken(token);
          if (me?.userId) {
            setUserId(me.userId);
            // Optionally persist userId for future guest detection or reloads
            sessionStorage.setItem('userId', String(me.userId));
          }
        }

        // Prefetch important queries so UI is ready (only for real authenticated users)
        await Promise.all([
          queryClient.prefetchQuery({
            queryKey: ['initialAmount'],
            queryFn: async () => {
              const res = await apiClient.get('/items?itemType=3');
              return res && (res as any).data ? (res as any).data : res;
            },
          }),
        ]);
      } catch (err) {
        // validation failed — clear session and fall back to login
        clearSession();
      } finally {
        if (mounted) setIsChecking(false);
      }
    }

    restore();

    return () => {
      mounted = false;
    };
  }, [setUserId, setToken, clearSession, queryClient]);

  return { isChecking };
}
