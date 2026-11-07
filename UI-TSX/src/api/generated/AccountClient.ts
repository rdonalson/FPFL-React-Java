// src/api/generated/AccountClient.ts
import { apiClient } from '@/api/client';
import type { ApiResponse } from '@/api/models/ApiResponse';
import { callAndParse } from '@/api/utils/apiParse';
import type {
  Account,
  AccountRequest,
  Purchase,
  PurchaseRequest,
} from '@/features/accounts/types/Account';

const BASE_ACCOUNTS = '/accounts';

/** Cards & Loans. The API takes the owner from the access token. */
export const AccountClient = {
  async list(): Promise<ApiResponse<Account[]>> {
    return callAndParse<Account[]>(() => apiClient.get<ApiResponse<Account[]>>(BASE_ACCOUNTS));
  },

  async get(id: number): Promise<ApiResponse<Account>> {
    return callAndParse<Account>(() =>
      apiClient.get<ApiResponse<Account>>(`${BASE_ACCOUNTS}/${id}`),
    );
  },

  async create(payload: AccountRequest): Promise<ApiResponse<Account>> {
    return callAndParse<Account>(() =>
      apiClient.post<ApiResponse<Account>>(BASE_ACCOUNTS, payload),
    );
  },

  async update(id: number, payload: AccountRequest): Promise<ApiResponse<Account>> {
    return callAndParse<Account>(() =>
      apiClient.put<ApiResponse<Account>>(`${BASE_ACCOUNTS}/${id}`, payload),
    );
  },

  async remove(id: number): Promise<ApiResponse<void>> {
    return callAndParse<void>(() => apiClient.delete<ApiResponse<void>>(`${BASE_ACCOUNTS}/${id}`));
  },

  async addPurchase(accountId: number, payload: PurchaseRequest): Promise<ApiResponse<Purchase>> {
    return callAndParse<Purchase>(() =>
      apiClient.post<ApiResponse<Purchase>>(`${BASE_ACCOUNTS}/${accountId}/purchases`, payload),
    );
  },

  async updatePurchase(
    accountId: number,
    purchaseId: number,
    payload: PurchaseRequest,
  ): Promise<ApiResponse<Purchase>> {
    return callAndParse<Purchase>(() =>
      apiClient.put<ApiResponse<Purchase>>(
        `${BASE_ACCOUNTS}/${accountId}/purchases/${purchaseId}`,
        payload,
      ),
    );
  },

  async removePurchase(accountId: number, purchaseId: number): Promise<ApiResponse<void>> {
    return callAndParse<void>(() =>
      apiClient.delete<ApiResponse<void>>(`${BASE_ACCOUNTS}/${accountId}/purchases/${purchaseId}`),
    );
  },
};
