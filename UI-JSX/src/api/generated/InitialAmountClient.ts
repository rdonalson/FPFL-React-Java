import { apiClient } from '@/api/client';
import { ApiResponse } from '@/api/models/ApiResponse';
import {
  InitialAmountRequest,
  InitialAmountResponse,
} from '@/features/catalog-command/transactions/types/InitialAmount';
import { getSessionUserId } from '@/api/utils/userId';

const BASE_IA = '/initial-amount';

export const InitialAmountClient = {
  async fetchForCurrentUser(): Promise<ApiResponse<InitialAmountResponse>> {
    const userId = getSessionUserId();
    if (!userId) throw new Error('UserId missing from session');

    const res = await apiClient.get<ApiResponse<InitialAmountResponse>>(`${BASE_IA}/${userId}`);

    return res.data; // ApiResponse<InitialAmountResponse>
  },

  async create(amount: number): Promise<ApiResponse<InitialAmountResponse>> {
    const userId = getSessionUserId();
    if (!userId) throw new Error('UserId missing from session');

    const payload: InitialAmountRequest = { userId, amount };

    const res = await apiClient.post<ApiResponse<InitialAmountResponse>>(BASE_IA, payload);

    return res.data;
  },

  async update(id: number, amount: number): Promise<ApiResponse<InitialAmountResponse>> {
    const userId = getSessionUserId();
    if (!userId) throw new Error('UserId missing from session');

    const payload: InitialAmountRequest = { userId, amount };

    const res = await apiClient.put<ApiResponse<InitialAmountResponse>>(
      `${BASE_IA}/${id}`,
      payload,
    );

    return res.data;
  },
};
