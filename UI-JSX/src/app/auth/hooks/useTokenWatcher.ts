// src/app/auth/hooks/useTokenWatcher.ts
import { useEffect, useRef, useState, useCallback } from 'react';
import { useSessionStore } from '@/app/state/sessionStore';

export function useTokenWatcher() {
  const accessToken = useSessionStore(s => s.session?.accessToken);
  const isAuthenticated = useSessionStore(s => s.isAuthenticated);
  const expiresAt = useSessionStore(s => s.expiresAt);
  const sessionRestored = useSessionStore(s => s.sessionRestored);
  const warningBeforeMs = useSessionStore(s => s.warningBeforeMs);
  const gracePeriodMs = useSessionStore(s => s.gracePeriodMs);
  const debug = useSessionStore(s => s.sessionDebug);

  const [showDialog, setShowDialog] = useState(false);

  const warningTimerRef = useRef<number | null>(null);
  const logoutTimerRef = useRef<number | null>(null);

  const clearTimers = useCallback(() => {
    if (warningTimerRef.current !== null) {
      clearTimeout(warningTimerRef.current);
      warningTimerRef.current = null;
    }
    if (logoutTimerRef.current !== null) {
      clearTimeout(logoutTimerRef.current);
      logoutTimerRef.current = null;
    }
  }, []);

  const schedule = useCallback(() => {
    clearTimers();
    setShowDialog(false);

    if (!sessionRestored) {
      if (debug) console.debug('[Watcher] Skip schedule: session not restored');
      return;
    }

    if (!isAuthenticated || !accessToken || !expiresAt) {
      if (debug) console.debug('[Watcher] Skip schedule: missing token or expiry');
      return;
    }

    const now = Date.now();
    const warningTime = expiresAt - warningBeforeMs;
    const warningDelay = Math.max(0, warningTime - now);
    const logoutDelay = Math.max(0, expiresAt - now + gracePeriodMs);

    if (debug) {
      console.debug('[Watcher] Scheduling timers', {
        now,
        expiresAt,
        warningBeforeMs,
        warningDelay,
        logoutDelay,
      });
    }

    // Warning timer
    warningTimerRef.current = window.setTimeout(() => {
      if (useSessionStore.getState().isAuthenticated) {
        setShowDialog(true);
        window.dispatchEvent(new Event('session-expiring'));
      }
      warningTimerRef.current = null;
    }, warningDelay);

    // Logout timer
    logoutTimerRef.current = window.setTimeout(() => {
      setShowDialog(false);
      window.dispatchEvent(new Event('session-expired'));
      useSessionStore.getState().clearSession();
      logoutTimerRef.current = null;
    }, logoutDelay);
  }, [
    accessToken,
    expiresAt,
    sessionRestored,
    warningBeforeMs,
    gracePeriodMs,
    isAuthenticated,
    clearTimers,
    debug,
  ]);

  // Re-schedule when expiry or token changes
  useEffect(() => {
    schedule();
    return () => clearTimers();
  }, [schedule, clearTimers]);

  // Hide dialog on explicit hide event
  useEffect(() => {
    const hide = () => setShowDialog(false);
    window.addEventListener('session-expiring-hide', hide);
    return () => window.removeEventListener('session-expiring-hide', hide);
  }, []);

  // Hide dialog on logout
  useEffect(() => {
    const onLogout = () => {
      clearTimers();
      setShowDialog(false);
    };
    window.addEventListener('session-logged-out', onLogout);
    return () => window.removeEventListener('session-logged-out', onLogout);
  }, [clearTimers]);

  return { showDialog, setShowDialog };
}
