import { beforeEach, describe, expect, it, vi } from 'vitest';

const http = vi.hoisted(() => {
  const ok = (data: unknown) => Promise.resolve({ status: 200, config: {}, data: { data } });
  return {
    get: vi.fn((url: string) => ok({ url })),
    post: vi.fn((url: string, body?: unknown) => ok({ url, body })),
    put: vi.fn((url: string, body?: unknown) => ok({ url, body })),
    delete: vi.fn((url: string) => ok({ url })),
  };
});
vi.mock('@/api/client', () => ({ apiClient: http }));

import { AuthClient } from './AuthClient';
import { DebtClient } from './DebtClient';
import { DisplayClient } from './DisplayClient';
import { InitialAmountClient } from './InitialAmountClient';
import { ItemClient } from './ItemClient';
import { ItemTypeClient } from './ItemTypeClient';
import { TimePeriodClient } from './TimePeriodClient';
import { useSessionStore } from '@/app/state/sessionStore';

beforeEach(() => {
  vi.clearAllMocks();
  useSessionStore.setState({ userId: 'user-1' });
});

describe('AuthClient', () => {
  it('posts to the /auth endpoints', async () => {
    await AuthClient.login({ email: 'a@b.c', password: 'pw' });
    await AuthClient.register({ first: 'R', last: 'D', email: 'a@b.c', password: 'pw' } as any);
    await AuthClient.refresh('refresh-token');
    await AuthClient.changePassword({ currentPassword: 'a', newPassword: 'b' } as any);

    expect(http.post.mock.calls.map(c => c[0])).toEqual([
      '/auth/login',
      '/auth/register',
      '/auth/refresh',
      '/auth/change-password',
    ]);
    expect(http.post.mock.calls[2][1]).toEqual({ refreshToken: 'refresh-token' });
  });
});

describe('ItemClient', () => {
  it("reads the signed-in user's items by type and does CRUD by id", async () => {
    const res = await ItemClient.fetchForCurrentUser(2);
    expect(res.data).toEqual({ url: '/items/user-1/2' });

    await ItemClient.getById(5);
    await ItemClient.create({ amount: 1 } as any);
    await ItemClient.update(5, { amount: 2 } as any);
    await ItemClient.remove(5);
    await ItemClient.fetchItemTypes();
    await ItemClient.fetchTimePeriods();

    expect(http.get.mock.calls.map(c => c[0])).toEqual([
      '/items/user-1/2',
      '/items/5',
      '/item-types',
      '/time-periods',
    ]);
    expect(http.post).toHaveBeenCalledWith('/items', { amount: 1 });
    expect(http.put).toHaveBeenCalledWith('/items/5', { amount: 2 });
    expect(http.delete).toHaveBeenCalledWith('/items/5');
  });
});

describe('InitialAmountClient', () => {
  it('always sends the session user', async () => {
    await InitialAmountClient.fetchForCurrentUser();
    await InitialAmountClient.create(250);
    await InitialAmountClient.update(3, 300);

    expect(http.get).toHaveBeenCalledWith('/initial-amount/user-1');
    expect(http.post).toHaveBeenCalledWith('/initial-amount', { userId: 'user-1', amount: 250 });
    expect(http.put).toHaveBeenCalledWith('/initial-amount/3', { userId: 'user-1', amount: 300 });
  });

  it('refuses to call without a signed-in user', async () => {
    useSessionStore.setState({ userId: null });
    await expect(InitialAmountClient.fetchForCurrentUser()).rejects.toThrow(
      'UserId missing from session',
    );
    expect(http.get).not.toHaveBeenCalled();
  });
});

describe('admin catalog clients', () => {
  it('ItemTypeClient and TimePeriodClient hit their own resources', async () => {
    await ItemTypeClient.getAll();
    await ItemTypeClient.getById(1);
    await ItemTypeClient.create({ id: 4, name: 'Other' });
    await ItemTypeClient.update(4, { id: 4, name: 'Misc' });
    await ItemTypeClient.delete(4);

    await TimePeriodClient.getAll();
    await TimePeriodClient.getById(6);
    await TimePeriodClient.create({ Name: 'Hourly' });
    await TimePeriodClient.update(11, { Name: 'Hourly' });
    await TimePeriodClient.delete(11);

    expect(http.get.mock.calls.map(c => c[0])).toEqual([
      '/item-types',
      '/item-types/1',
      '/time-periods',
      '/time-periods/6',
    ]);
    expect(http.post.mock.calls.map(c => c[0])).toEqual(['/item-types', '/time-periods']);
    expect(http.put.mock.calls.map(c => c[0])).toEqual(['/item-types/4', '/time-periods/11']);
    expect(http.delete.mock.calls.map(c => c[0])).toEqual(['/item-types/4', '/time-periods/11']);
  });
});

describe('DisplayClient and DebtClient', () => {
  it('post the ledger and paydown requests and manage debts', async () => {
    const body = {
      userId: 'user-1',
      ledgerStartDate: '2026-10-01',
      ledgerEndDate: '2026-12-31',
      includeGrouping: false,
    };
    await DisplayClient.fetchLedger(body);
    await DisplayClient.fetchPaydown(body);
    expect(http.post).toHaveBeenNthCalledWith(1, '/display/ledger', body);
    expect(http.post).toHaveBeenNthCalledWith(2, '/display/paydown', body);

    await DebtClient.list();
    await DebtClient.create({ itemId: 1 } as any);
    await DebtClient.update(2, { itemId: 1 } as any);
    await DebtClient.remove(2);
    expect(http.get).toHaveBeenCalledWith('/debts');
    expect(http.post).toHaveBeenLastCalledWith('/debts', { itemId: 1 });
    expect(http.put).toHaveBeenCalledWith('/debts/2', { itemId: 1 });
    expect(http.delete).toHaveBeenCalledWith('/debts/2');
  });
});
