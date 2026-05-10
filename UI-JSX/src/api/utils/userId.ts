import { useSessionStore } from '@/app/state/sessionStore';

const DEFAULT_USER_KEY = 'userID';

/**
 * Resolve the current session user id.
 * Priority:
 *  1) Zustand session store (runtime, preferred)
 *  2) sessionStorage (page reload fallback)
 *  3) empty string (guest fallback)
 */
export function getSessionUserId(): string {
  try {
    const fromStore = useSessionStore.getState?.().userID;
    if (fromStore) return fromStore;
  } catch {
    // ignore if Zustand isn't available
  }

  try {
    const fromStorage =
      typeof window !== 'undefined' ? sessionStorage.getItem(DEFAULT_USER_KEY) : null;
    return fromStorage ?? '';
  } catch {
    // sessionStorage may throw in some environments; fall back to empty string
    return '';
  }
}
