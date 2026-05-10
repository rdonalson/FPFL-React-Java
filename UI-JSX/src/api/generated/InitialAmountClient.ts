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
 * Helper to safely retrieve the current session userID.
 */
function getSessionUserId(): string {
  const userID = useSessionStore.getState().userID;
  if (!userID) {
    throw new Error('UserId missing from session');
  }
  return userID;
}

export const InitialAmountClient = {
  /**
   * GET /initial-amount/{userID}
   */
  async fetchForCurrentUser(): Promise<ApiResponse<InitialAmountResponse>> {
    const userID = getSessionUserId();

    const res = await apiClient.get<ApiResponse<InitialAmountResponse>>(`${BASE_IA}/${userID}`);

    return res.data;
  },

  /**
   * POST /initial-amount
   * Body: { userID, amount }
   */
  async create(amount: number): Promise<ApiResponse<InitialAmountResponse>> {
    const userID = getSessionUserId();

    const payload: InitialAmountRequest = { userID, amount };

    const res = await apiClient.post<ApiResponse<InitialAmountResponse>>(BASE_IA, payload);

    return res.data;
  },

  /**
   * PUT /initial-amount/{id}
   * Body: { userID, amount }
   */
  async update(id: number, amount: number): Promise<ApiResponse<InitialAmountResponse>> {
    const userID = getSessionUserId();

    const payload: InitialAmountRequest = { userID, amount };

    const res = await apiClient.put<ApiResponse<InitialAmountResponse>>(
      `${BASE_IA}/${id}`,
      payload,
    );

    return res.data;
  },
};
