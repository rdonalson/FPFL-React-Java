import { describe, expect, it } from 'vitest';
import { unwrap } from './responseHelpers';

const response = <T>(data: T, message = 'ok', status = 200) => ({
  status,
  message,
  data,
  correlationId: '',
  timestamp: '',
});

describe('unwrap', () => {
  it('returns the data of an ApiResponse', () => {
    expect(unwrap(response([1, 2]), [])).toEqual([1, 2]);
  });

  it('returns already-unwrapped values as they are', () => {
    expect(unwrap({ id: 1, name: 'x' } as any, null)).toEqual({ id: 1, name: 'x' });
  });

  it('falls back when there is no response or no data', () => {
    expect(unwrap(null, [])).toEqual([]);
    expect(unwrap(undefined)).toBeNull();
    expect(unwrap(response(null), 'fallback')).toBe('fallback');
  });

  it('throws when asked to and nothing came back', () => {
    expect(() => unwrap(null, null, true)).toThrow('No response from API');
    expect(() => unwrap(response(null, 'Item 4 not found', 404), null, true)).toThrow(
      'Item 4 not found',
    );
  });

  it('treats a successful delete as success even without data', () => {
    expect(unwrap(response(null, 'Item 7 deleted successfully'), undefined, true)).toBe(true);
    expect(unwrap(response(null, 'Item 7 deleted successfully'), 'done')).toBe('done');
  });
});
