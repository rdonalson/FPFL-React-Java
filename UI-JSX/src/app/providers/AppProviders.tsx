// src/app/providers/AppProviders.tsx
import { PrimeReactProvider } from 'primereact/api';
import { ToastProvider } from './toast/ToastProvider';
import { AppErrorBoundary } from './error/AppErrorBoundary';
import { QueryProvider } from './query/QueryProvider';
import { LayoutProvider } from '@/app/layout/context/layoutcontext';

export function AppProviders({ children }: { children: React.ReactNode }) {
  return (
    <PrimeReactProvider value={{ ripple: true }}>
      <LayoutProvider>
        <ToastProvider>
          <AppErrorBoundary>
            <QueryProvider>
              {children}
            </QueryProvider>
          </AppErrorBoundary>
        </ToastProvider>
      </LayoutProvider>
    </PrimeReactProvider>
  );
}