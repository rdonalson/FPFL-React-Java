import { useRef } from 'react';
import { Toast } from 'primereact/toast';
import { ToastContext } from './toast-context';

export function ToastProvider({ children }: { children: React.ReactNode }) {
  const toastRef = useRef<Toast>(null);

  return (
    <ToastContext.Provider value={toastRef}>
      <Toast ref={toastRef} />
      {children}
    </ToastContext.Provider>
  );
}
