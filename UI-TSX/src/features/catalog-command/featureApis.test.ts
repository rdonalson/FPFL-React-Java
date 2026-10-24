import { beforeEach, describe, expect, it, vi } from 'vitest';

const clients = vi.hoisted(() => ({
  item: {
    fetchForCurrentUser: vi.fn(),
    getById: vi.fn(),
    create: vi.fn(),
    update: vi.fn(),
    remove: vi.fn(),
    fetchItemTypes: vi.fn(),
    fetchTimePeriods: vi.fn(),
  },
  initial: { fetchForCurrentUser: vi.fn(), create: vi.fn(), update: vi.fn() },
  itemType: {
    getAll: vi.fn(),
    getById: vi.fn(),
    create: vi.fn(),
    update: vi.fn(),
    delete: vi.fn(),
  },
  timePeriod: {
    getAll: vi.fn(),
    getById: vi.fn(),
    create: vi.fn(),
    update: vi.fn(),
    delete: vi.fn(),
  },
}));
vi.mock('@/api/generated/ItemClient', () => ({ ItemClient: clients.item }));
vi.mock('@/api/generated/InitialAmountClient', () => ({ InitialAmountClient: clients.initial }));
vi.mock('@/api/generated/ItemTypeClient', () => ({ ItemTypeClient: clients.itemType }));
vi.mock('@/api/generated/TimePeriodClient', () => ({ TimePeriodClient: clients.timePeriod }));

import * as itemApi from './transactions/api/itemApi';
import * as initialAmountApi from './transactions/api/initialAmountApi';
import { itemTypeApi } from './admin/itemType/api/itemTypeApi';
import { timePeriodApi } from './admin/timePeriod/api/timePeriodApi';
import { useSessionStore } from '@/app/state/sessionStore';

const ok = <T>(data: T, message = 'ok') => ({ status: 200, message, data });

beforeEach(() => {
  vi.clearAllMocks();
  useSessionStore.setState({ userId: 'user-1' });
});

describe('itemApi', () => {
  it('lists, reads, creates, updates and deletes items', async () => {
    clients.item.fetchForCurrentUser.mockResolvedValue(ok([{ id: 1 }]));
    expect(await itemApi.getItemsForUserAndType('user-1', 1)).toEqual([{ id: 1 }]);
    expect(clients.item.fetchForCurrentUser).toHaveBeenCalledWith(1);

    clients.item.getById.mockResolvedValue(ok({ id: 2 }));
    expect(await itemApi.getItemById(2)).toEqual({ id: 2 });

    clients.item.create.mockResolvedValue(ok({ id: 3 }));
    const payload: any = { amount: 10, ItemType: { Id: 2 } };
    expect(await itemApi.createItem(payload)).toEqual({ id: 3 });
    // fills in what the backend needs
    expect(clients.item.create.mock.calls[0][0]).toMatchObject({ fkItemType: 2, userId: 'user-1' });

    clients.item.update.mockResolvedValue(ok({ id: 3, amount: 20 }));
    expect(await itemApi.updateItem(3, { amount: 20 } as any)).toEqual({ id: 3, amount: 20 });

    clients.item.remove.mockResolvedValue(ok(null, 'Item 3 deleted successfully'));
    await expect(itemApi.deleteItem(3)).resolves.toBeUndefined();
  });

  it('an empty list is fine, a failed save is not', async () => {
    clients.item.fetchForCurrentUser.mockResolvedValue(ok(null));
    expect(await itemApi.getItemsForUserAndType('user-1', 2)).toEqual([]);

    clients.item.create.mockResolvedValue({
      status: 400,
      message: 'Item Name is required',
      data: null,
    });
    await expect(itemApi.createItem({ amount: 1 } as any)).rejects.toThrow('Item Name is required');
  });

  it('passes item types and time periods through', async () => {
    clients.item.fetchItemTypes.mockResolvedValue(ok([{ Id: 1 }]));
    clients.item.fetchTimePeriods.mockResolvedValue(ok([{ Id: 6 }]));
    expect(await itemApi.fetchItemTypes()).toEqual([{ Id: 1 }]);
    expect(await itemApi.fetchTimePeriods()).toEqual([{ Id: 6 }]);
  });
});

describe('initialAmountApi', () => {
  it('returns null when the user has no initial amount yet', async () => {
    clients.initial.fetchForCurrentUser.mockResolvedValue(ok(null));
    expect(await initialAmountApi.fetchInitialAmount()).toBeNull();
  });

  it('reads, creates and updates it', async () => {
    clients.initial.fetchForCurrentUser.mockResolvedValue(ok({ id: 1, amount: 100 }));
    expect(await initialAmountApi.fetchInitialAmount()).toEqual({ id: 1, amount: 100 });

    clients.initial.create.mockResolvedValue(ok({ id: 1, amount: 200 }));
    expect((await initialAmountApi.createInitialAmount(200)).amount).toBe(200);
    expect(clients.initial.create).toHaveBeenCalledWith(200);

    clients.initial.update.mockResolvedValue(ok({ id: 1, amount: 300 }));
    expect((await initialAmountApi.updateInitialAmount(1, 300)).amount).toBe(300);
    expect(clients.initial.update).toHaveBeenCalledWith(1, 300);
  });
});

describe('itemTypeApi and timePeriodApi', () => {
  it('do CRUD and accept an empty delete response', async () => {
    clients.itemType.getAll.mockResolvedValue(ok([{ id: 1, name: 'Credit' }]));
    expect(await itemTypeApi.fetchAll()).toHaveLength(1);
    clients.itemType.getById.mockResolvedValue(ok(null));
    expect(await itemTypeApi.fetchById(9)).toBeNull();
    clients.itemType.create.mockResolvedValue(ok({ id: 4, name: 'Other' }));
    await itemTypeApi.create({ id: 4, name: 'Other' });
    expect(clients.itemType.create).toHaveBeenCalledWith({ id: 4, name: 'Other' });
    clients.itemType.update.mockResolvedValue(ok({ id: 4, name: 'Misc' }));
    await itemTypeApi.update({ id: 4, name: 'Misc' });
    expect(clients.itemType.update).toHaveBeenCalledWith(4, { id: 4, name: 'Misc' });
    clients.itemType.delete.mockResolvedValue({ status: 200, data: undefined });
    await expect(itemTypeApi.remove(4)).resolves.toBeUndefined();
    clients.itemType.delete.mockResolvedValue(undefined);
    await expect(itemTypeApi.remove(4)).rejects.toThrow('No response from server');

    clients.timePeriod.getAll.mockResolvedValue(ok([{ id: 6, name: 'Monthly' }]));
    expect(await timePeriodApi.fetchAll()).toHaveLength(1);
    clients.timePeriod.update.mockResolvedValue(ok({ id: 6, name: 'Monthly' }));
    await timePeriodApi.update({ id: 6, name: 'Monthly' } as any);
    expect(clients.timePeriod.update).toHaveBeenCalledWith(6, { id: 6, name: 'Monthly' });
    clients.timePeriod.delete.mockResolvedValue(undefined);
    await expect(timePeriodApi.remove(6)).rejects.toThrow('No response from server');
  });
});
