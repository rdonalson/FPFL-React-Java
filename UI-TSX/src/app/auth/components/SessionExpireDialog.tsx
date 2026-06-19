// src/app/auth/components/SessionExpireDialog.tsx
import { useEffect } from 'react';
import { Dialog } from 'primereact/dialog';
import { Button } from 'primereact/button';
import { authApi } from '@/app/auth/api/authApi';
import { useSessionStore } from '@/app/state/sessionStore';

interface Props {
  visible: boolean;
  onHide: () => void;
}

export function SessionExpireDialog({ visible, onHide }: Props) {
  const refreshToken = useSessionStore(s => s.session?.refreshToken);

  useEffect(() => {
    const hide = () => onHide();
    window.addEventListener('session-expiring-hide', hide);
    return () => window.removeEventListener('session-expiring-hide', hide);
  }, [onHide]);

  const handleStayLoggedIn = async () => {
    if (!refreshToken) return;

    const res = await authApi.refresh(refreshToken);

    if (res) {
      // Hide dialog after successful refresh
      window.dispatchEvent(new Event('session-expiring-hide'));
    }
  };

  const handleLogout = () => {
    useSessionStore.getState().clearSession();
    window.dispatchEvent(new Event('session-expiring-hide'));
  };

  return (
    <Dialog header="Session Expiring" visible={visible} onHide={onHide} closable={false} modal>
      <p>Your session is about to expire. Would you like to stay logged in?</p>

      <div className="flex justify-end gap-2 mt-4">
        <Button label="Logout" severity="danger" onClick={handleLogout} />
        <Button label="Stay Logged In" onClick={handleStayLoggedIn} />
      </div>
    </Dialog>
  );
}
