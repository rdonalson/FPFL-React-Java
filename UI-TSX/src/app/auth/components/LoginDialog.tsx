// src/app/auth/LoginDialog.tsx
import { LoginForm } from './LoginForm';
import { useSessionStore } from '@/app/state/sessionStore';
import { AuthDialogLayout } from '@/app/layout/AuthDialogLayout';

interface LoginDialogProps {
  visible: boolean;
  onHide: () => void;
}

export function LoginDialog({ visible, onHide }: LoginDialogProps) {
  const setSession = useSessionStore(s => s.setSession);

  return (
    <AuthDialogLayout title="Login" visible={visible} onHide={onHide}>
      <LoginForm
        onSuccess={session => {
          setSession({ ...session, raw: session });
          onHide();
        }}
      />
    </AuthDialogLayout>
  );
}
