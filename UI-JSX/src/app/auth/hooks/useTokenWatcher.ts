// src/app/auth/hooks/useTokenWatcher.ts
import { useEffect, useState } from 'react';
import { jwtDecode } from 'jwt-decode';
import { useSessionStore } from '@/app/state/sessionStore';

export function useTokenWatcher() {
  const session = useSessionStore(s => s.session);
  const [showDialog, setShowDialog] = useState(false);

  useEffect(() => {
    if (!session?.accessToken) return;

    const { exp } = jwtDecode<{ exp: number }>(session.accessToken);
    const expiresAt = exp * 1000;
    const now = Date.now();

    const warningTime = expiresAt - 60_000; // 1 minute before expiration

    if (warningTime <= now) {
      setShowDialog(true);
      return;
    }

    const timer = setTimeout(() => {
      setShowDialog(true);
    }, warningTime - now);

    return () => clearTimeout(timer);
  }, [session]);

  return { showDialog, setShowDialog };
}
