import { create } from 'zustand';

interface SessionState {
  token: string | null;
  refreshToken: string | null;
  userId: string | null;
  email: string | null;
  roles: string[] | null;
  raw: any | null;

  setSession: (session: {
    token: string;
    refreshToken: string | null;
    userId: string;
    email: string;
    roles: string[];
    raw: any;
  }) => void;

  clearSession: () => void;
}

export const useSessionStore = create<SessionState>(set => ({
  token: sessionStorage.getItem('token'),
  refreshToken: sessionStorage.getItem('refreshToken'),
  userId: sessionStorage.getItem('userId'),
  email: sessionStorage.getItem('email'),
  roles: JSON.parse(sessionStorage.getItem('roles') || 'null'),
  raw: JSON.parse(sessionStorage.getItem('raw') || 'null'),

  setSession: session => {
    sessionStorage.setItem('token', session.token);
    sessionStorage.setItem('refreshToken', session.refreshToken ?? '');
    sessionStorage.setItem('userId', session.userId);
    sessionStorage.setItem('email', session.email);
    sessionStorage.setItem('roles', JSON.stringify(session.roles));
    sessionStorage.setItem('raw', JSON.stringify(session.raw));

    set({
      token: session.token,
      refreshToken: session.refreshToken,
      userId: session.userId,
      email: session.email,
      roles: session.roles,
      raw: session.raw,
    });
  },

  clearSession: () => {
    sessionStorage.clear();
    set({
      token: null,
      refreshToken: null,
      userId: null,
      email: null,
      roles: null,
      raw: null,
    });
  },
}));
