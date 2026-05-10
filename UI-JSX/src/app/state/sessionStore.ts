import { create } from 'zustand';

interface SessionState {
  accessToken: string | null;
  refreshToken: string | null;
  id: number | null; // numeric DB id
  userID: string | null; // UUID
  email: string | null;
  first: string | null;
  last: string | null;
  roles: string[] | null;
  raw: any | null;

  isAuthenticated: boolean;

  setSession: (session: {
    accessToken: string;
    refreshToken: string | null;
    id: number;
    userID: string;
    email: string;
    first: string;
    last: string;
    roles: string[];
    raw: any;
  }) => void;

  clearSession: () => void;
}

export const useSessionStore = create<SessionState>(set => ({
  accessToken: sessionStorage.getItem('accessToken'),
  refreshToken: sessionStorage.getItem('refreshToken'),
  id: Number(sessionStorage.getItem('id')) || null,
  userID: sessionStorage.getItem('userID'),
  email: sessionStorage.getItem('email'),
  first: sessionStorage.getItem('first'),
  last: sessionStorage.getItem('last'),
  roles: JSON.parse(sessionStorage.getItem('roles') || 'null'),
  raw: JSON.parse(sessionStorage.getItem('raw') || 'null'),

  isAuthenticated: !!sessionStorage.getItem('accessToken'),

  setSession: session => {
    sessionStorage.setItem('accessToken', session.accessToken);
    sessionStorage.setItem('refreshToken', session.refreshToken ?? '');
    sessionStorage.setItem('id', String(session.id));
    sessionStorage.setItem('userID', session.userID);
    sessionStorage.setItem('email', session.email);
    sessionStorage.setItem('first', session.first);
    sessionStorage.setItem('last', session.last);
    sessionStorage.setItem('roles', JSON.stringify(session.roles));
    sessionStorage.setItem('raw', JSON.stringify(session.raw));

    set({
      accessToken: session.accessToken,
      refreshToken: session.refreshToken,
      id: session.id,
      userID: session.userID,
      email: session.email,
      first: session.first,
      last: session.last,
      roles: session.roles,
      raw: session.raw,
      isAuthenticated: true,
    });
  },

  clearSession: () => {
    sessionStorage.clear();
    set({
      accessToken: null,
      refreshToken: null,
      id: null,
      userID: null,
      email: null,
      first: null,
      last: null,
      roles: null,
      raw: null,
      isAuthenticated: false,
    });
  },
}));
