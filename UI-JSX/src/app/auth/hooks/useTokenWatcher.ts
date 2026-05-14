import { useEffect, useState } from 'react';
import { jwtDecode } from 'jwt-decode';
import { useSessionStore } from '@/app/state/sessionStore';

export function useTokenWatcher() {
  const accessToken = useSessionStore(s => s.session?.accessToken);
  const [showDialog, setShowDialog] = useState(false);

  // Schedule dialog before expiration
  useEffect(() => {
    if (!accessToken) return;

    const { exp } = jwtDecode<{ exp: number }>(accessToken);
    const expiresAt = exp * 1000;
    const now = Date.now();

    const warningTime = expiresAt - 10_000; // warn 10 seconds before expiration
    const delay = Math.max(0, warningTime - now);

    const timer = setTimeout(() => {
      setShowDialog(true);
    }, delay);

    return () => clearTimeout(timer);
  }, [accessToken]);

  return { showDialog, setShowDialog };
}
