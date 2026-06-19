// src/app/providers/AppProviders.tsx
import { PrimeReactProvider } from 'primereact/api';
import { ToastProvider } from './toast/ToastProvider';
import { AppErrorBoundary } from './error/AppErrorBoundary';
import { QueryProvider } from './query/QueryProvider';

export function AppProviders({ children }: { children: React.ReactNode }) {
  return (
    <PrimeReactProvider value={{ ripple: true }}>
      <ToastProvider>
        <AppErrorBoundary>
          <QueryProvider>{children}</QueryProvider>
        </AppErrorBoundary>
      </ToastProvider>
    </PrimeReactProvider>
  );
}
