// src/app/auth/RegisterDialog.tsx
import { Dialog } from 'primereact/dialog';
import { RegisterForm } from './RegisterForm';
import { useSessionStore } from '@/app/state/sessionStore';

interface RegisterDialogProps {
  visible: boolean;
  onHide: () => void;
}

export function RegisterDialog({ visible, onHide }: RegisterDialogProps) {
  const setSession = useSessionStore(s => s.setSession);

  return (
    <Dialog
      header="Create Account"
      visible={visible}
      onHide={onHide}
      style={{ width: '30rem' }}
      modal
    >
      <RegisterForm
        onSuccess={session => {
          setSession(session);
          onHide();
        }}
      />
    </Dialog>
  );
}
