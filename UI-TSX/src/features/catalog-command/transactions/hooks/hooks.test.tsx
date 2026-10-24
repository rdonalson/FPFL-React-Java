import { beforeEach, describe, expect, it, vi } from 'vitest';
import { act, renderHook, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import type { ReactNode } from 'react';

const itemApi = vi.hoisted(() => ({
  getItemsForUserAndType: vi.fn(),
  createItem: vi.fn(),
  updateItem: vi.fn(),
  deleteItem: vi.fn(),
}));
vi.mock('../api/itemApi', () => itemApi);
const client = vi.hoisted(() => ({
  fetchForCurrentUser: vi.fn(),
  create: vi.fn(),
  update: vi.fn(),
}));
vi.mock('@/api/generated/InitialAmountClient', () => ({ InitialAmountClient: client }));

import { useItem } from './useItem';
import { useInitialAmount } from './useInitialAmount';

beforeEach(() => vi.clearAllMocks());

describe('useItem', () => {
  it('loads, creates, updates and removes items in its list', async () => {
    itemApi.getItemsForUserAndType.mockResolvedValue([{ id: 1, name: 'Rent' }]);
    itemApi.createItem.mockResolvedValue({ id: 2, name: 'Gym' });
    itemApi.updateItem.mockResolvedValue({ id: 1, name: 'Rent (new)' });
    itemApi.deleteItem.mockResolvedValue(undefined);
    const { result } = renderHook(() => useItem());

    await act(() => result.current.loadForUserAndType('user-1', 2));
    expect(result.current.items).toEqual([{ id: 1, name: 'Rent' }]);

    await act(() => result.current.create({ name: 'Gym' } as any));
    expect(result.current.items.map(i => i.id)).toEqual([2, 1]);

    await act(() => result.current.update(1, { name: 'Rent (new)' } as any));
    expect(result.current.items.find(i => i.id === 1)?.name).toBe('Rent (new)');

    await act(() => result.current.remove(2));
    expect(result.current.items.map(i => i.id)).toEqual([1]);
    expect(result.current.loading).toBe(false);
  });

  it('keeps the error message and rethrows failed saves', async () => {
    itemApi.getItemsForUserAndType.mockRejectedValue(new Error('Database failure'));
    itemApi.createItem.mockRejectedValue(new Error('Item Name is required'));
    const { result } = renderHook(() => useItem());

    await act(() => result.current.loadForUserAndType('user-1', 1));
    expect(result.current.error).toBe('Database failure');

    await act(async () => {
      await expect(result.current.create({} as any)).rejects.toThrow('Item Name is required');
    });
    expect(result.current.error).toBe('Item Name is required');
  });
});

describe('useInitialAmount', () => {
  it('reads it and refreshes after saving', async () => {
    const qc = new QueryClient({ defaultOptions: { queries: { retry: false } } });
    const wrapper = ({ children }: { children: ReactNode }) => (
      <QueryClientProvider client={qc}>{children}</QueryClientProvider>
    );
    client.fetchForCurrentUser.mockResolvedValue({ data: { id: 1, amount: 100 } });
    client.create.mockResolvedValue({ data: { id: 1, amount: 0 } });
    client.update.mockResolvedValue({ data: { id: 1, amount: 200 } });

    const { result } = renderHook(() => useInitialAmount(), { wrapper });
    await waitFor(() =>
      expect(result.current.initialAmount).toEqual({ data: { id: 1, amount: 100 } }),
    );

    await act(() => result.current.updateInitialAmount({ id: 1, amount: 200 }));
    expect(client.update).toHaveBeenCalledWith(1, 200);
    await act(() => result.current.createInitialAmount(0));
    expect(client.create).toHaveBeenCalledWith(0);
    await waitFor(() => expect(client.fetchForCurrentUser).toHaveBeenCalledTimes(3));
  });
});
