// src/app/auth/ChangePasswordDialog.tsx
import { AuthDialogLayout } from '@/app/layout/AuthDialogLayout';
import { ChangePasswordForm } from './ChangePasswordForm';

interface ChangePasswordDialogProps {
  visible: boolean;
  onHide: () => void;
  id: number;
}

export function ChangePasswordDialog({ visible, onHide, id }: ChangePasswordDialogProps) {
  return (
    <AuthDialogLayout title="Change Password" visible={visible} onHide={onHide}>
      <ChangePasswordForm id={id} onSuccess={onHide} />
    </AuthDialogLayout>
  );
}
