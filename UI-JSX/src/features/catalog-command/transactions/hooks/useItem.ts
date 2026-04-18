// hooks/useItem.ts
import { useCallback, useState } from 'react';
import { Item } from '../types/Item';
import * as itemApi from '../api/itemApi';

export function useItem() {
  const [items, setItems] = useState<Item[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function loadForUserAndType(userId: string, itemType: number) {
    setLoading(true);
    setError(null);
    try {
      const data = await itemApi.getItemsForUserAndType(userId, itemType);
      setItems(data);
    } catch (e: any) {
      setError(e.message || 'Failed to load items');
    } finally {
      setLoading(false);
    }
  }

  const create = useCallback(async (payload: Item) => {
    setLoading(true);
    setError(null);
    try {
      const created = await itemApi.createItem(payload);
      setItems(s => [created, ...s]);
      return created;
    } catch (e: any) {
      setError(e.message || 'Create failed');
      throw e;
    } finally {
      setLoading(false);
    }
  }, []);

  const update = useCallback(async (id: number, payload: Item) => {
    setLoading(true);
    setError(null);
    try {
      const updated = await itemApi.updateItem(id, payload);
      setItems(s => s.map(it => (it.id === updated.id ? updated : it)));
      return updated;
    } catch (e: any) {
      setError(e.message || 'Update failed');
      throw e;
    } finally {
      setLoading(false);
    }
  }, []);

  const remove = useCallback(async (id: number) => {
    setLoading(true);
    setError(null);
    try {
      await itemApi.deleteItem(id);
      setItems(s => s.filter(it => it.id !== id));
    } catch (e: any) {
      setError(e.message || 'Delete failed');
      throw e;
    } finally {
      setLoading(false);
    }
  }, []);

  return {
    items,
    loading,
    error,
    loadForUserAndType,
    create,
    update,
    remove,
    setItems,
    setError,
  };
}
