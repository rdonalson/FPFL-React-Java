// src/api/authApi.ts
import { apiClient } from './client';

export async function loginApi(email: string, password: string) {
  const res = await apiClient.post('/auth/login', { email, password });
  const data = res && (res as any).data ? (res as any).data : res;

  return {
    token: data.accessToken,
    refreshToken: data.refreshToken,
    userId: data.userID,
    email: data.email,
    roles: data.roles,
    raw: data,
  };
}

export async function meApi(): Promise<any> {
  const res = await apiClient.get('/auth/me');
  return res && (res as any).data ? (res as any).data : res;
}

export async function refreshApi(): Promise<any> {
  const res = await apiClient.post('/auth/refresh');
  return res && (res as any).data ? (res as any).data : res;
}
