// src/api/authApi.ts
import { apiClient } from './client';

type LoginResponse = {
  token?: string;
  userId?: string;
  [key: string]: any;
};

export async function loginApi(username: string, password: string): Promise<LoginResponse> {
  const res = await apiClient.post('/auth/login', { username, password });
  // apiClient's response interceptor returns response.data, but be defensive:
  return res && (res as any).data ? (res as any).data : res;
}

export async function meApi(): Promise<any> {
  const res = await apiClient.get('/auth/me');
  return res && (res as any).data ? (res as any).data : res;
}

export async function refreshApi(): Promise<any> {
  const res = await apiClient.post('/auth/refresh');
  return res && (res as any).data ? (res as any).data : res;
}
