// src/app/auth/LoginDialog.tsx
import { Dialog } from 'primereact/dialog';
import { LoginForm } from './LoginForm';
import { useSessionStore } from '@/app/state/sessionStore';

interface LoginDialogProps {
  visible: boolean;
  onHide: () => void;
}

export function LoginDialog({ visible, onHide }: LoginDialogProps) {
  const setSession = useSessionStore(s => s.setSession);

  return (
    <Dialog header="Login" visible={visible} onHide={onHide} style={{ width: '30rem' }} modal>
      <LoginForm
        onSuccess={session => {
          setSession({ ...session, raw: session });
          onHide();
        }}
      />
    </Dialog>
  );
}
