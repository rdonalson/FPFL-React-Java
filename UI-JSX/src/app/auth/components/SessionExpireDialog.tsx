import { Dialog } from 'primereact/dialog';
import { Button } from 'primereact/button';
import { useSessionStore } from '@/app/state/sessionStore';
import { refreshApi } from '@/app/auth/api/authApi';

interface Props {
  visible: boolean;
  onHide: () => void;
}

export function SessionExpireDialog({ visible, onHide }: Props) {
  const session = useSessionStore(s => s.session);
  const setSession = useSessionStore(s => s.setSession);
  const clearSession = useSessionStore(s => s.clearSession);

  const stayLoggedIn = async () => {
    try {
      if (!session?.refreshToken) {
        throw new Error('No refresh token available');
      }

      const refreshed = await refreshApi(session.refreshToken);

      // ⭐ FIX: merge refreshed tokens with existing session
      setSession({
        accessToken: refreshed.accessToken,
        refreshToken: refreshed.refreshToken,

        id: session.id!, // non-null assertion
        userId: session.userId!,
        email: session.email!,
        first: session.first!,
        last: session.last!,
        roles: session.roles!,

        raw: refreshed,
      });

      onHide();
    } catch (err) {
      console.error('Failed to refresh session', err);
      clearSession();
      onHide();
    }
  };

  return (
    <Dialog
      header="Session Expiring"
      visible={visible}
      onHide={onHide}
      modal
      style={{ width: '25rem' }}
    >
      <p>Your session is about to expire. Would you like to stay logged in?</p>

      <div className="flex justify-content-end gap-2 mt-3">
        <Button label="Logout" severity="danger" onClick={clearSession} />
        <Button label="Stay Logged In" onClick={stayLoggedIn} />
      </div>
    </Dialog>
  );
}
