// src/app/providers/QueryProvider.tsx
import { QueryClient, QueryClientProvider, QueryCache, MutationCache } from "@tanstack/react-query";
import { Toaster, toast } from "react-hot-toast";

const queryClient = new QueryClient({
  queryCache: new QueryCache({
    onError: (error: unknown) => {
      const message =
        (error as any)?.message ?? "An unexpected error occurred while fetching data";
      toast.error(message);
    },
  }),
  mutationCache: new MutationCache({
    onError: (error: unknown) => {
      const message =
        (error as any)?.message ?? "An unexpected error occurred while saving changes";
      toast.error(message);
    },
  }),
  defaultOptions: {
    queries: {
      retry: false,
    },
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
