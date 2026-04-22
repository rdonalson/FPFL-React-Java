// src/api/authApi.ts
import { apiClient } from './client';

export async function loginApi(username: string, password: string) {
  const data = await apiClient.post('/auth/login', { username, password });
  return data; // if apiClient is typed to return response.data, this is already payload
}

export async function meApi() {
  const data = await apiClient.get('/auth/me');
  // Normalize: if axios returned an AxiosResponse, return .data; otherwise return as-is
  return data && (data as any).data ? (data as any).data : data;
}

export async function refreshApi() {
  const data = await apiClient.post('/auth/refresh');
  return data && (data as any).data ? (data as any).data : data;
}
