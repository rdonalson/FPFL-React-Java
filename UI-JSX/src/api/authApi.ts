// src/api/authApi.ts
import { apiClient } from './client';

export async function loginApi(username: string, password: string) {
  const data = await apiClient.post('/auth/login', { username, password });
  return data; // { userId, accessToken, refreshToken? }
}

export async function meApi() {
  const data = await apiClient.get('/auth/me');
  return data; // { userId, email, roles }
}

export async function refreshApi() {
  const data = await apiClient.post('/auth/refresh');
  return data; // { accessToken, userId }
}
