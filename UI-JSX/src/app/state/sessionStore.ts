// src/app/state/sessionStore.ts
import { create } from 'zustand';

export interface SessionState {
  accessToken: string | null;
  refreshToken: string | null;
  id: number | null;
  userId: string | null;
  email: string | null;
  first: string | null;
  last: string | null;
  roles: string[] | null;
  raw: any | null;

  // Derived state (NOT a getter — Zustand does not support getters)
  isAuthenticated: boolean;

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

export const useSessionStore = create<SessionState>((set, get) => ({
  // Initial hydration from sessionStorage
  accessToken: sessionStorage.getItem('accessToken'),
  refreshToken: sessionStorage.getItem('refreshToken'),
  id: Number(sessionStorage.getItem('id')) || null,
  userId: sessionStorage.getItem('userId'),
  email: sessionStorage.getItem('email'),
  first: sessionStorage.getItem('first'),
  last: sessionStorage.getItem('last'),
  roles: JSON.parse(sessionStorage.getItem('roles') || 'null'),
  raw: JSON.parse(sessionStorage.getItem('raw') || 'null'),

  // Derived state (must be stored, not a getter)
  isAuthenticated: Boolean(sessionStorage.getItem('accessToken')),

  // Set session after login or restore
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

    // Update Zustand state
    set({
      accessToken: session.accessToken,
      refreshToken: session.refreshToken,
      id: session.id,
      userId: session.userId,
      email: session.email,
      first: session.first,
      last: session.last,
      roles: session.roles,
      raw: session.raw,
      isAuthenticated: true,
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
    });

    // Global logout event
    window.dispatchEvent(new Event('session-logged-out'));
  },
}));
