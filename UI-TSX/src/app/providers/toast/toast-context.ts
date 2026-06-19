import { createContext } from 'react';
import { Toast } from 'primereact/toast';

export const ToastContext = createContext<React.RefObject<Toast | null> | null>(null);
