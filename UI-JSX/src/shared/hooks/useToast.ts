// src/shared/hooks/useToast.ts

import { useOutletContext } from 'react-router-dom';
import { Toast } from 'primereact/toast';

interface LayoutContext {
  toastRef: React.RefObject<Toast>;
}

export function useToast() {
  return useOutletContext<LayoutContext>().toastRef;
}
