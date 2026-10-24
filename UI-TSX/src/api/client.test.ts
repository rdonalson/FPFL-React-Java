import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import type { InternalAxiosRequestConfig } from 'axios';
import { apiClient } from './client';
import { useSessionStore } from '@/app/state/sessionStore';

let seen: InternalAxiosRequestConfig | null = null;
const fetchSpy = vi.fn(async () => ({}) as Response);

beforeEach(() => {
  seen = null;
  vi.stubGlobal('fetch', fetchSpy);
  vi.spyOn(console, 'error').mockImplementation(() => {});
});

afterEach(() => {
  apiClient.defaults.adapter = undefined;
  useSessionStore.setState({ accessToken: null, userId: null });
  vi.unstubAllGlobals();
  vi.restoreAllMocks();
  fetchSpy.mockClear();
});

/** Answer every request with `status`, recording what was sent. */
function respondWith(status: number, data: unknown) {
  apiClient.defaults.adapter = async config => {
    seen = config;
    const response = { data, status, statusText: String(status), headers: {}, config };
    if (status >= 400) {
      const error: any = new Error(`Request failed with status code ${status}`);
      error.config = config;
      error.response = response;
      error.isAxiosError = true;
      throw error;
    }
    return response;
  };
}

describe('apiClient', () => {
  it('adds the bearer token, user id and a correlation id to every request', async () => {
    useSessionStore.setState({ accessToken: 'jwt-123', userId: 'user-1' });
    respondWith(200, { data: 'ok' });

    const res = await apiClient.get('/items');

    expect(res.data).toEqual({ data: 'ok' });
    expect(seen?.headers['Authorization']).toBe('Bearer jwt-123');
    expect(seen?.headers['X-User-Id']).toBe('user-1');
    expect(seen?.headers['X-Correlation-ID']).toMatch(/^[0-9a-f-]{36}$/);
  });

  it('sends no auth header when signed out', async () => {
    respondWith(200, {});
    await apiClient.get('/item-types');
    expect(seen?.headers['Authorization']).toBeUndefined();
  });

  it("normalizes errors to the API's message and reports them to /client-logs", async () => {
    respondWith(404, { message: 'Item 9 not found' });

    await expect(apiClient.get('/items/9')).rejects.toMatchObject({
      status: 404,
      message: 'Item 9 not found',
      url: '/items/9',
    });

    expect(fetchSpy).toHaveBeenCalledTimes(1);
    const [url, init] = fetchSpy.mock.calls[0] as unknown as [string, RequestInit];
    expect(url).toMatch(/\/client-logs$/);
    expect(JSON.parse(init.body as string)).toMatchObject({ level: 'error', status: 404 });
  });

  it('falls back to the transport message when the server sent none', async () => {
    respondWith(500, null);
    await expect(apiClient.get('/boom')).rejects.toMatchObject({
      status: 500,
      message: 'Request failed with status code 500',
    });
  });
});
