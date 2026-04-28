import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { InitialAmountClient } from '@/api/generated/InitialAmountClient';

export function useInitialAmount() {
  const qc = useQueryClient();

  const query = useQuery({
    queryKey: ['initialAmount'],
    queryFn: () => InitialAmountClient.fetchForCurrentUser(),
  });

  const create = useMutation({
    mutationFn: (amount: number) => InitialAmountClient.create(amount),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['initialAmount'] }),
  });

  const update = useMutation({
    mutationFn: ({ id, amount }: { id: number; amount: number }) =>
      InitialAmountClient.update(id, amount),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['initialAmount'] }),
  });

  return {
    initialAmount: query.data,
    isLoading: query.isLoading,
    createInitialAmount: create.mutateAsync,
    updateInitialAmount: update.mutateAsync,
  };
}
