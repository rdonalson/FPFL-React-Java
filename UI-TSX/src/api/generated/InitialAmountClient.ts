// src/api/generated/InitialAmountClient.ts
import { apiClient } from '@/api/client';
import { ApiResponse } from '@/api/models/ApiResponse';
import {
  InitialAmountRequest,
  InitialAmountResponse,
} from '@/features/catalog-command/transactions/types/InitialAmount';
import { useSessionStore } from '@/app/state/sessionStore';

const BASE_IA = '/initial-amount';

/**
 * Helper to safely retrieve the current session userId.
 */
function getSessionUserId(): string {
  const userId = useSessionStore.getState().userId;
  if (!userId) {
    throw new Error('UserId missing from session');
  }
  return userId;
}

export const InitialAmountClient = {
  /**
   * GET /initial-amount/{userId}
   */
  async fetchForCurrentUser(): Promise<ApiResponse<InitialAmountResponse>> {
    const userId = getSessionUserId();

    const res = await apiClient.get<ApiResponse<InitialAmountResponse>>(`${BASE_IA}/${userId}`);

    return res.data;
  },

  /**
   * POST /initial-amount
   * Body: { userId, amount }
   */
  async create(amount: number): Promise<ApiResponse<InitialAmountResponse>> {
    const userId = getSessionUserId();

    const payload: InitialAmountRequest = { userId, amount };

    const res = await apiClient.post<ApiResponse<InitialAmountResponse>>(BASE_IA, payload);

    return res.data;
  },

  /**
   * PUT /initial-amount/{id}
   * Body: { userId, amount }
   */
  async update(id: number, amount: number): Promise<ApiResponse<InitialAmountResponse>> {
    const userId = getSessionUserId();

    const payload: InitialAmountRequest = { userId, amount };

    const res = await apiClient.put<ApiResponse<InitialAmountResponse>>(
      `${BASE_IA}/${id}`,
      payload,
    );

    return res.data;
  },
};
