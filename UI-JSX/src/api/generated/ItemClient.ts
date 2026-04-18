// api/itemClient.ts
import {
  Item,
  ItemTypeDto,
  TimePeriodDto,
} from '@/features/catalog-command/transactions/types/Item';

const BASE = 'http://localhost:9000';
const ITEMS = `${BASE}/items`;
const ITEM_TYPES = `${BASE}/item-types`;
const TIME_PERIODS = `${BASE}/time-periods`;

export type ApiResponse<T> = {
  correlationId?: string;
  data?: T;
  message?: string;
  status?: number;
  timestamp?: string;
};

async function parseApi<T>(res: Response): Promise<ApiResponse<T>> {
  const text = await res.text();
  if (!res.ok) {
    try {
      const parsed = JSON.parse(text);
      throw new Error(parsed?.message ?? JSON.stringify(parsed) ?? res.statusText);
    } catch {
      throw new Error(text || res.statusText);
    }
  }
  try {
    return JSON.parse(text) as ApiResponse<T>;
  } catch {
    return { data: undefined } as ApiResponse<T>;
  }
}

/**
 * Items endpoints
 */
export const itemClient = {
  /**
   * Get items for a user and itemType (returns array in ApiResponse.data)
   */
  async getByUserAndType(userId: string, itemType: number): Promise<Item[]> {
    const url = `${ITEMS}/${encodeURIComponent(userId)}/${itemType}`;
    const res = await fetch(url, { method: 'GET', headers: { Accept: 'application/json' } });
    const api = await parseApi<Item[]>(res);
    return api.data ?? [];
  },

  /**
   * Get single item by id (assumes backend supports GET /items/{id})
   */
  async getById(id: number): Promise<Item | null> {
    const url = `${ITEMS}/${id}`;
    const res = await fetch(url, { method: 'GET', headers: { Accept: 'application/json' } });
    const api = await parseApi<Item>(res);
    return api.data ?? null;
  },

  /**
   * Create item (POST /items)
   * Ensure payload includes fkItemType and fkPeriod as required by backend.
   */
  async create(payload: Item): Promise<Item> {
    const res = await fetch(ITEMS, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', Accept: 'application/json' },
      body: JSON.stringify(payload),
    });
    const api = await parseApi<Item>(res);
    if (!api.data) throw new Error(api.message ?? 'No data returned from create');
    return api.data;
  },

  /**
   * Update item (PUT /items/{id})
   */
  async update(id: number, payload: Item): Promise<Item> {
    const res = await fetch(`${ITEMS}/${id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json', Accept: 'application/json' },
      body: JSON.stringify(payload),
    });
    const api = await parseApi<Item>(res);
    if (!api.data) throw new Error(api.message ?? 'No data returned from update');
    return api.data;
  },

  /**
   * Delete item (DELETE /items/{id})
   */
  async remove(id: number): Promise<void> {
    const res = await fetch(`${ITEMS}/${id}`, {
      method: 'DELETE',
      headers: { Accept: 'application/json' },
    });
    if (!res.ok) {
      const text = await res.text();
      throw new Error(text || res.statusText);
    }
  },

  /**
   * Fetch ItemType list (used to populate dropdowns)
   */
  async fetchItemTypes(): Promise<ItemTypeDto[]> {
    const res = await fetch(ITEM_TYPES, { method: 'GET', headers: { Accept: 'application/json' } });
    const api = await parseApi<ItemTypeDto[]>(res);
    return api.data ?? [];
  },

  /**
   * Fetch TimePeriod list (used to populate dropdowns)
   */
  async fetchTimePeriods(): Promise<TimePeriodDto[]> {
    const res = await fetch(TIME_PERIODS, {
      method: 'GET',
      headers: { Accept: 'application/json' },
    });
    const api = await parseApi<TimePeriodDto[]>(res);
    return api.data ?? [];
  },
};
