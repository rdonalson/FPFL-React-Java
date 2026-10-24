import { afterEach, describe, expect, it, vi } from 'vitest';
import { act, renderHook } from '@testing-library/react';
import { SMALL_SCREEN_QUERY, useIsSmallScreen } from './useIsSmallScreen';

function mockMatchMedia(initial: boolean) {
  const listeners = new Set<() => void>();
  const mql = {
    matches: initial,
    media: SMALL_SCREEN_QUERY,
    addEventListener: (_: string, fn: () => void) => listeners.add(fn),
    removeEventListener: (_: string, fn: () => void) => listeners.delete(fn),
  };
  const original = window.matchMedia;
  window.matchMedia = vi.fn(() => mql) as any;
  return {
    set(matches: boolean) {
      mql.matches = matches;
      listeners.forEach(fn => fn());
    },
    listeners,
    restore: () => (window.matchMedia = original),
  };
}

let media: ReturnType<typeof mockMatchMedia>;
afterEach(() => media?.restore());

describe('useIsSmallScreen', () => {
  it('reports the current match and follows changes', () => {
    media = mockMatchMedia(false);
    const { result } = renderHook(() => useIsSmallScreen());
    expect(result.current).toBe(false);

    act(() => media.set(true));
    expect(result.current).toBe(true);

    act(() => media.set(false));
    expect(result.current).toBe(false);
  });

  it('starts true on a phone and stops listening on unmount', () => {
    media = mockMatchMedia(true);
    const { result, unmount } = renderHook(() => useIsSmallScreen());
    expect(result.current).toBe(true);
    expect(media.listeners.size).toBe(1);

    unmount();
    expect(media.listeners.size).toBe(0);
  });
});
