import { create } from 'zustand';

interface SessionState {
  userId: string | null;
  token: string | null;

  setUserId: (id: string) => void;
  setToken: (token: string) => void;

  clearSession: () => void;
}

export const useSessionStore = create<SessionState>(set => ({
  userId: sessionStorage.getItem('userId'),
  token: sessionStorage.getItem('token'),

  setUserId: id => {
    sessionStorage.setItem('userId', id);
    set({ userId: id });
  },

  setToken: token => {
    sessionStorage.setItem('token', token);
    set({ token });
  },

  clearSession: () => {
    sessionStorage.removeItem('userId');
    sessionStorage.removeItem('token');
    set({ userId: null, token: null });
  },
}));
