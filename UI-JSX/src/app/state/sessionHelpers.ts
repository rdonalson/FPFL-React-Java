// src/app/state/sessionHelpers.ts
import { useSessionStore } from './sessionStore';

/**
 * Return the current session userId (or null).
 * Uses Zustand store getState so it can be called outside React components.
 */
export function getSessionUserId(): string | null {
  try {
    return useSessionStore.getState().userId ?? null;
  } catch {
    return null;
  }
}
