// src/shared/hooks/useToast.ts
import { useContext } from 'react';
import { ToastContext } from '@/app/providers/toast-context';

export function useToast() {
  return useContext(ToastContext);
}
