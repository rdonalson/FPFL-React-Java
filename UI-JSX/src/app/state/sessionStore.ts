// src/app/state/sessionStore.ts
import { create } from 'zustand';

export interface SessionObject {
  accessToken: string | null;
  refreshToken: string | null;
  id: number | null;
  userId: string | null;
  email: string | null;
  first: string | null;
  last: string | null;
  roles: string[] | null;
  raw: any | null;
}

export interface SessionState {
  // Individual fields (for convenience)
  accessToken: string | null;
  refreshToken: string | null;
  id: number | null;
  userId: string | null;
  email: string | null;
  first: string | null;
  last: string | null;
  roles: string[] | null;
  raw: any | null;

  // Derived state
  isAuthenticated: boolean;

  // NEW: full session object
  session: SessionObject | null;

  setSession: (session: {
    accessToken: string;
    refreshToken: string | null;
    id: number;
    userId: string;
    email: string;
    first: string;
    last: string;
    roles: string[];
    raw: any;
  }) => void;

  clearSession: () => void;
}

export const useSessionStore = create<SessionState>(set => {
  // Hydrate from sessionStorage once
  const hydrated: SessionObject = {
    accessToken: sessionStorage.getItem('accessToken'),
    refreshToken: sessionStorage.getItem('refreshToken'),
    id: Number(sessionStorage.getItem('id')) || null,
    userId: sessionStorage.getItem('userId'),
    email: sessionStorage.getItem('email'),
    first: sessionStorage.getItem('first'),
    last: sessionStorage.getItem('last'),
    roles: JSON.parse(sessionStorage.getItem('roles') || 'null'),
    raw: JSON.parse(sessionStorage.getItem('raw') || 'null'),
  };

  return {
    // Individual fields
    accessToken: hydrated.accessToken,
    refreshToken: hydrated.refreshToken,
    id: hydrated.id,
    userId: hydrated.userId,
    email: hydrated.email,
    first: hydrated.first,
    last: hydrated.last,
    roles: hydrated.roles,
    raw: hydrated.raw,

    // Derived
    isAuthenticated: Boolean(hydrated.accessToken),

    // NEW: full session object
    session: hydrated,

    // Set session after login or refresh
    setSession: session => {
      // Persist to sessionStorage
      sessionStorage.setItem('accessToken', session.accessToken);
      sessionStorage.setItem('refreshToken', session.refreshToken ?? '');
      sessionStorage.setItem('id', String(session.id));
      sessionStorage.setItem('userId', session.userId);
      sessionStorage.setItem('email', session.email);
      sessionStorage.setItem('first', session.first);
      sessionStorage.setItem('last', session.last);
      sessionStorage.setItem('roles', JSON.stringify(session.roles));
      sessionStorage.setItem('raw', JSON.stringify(session.raw));

      const sessionObj: SessionObject = {
        accessToken: session.accessToken,
        refreshToken: session.refreshToken,
        id: session.id,
        userId: session.userId,
        email: session.email,
        first: session.first,
        last: session.last,
        roles: session.roles,
        raw: session.raw,
      };

      // Update Zustand state
      set({
        ...sessionObj,
        isAuthenticated: true,
        session: sessionObj,
      });
    },

    // Clear session on logout
    clearSession: () => {
      // Remove everything from sessionStorage
      sessionStorage.removeItem('accessToken');
      sessionStorage.removeItem('refreshToken');
      sessionStorage.removeItem('id');
      sessionStorage.removeItem('userId');
      sessionStorage.removeItem('email');
      sessionStorage.removeItem('first');
      sessionStorage.removeItem('last');
      sessionStorage.removeItem('roles');
      sessionStorage.removeItem('raw');

      // Reset Zustand state
      set({
        accessToken: null,
        refreshToken: null,
        id: null,
        userId: null,
        email: null,
        first: null,
        last: null,
        roles: null,
        raw: null,
        isAuthenticated: false,
        session: null,
      });

      // Global logout event
      window.dispatchEvent(new Event('session-logged-out'));
    },
  };
});
