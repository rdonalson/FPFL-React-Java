// src/app/auth/RegisterDialog.tsx
import { RegisterForm } from './RegisterForm';
import { useSessionStore } from '@/app/state/sessionStore';
import { AuthDialogLayout } from '@/app/layout/AuthDialogLayout';
import { createInitialAmount } from '@/features/catalog-command/transactions/api/initialAmountApi';

interface RegisterDialogProps {
  visible: boolean;
  onHide: () => void;
}

export function RegisterDialog({ visible, onHide }: RegisterDialogProps) {
  const setSession = useSessionStore(s => s.setSession);

  return (
    <AuthDialogLayout title="Create Account" visible={visible} onHide={onHide}>
      <RegisterForm
        onSuccess={async session => {
          setSession(session);

          // Auto-create an initial amount for the new user. This is a bit of a hack to
          // avoid having to handle the "no initial amount" case in the UI, but it keeps things simple.
          try {
            await createInitialAmount(0);
          } catch (err) {
            console.error('Failed to auto-create initial amount', err);
          }

          onHide();
        }}
      />
    </AuthDialogLayout>
  );
}
