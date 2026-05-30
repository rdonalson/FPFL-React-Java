// src/app/auth/RegisterDialog.tsx
import { RegisterForm } from './RegisterForm';
import { useSessionStore } from '@/app/state/sessionStore';
import { AuthDialogLayout } from '@/app/layout/AuthDialogLayout';

interface RegisterDialogProps {
  visible: boolean;
  onHide: () => void;
}

export function RegisterDialog({ visible, onHide }: RegisterDialogProps) {
  const setSession = useSessionStore(s => s.setSession);

  return (
    <AuthDialogLayout title="Create Account" visible={visible} onHide={onHide}>
      <RegisterForm
        onSuccess={session => {
          setSession(session);
          onHide();
        }}
      />
    </AuthDialogLayout>
  );
}
