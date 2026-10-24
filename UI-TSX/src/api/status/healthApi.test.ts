import { afterEach, describe, expect, it, vi } from 'vitest';
import { HEALTH_URL, INFO_URL, fetchHealth, fetchInfo } from './healthApi';

afterEach(() => vi.unstubAllGlobals());

const reply = (status: number, body: string) =>
  ({
    ok: status >= 200 && status < 300,
    status,
    statusText: 'x',
    text: async () => body,
  }) as Response;

describe('healthApi', () => {
  it('points at the actuator', () => {
    expect(HEALTH_URL).toMatch(/\/actuator\/health$/);
    expect(INFO_URL).toMatch(/\/actuator\/info$/);
  });

  it('reports an UP API', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn(async () => reply(200, '{"status":"UP"}')),
    );
    const result = await fetchHealth();

    expect(result.ok).toBe(true);
    expect(result.httpStatus).toBe(200);
    expect(result.body?.status).toBe('UP');
    expect(result.error).toBeUndefined();
  });

  it('still reads the body of a 503 (a component is DOWN)', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn(async () => reply(503, '{"status":"DOWN"}')),
    );
    const result = await fetchHealth();

    expect(result.ok).toBe(false);
    expect(result.body?.status).toBe('DOWN');
    expect(result.error).toMatch(/HTTP 503/);
  });

  it('explains auth, missing endpoints and non-JSON answers', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn(async () => reply(401, '{}')),
    );
    expect((await fetchInfo()).hint).toMatch(/Spring Security/);

    vi.stubGlobal(
      'fetch',
      vi.fn(async () => reply(404, '{}')),
    );
    expect((await fetchInfo()).hint).toMatch(/exposure\.include/);

    vi.stubGlobal(
      'fetch',
      vi.fn(async () => reply(200, '<html>')),
    );
    const html = await fetchInfo();
    expect(html.ok).toBe(false);
    expect(html.hint).toMatch(/wasn't JSON/);
  });

  it('tells "nothing answered" apart from "blocked by CORS"', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn(async () => Promise.reject(new TypeError('Failed to fetch'))),
    );
    const down = await fetchHealth();
    expect(down.httpStatus).toBe(0);
    expect(down.blocked).toBe(false);
    expect(down.hint).toMatch(/Nothing answered/);

    // The no-cors probe resolves: something is there, the browser just won't let us read it
    vi.stubGlobal(
      'fetch',
      vi.fn(async (_url: string, init?: RequestInit) =>
        init?.mode === 'no-cors'
          ? ({} as Response)
          : Promise.reject(new TypeError('Failed to fetch')),
      ),
    );
    const blocked = await fetchHealth();
    expect(blocked.blocked).toBe(true);
    expect(blocked.error).toBe('Blocked by the browser (CORS)');
  });
});
