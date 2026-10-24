// src/api/generated/DebtClient.ts
import { apiClient } from '@/api/client';
import type { ApiResponse } from '@/api/models/ApiResponse';
import { callAndParse } from '@/api/utils/apiParse';
import type { Debt, DebtRequest } from '@/features/debt-paydown/types/Debt';

const BASE_DEBTS = '/debts';

/** Debt Paydown's tracked debts. The API takes the owner from the access token. */
export const DebtClient = {
  async list(): Promise<ApiResponse<Debt[]>> {
    return callAndParse<Debt[]>(() => apiClient.get<ApiResponse<Debt[]>>(BASE_DEBTS));
  },

  async create(payload: DebtRequest): Promise<ApiResponse<Debt>> {
    return callAndParse<Debt>(() => apiClient.post<ApiResponse<Debt>>(BASE_DEBTS, payload));
  },

  async update(id: number, payload: DebtRequest): Promise<ApiResponse<Debt>> {
    return callAndParse<Debt>(() =>
      apiClient.put<ApiResponse<Debt>>(`${BASE_DEBTS}/${id}`, payload),
    );
  },

  async remove(id: number): Promise<ApiResponse<void>> {
    return callAndParse<void>(() => apiClient.delete<ApiResponse<void>>(`${BASE_DEBTS}/${id}`));
  },
};
