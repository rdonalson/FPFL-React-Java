// src/app/auth/api/authApi.ts
import { apiClient } from '@/api/client';
import { normalizeAuthResponse } from '@/api/utils/normalizeAuthResponse';

export async function loginApi(email: string, password: string) {
  const res = await apiClient.post('/auth/login', { email, password });
  const data = res.data;

  // Normalize backend → session shape
  return normalizeAuthResponse(data);
}

export async function registerApi(first: string, last: string, email: string, password: string) {
  const res = await apiClient.post('/auth/register', {
    first,
    last,
    email,
    password,
  });
  const data = res.data;

  return normalizeAuthResponse(data);
}

// Optional: only keep if your backend supports refresh tokens
export async function refreshApi() {
  const res = await apiClient.post('/auth/refresh');
  const data = res.data;

  return normalizeAuthResponse(data);
}
