// src/app/providers/QueryProvider.tsx
import { QueryClient, QueryClientProvider, QueryCache, MutationCache } from '@tanstack/react-query';
import { useToast } from '@/shared/hooks/useToast';

export function QueryProvider({ children }: { children: React.ReactNode }) {
  const toast = useToast();

  const handleGlobalError = (error: any) => {
    const status = error?.status ?? 0;

    if (status >= 500 || status === 0) {
      toast?.current?.show({
        severity: 'error',
        summary: 'Server Error',
        detail: error?.message ?? 'A server error occurred',
        life: 4000,
      });
    }
  };

  const queryClient = new QueryClient({
    queryCache: new QueryCache({
      onError: handleGlobalError,
    }),
    mutationCache: new MutationCache({
      onError: handleGlobalError,
    }),
    defaultOptions: {
      queries: { retry: false },
    },
  });

  return <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>;
}
