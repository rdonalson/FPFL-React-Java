// src/app/auth/layout/AuthDialogLayout.tsx
import { Dialog } from 'primereact/dialog';

interface AuthDialogLayoutProps {
  title: string;
  visible: boolean;
  onHide: () => void;
  children: React.ReactNode;
}

export function AuthDialogLayout({ title, visible, onHide, children }: AuthDialogLayoutProps) {
  return (
    <Dialog
      header={title}
      visible={visible}
      onHide={onHide}
      modal
      style={{ width: '720px' }}
      breakpoints={{ '960px': '80vw', '640px': '100vw' }}
      contentClassName="p-pt-2 p-px-4"
    >
      {children}
    </Dialog>
  );
}
