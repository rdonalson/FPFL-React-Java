import { describe, expect, it } from 'vitest';
import { callAndParse, parseApi } from './apiParse';

describe('parseApi', () => {
  it('unwraps an axios response to its ApiResponse body', async () => {
    const body = { status: 200, message: 'ok', data: [1, 2] };
    expect(await parseApi({ status: 200, config: {}, data: body })).toBe(body);
  });

  it('passes an ApiResponse through untouched', async () => {
    const body = { status: 201, message: 'created', data: { id: 1 } };
    expect(await parseApi(body)).toBe(body);
  });

  it('wraps raw payloads and handles nothing at all', async () => {
    expect(await parseApi([1, 2, 3])).toEqual({ data: [1, 2, 3] });
    expect(await parseApi('text')).toEqual({ data: 'text' });
    expect(await parseApi(null)).toEqual({ data: undefined });
    expect(await parseApi(undefined)).toEqual({ data: undefined });
  });
});

describe('callAndParse', () => {
  it('returns the parsed response', async () => {
    const res = await callAndParse(async () => ({ status: 200, config: {}, data: { data: 'x' } }));
    expect(res).toEqual({ data: 'x' });
  });

  it("rethrows the client's normalized error as an Error with its message", async () => {
    const call = () =>
      callAndParse(async () => {
        throw { status: 409, message: 'Item already exists' };
      });
    await expect(call()).rejects.toThrow('Item already exists');
  });

  it('rethrows non-object errors as they are', async () => {
    await expect(
      callAndParse(async () => {
        throw 'boom';
      }),
    ).rejects.toBe('boom');
  });
});
