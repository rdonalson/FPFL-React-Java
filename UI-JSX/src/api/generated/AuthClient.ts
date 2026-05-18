// src/api/generated/AuthClient.ts
import { apiClient } from '@/api/client';
import { callAndParse } from '@/api/utils/apiParse';

import type { ApiResponse } from '@/api/models/ApiResponse';
import type { LoginRequest } from '@/app/auth/types/LoginRequest';
import type { AuthResponse } from '@/app/auth/types/AuthResponse';
import { RegisterRequest } from '@/app/auth/types/RegisterRequest';
import { RefreshResponse } from '@/app/auth/types/RefreshResponse';

const BASE = '/auth';

export const AuthClient = {
  async login(payload: LoginRequest): Promise<ApiResponse<AuthResponse>> {
    return callAndParse<AuthResponse>(() =>
      apiClient.post<ApiResponse<AuthResponse>>(`${BASE}/login`, payload),
    );
  },

  async register(payload: RegisterRequest): Promise<ApiResponse<AuthResponse>> {
    return callAndParse<AuthResponse>(() =>
      apiClient.post<ApiResponse<AuthResponse>>(`${BASE}/register`, payload),
    );
  },

  async refresh(refreshToken: string): Promise<ApiResponse<RefreshResponse>> {
    return callAndParse<RefreshResponse>(() =>
      apiClient.post<ApiResponse<RefreshResponse>>(`${BASE}/refresh`, {
        refreshToken,
      }),
    );
  },
};
