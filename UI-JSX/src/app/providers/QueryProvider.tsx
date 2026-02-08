// src/app/providers/QueryProvider.tsx
import { QueryClient, QueryClientProvider, QueryCache, MutationCache } from "@tanstack/react-query";
import { Toaster, toast } from "react-hot-toast";

const queryClient = new QueryClient({
  queryCache: new QueryCache({
    onError: (error: any) => {
      const status = error?.status ?? 0;

      // Only global-toast server failures
      if (status >= 500 || status === 0) {
        toast.error(error.message ?? "A server error occurred");
      }
    },
  }),

  mutationCache: new MutationCache({
    onError: (error: any) => {
      const status = error?.status ?? 0;

      // Only global-toast server failures
      if (status >= 500 || status === 0) {
        toast.error(error.message ?? "A server error occurred");
      }
    },
  }),

  defaultOptions: {
    queries: { retry: false },
  },
});


export function QueryProvider({ children }: { children: React.ReactNode }) {
  return (
    <QueryClientProvider client={queryClient}>
      {children}
      <Toaster position="top-right" />
    </QueryClientProvider>
  );
}
