// src/features/catalog-command/transactions/clients/InitialAmountClient.ts
import { apiClient } from '@/api/client';
import {
  InitialAmountRequest,
  InitialAmountResponse,
} from '@/features/catalog-command/transactions/types/InitialAmount';
import { getSessionUserId } from '@/api/utils/userId';
import { todayLocalIsoDate } from '@/api/utils/currentDate';

const BASE_IA = '/initial-amount';
const INITIAL_AMOUNT_NAME = 'IA';
const INITIAL_AMOUNT_TYPE = 3;

/**
 * initialAmountClient
 * - Uses the shared Axios apiClient instance (src/api/client.ts)
 * - Uses simple REST endpoints:
 *    GET  /items?userId=...&fkItemType=...
 *    POST /items
 *    PUT  /items/{id}
 *
 * Note: these paths match the generated ItemClient conventions in the backend.
 * If your backend uses different routes, adjust the URLs accordingly.
 */
export const InitialAmountClient = {
  async fetchForCurrentUser(): Promise<InitialAmountResponse | null> {
    const userId = getSessionUserId();
    if (!userId) throw new Error('UserId missing from session');

    const res = await apiClient.get<InitialAmountResponse | null>(`${BASE_IA}`, {
      params: { userId, fkItemType: INITIAL_AMOUNT_TYPE },
    });

    // apiClient response interceptor returns response.data already.
    // Expect an array (possibly empty) or null; return first item or null.
    //const arr = Array.isArray(res) ? (res as InitialAmountResponse) : null;
    return res  (res as InitialAmountResponse) : null;
  },

  async create(amount: number | 0): Promise<InitialAmountResponse> {
    const userId = getSessionUserId();
    if (!userId) throw new Error('UserId missing from session');

    const payload: InitialAmountRequest = {
      userId,
      name: INITIAL_AMOUNT_NAME,
      amount,
      fkItemType: INITIAL_AMOUNT_TYPE,
      beginDate: todayLocalIsoDate(),
    };

    const created = await apiClient.post<InitialAmountResponse>(`${BASE_IA}`, payload);
    return created;
  },

  async update(id: number, amount: number | 0): Promise<InitialAmountResponse> {
    const userId = getSessionUserId();
    if (!userId) throw new Error('UserId missing from session');

    const payload: InitialAmountRequest = {
      userId,
      name: INITIAL_AMOUNT_NAME,
      amount,
      fkItemType: INITIAL_AMOUNT_TYPE,
      beginDate: todayLocalIsoDate(),
    };

    const updated = await apiClient.put<InitialAmountResponse>(`${BASE_IA}/${id}`, payload);
    return updated as unknown as InitialAmountResponse;
  },
};
