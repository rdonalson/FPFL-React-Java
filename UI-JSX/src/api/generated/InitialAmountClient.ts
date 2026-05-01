// src/features/catalog-command/transactions/clients/InitialAmountClient.ts
import { apiClient } from '@/api/client';
import { ApiResponse } from '@/api/models/ApiResponse';
import {
  InitialAmountRequest,
  InitialAmountResponse,
} from '@/features/catalog-command/transactions/types/InitialAmount';
import { getSessionUserId } from '@/api/utils/userId';

const BASE_IA = '/initial-amount';

export const InitialAmountClient = {
  /**
   * GET /initial-amount/{userId}
   * Returns ApiResponse<InitialAmountResponse | null>
   */
  async fetchForCurrentUser(): Promise<ApiResponse<InitialAmountResponse | null>> {
    const userId = getSessionUserId();
    if (!userId) throw new Error('UserId missing from session');

    const res = await apiClient.get<ApiResponse<InitialAmountResponse | null>>(
      `${BASE_IA}/${userId}`,
    );

    // AxiosResponse<ApiResponse<...>> -> ApiResponse<...>
    return res.data;
  },

  /**
   * POST /initial-amount
   * body: { userId, amount }
   * Returns ApiResponse<InitialAmountResponse>
   */
  async create(amount: number): Promise<ApiResponse<InitialAmountResponse>> {
    const userId = getSessionUserId();
    if (!userId) throw new Error('UserId missing from session');

    const payload: InitialAmountRequest = {
      userId,
      amount,
    };

    const created = await apiClient.post<ApiResponse<InitialAmountResponse>>(`${BASE_IA}`, payload);

    return created.data;
  },

  /**
   * PUT /initial-amount/{id}
   * body: { userId, amount }
   * Returns ApiResponse<InitialAmountResponse>
   */
  async update(id: number, amount: number): Promise<ApiResponse<InitialAmountResponse>> {
    const userId = getSessionUserId();
    if (!userId) throw new Error('UserId missing from session');

    const payload: InitialAmountRequest = {
      userId,
      amount,
    };

    const updated = await apiClient.put<ApiResponse<InitialAmountResponse>>(
      `${BASE_IA}/${id}`,
      payload,
    );

    return updated.data;
  },
};
