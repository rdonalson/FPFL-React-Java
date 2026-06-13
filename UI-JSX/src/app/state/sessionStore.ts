// src/app/state/sessionStore.ts
import { create } from 'zustand';
import { devtools } from 'zustand/middleware';
import { AppConfig } from '@/config/appConfig';

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
  accessToken: string | null;
  refreshToken: string | null;
  id: number | null;
  userId: string | null;
  email: string | null;
  first: string | null;
  last: string | null;
  roles: string[] | null;
  raw: any | null;

  isAuthenticated: boolean;
  session: SessionObject | null;

  // Token watcher parameters persisted in store
  expiresAt: number | null;
  sessionRestored: boolean;
  warningBeforeMs: number;
  gracePeriodMs: number;
  sessionDebug: boolean;

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

  // Watcher helpers
  setExpiresAt: (ms: number | null) => void;
  setSessionRestored: (v: boolean) => void;
  setWatcherConfig: (cfg: {
    warningBeforeMs?: number;
    gracePeriodMs?: number;
    debug?: boolean;
  }) => void;

  clearSession: () => void;
}

export const useSessionStore = create<SessionState>()(
  devtools(
    set => {
      // Hydrate from sessionStorage once on store creation
      const hydrated: SessionObject = {
        accessToken: sessionStorage.getItem('accessToken'),
        refreshToken: sessionStorage.getItem('refreshToken'),
        id: Number(sessionStorage.getItem('id')) || null,
        userId: sessionStorage.getItem('userId'),
        email: sessionStorage.getItem('email'),
        first: sessionStorage.getItem('first'),
        last: sessionStorage.getItem('last'),
        roles: (() => {
          try {
            return JSON.parse(sessionStorage.getItem('roles') || 'null');
          } catch {
            return null;
          }
        })(),
        raw: (() => {
          try {
            return JSON.parse(sessionStorage.getItem('raw') || 'null');
          } catch {
            return null;
          }
        })(),
      };

      // Initialize watcher config from AppConfig defaults if available
      let initialWarning = 300_000;
      let initialGrace = 0;
      let initialDebug = false;
      try {
        // lazy require to avoid circular import at module load

        //const { AppConfig } = require('@/config/appConfig');
        const cfg = AppConfig.get();
        initialWarning = cfg?.session?.warningBeforeMs ?? initialWarning;
        initialGrace = cfg?.session?.gracePeriodMs ?? initialGrace;
        initialDebug = cfg?.session?.debug ?? initialDebug;
      } catch {
        // ignore if AppConfig not available at this stage
      }

      // Hydrate watcher-specific values from sessionStorage
      const hydratedExpiresAt = (() => {
        const raw = sessionStorage.getItem('expiresAt');
        if (!raw) return null;
        const n = Number(raw);
        return Number.isNaN(n) ? null : n;
      })();

      const hydratedSessionRestored = sessionStorage.getItem('sessionRestored') === '1';

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

        // Full session object
        session: hydrated,

        // Watcher state
        expiresAt: hydratedExpiresAt,
        sessionRestored: hydratedSessionRestored,
        warningBeforeMs: initialWarning,
        gracePeriodMs: initialGrace,
        sessionDebug: initialDebug,

        // Set session after login or refresh
        setSession: session => {
          // Persist to sessionStorage
          try {
            sessionStorage.setItem('accessToken', session.accessToken);
            sessionStorage.setItem('refreshToken', session.refreshToken ?? '');
            sessionStorage.setItem('id', String(session.id));
            sessionStorage.setItem('userId', session.userId);
            sessionStorage.setItem('email', session.email);
            sessionStorage.setItem('first', session.first);
            sessionStorage.setItem('last', session.last);
            sessionStorage.setItem('roles', JSON.stringify(session.roles));
            sessionStorage.setItem('raw', JSON.stringify(session.raw));

            // Persist expiresAt if present in session.raw (support expiresAt in ms or expiresIn in seconds)
            try {
              const raw = session.raw || {};
              let expiresAtMs: number | null = null;
              if (raw.expiresAt) {
                const n = Number(raw.expiresAt);
                // If value looks like seconds (small), convert to ms
                expiresAtMs = n > 1e12 ? n : n * 1_000;
              } else if (raw.expiresIn) {
                const n = Number(raw.expiresIn);
                if (!Number.isNaN(n)) expiresAtMs = Date.now() + n * 1_000;
              }
              // Also support explicit expiresAt passed directly on session.raw.tokenExpiresAt
              else if (raw.tokenExpiresAt) {
                const n = Number(raw.tokenExpiresAt);
                expiresAtMs = n > 1e12 ? n : n * 1_000;
              }
              if (expiresAtMs) {
                sessionStorage.setItem('expiresAt', String(expiresAtMs));
                // update store value as well
                set({ expiresAt: expiresAtMs }, false, 'session/setExpiresAt');
              }
            } catch {
              // ignore
            }
          } catch (e) {
            // ignore storage errors (e.g., private mode)

            console.warn('sessionStore: failed to persist sessionStorage', e);
          }

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

          // Update Zustand state + DevTools action
          set(
            {
              ...sessionObj,
              isAuthenticated: true,
              session: sessionObj,
            },
            false,
            'session/setSession',
          );

          // Notify listeners that a session was established/refreshed.
          // Use try/catch to avoid breaking the app if events are not supported.
          try {
            window.dispatchEvent(
              new CustomEvent('session-logged-in', { detail: { session: sessionObj } }),
            );
            window.dispatchEvent(
              new CustomEvent('session-refreshed', { detail: { session: sessionObj } }),
            );
          } catch {
            // best-effort
          }
        },

        // Watcher helpers
        setExpiresAt: (ms: number | null) => {
          try {
            if (ms === null) {
              sessionStorage.removeItem('expiresAt');
            } else {
              sessionStorage.setItem('expiresAt', String(ms));
            }
          } catch {
            // ignore
          }
          set({ expiresAt: ms }, false, 'session/setExpiresAt');
        },

        setSessionRestored: (v: boolean) => {
          try {
            if (v) sessionStorage.setItem('sessionRestored', '1');
            else sessionStorage.removeItem('sessionRestored');
          } catch {
            // ignore
          }
          set({ sessionRestored: v }, false, 'session/setSessionRestored');
        },

        setWatcherConfig: cfg => {
          const next = {
            warningBeforeMs: cfg.warningBeforeMs ?? initialWarning,
            gracePeriodMs: cfg.gracePeriodMs ?? initialGrace,
            sessionDebug: cfg.debug ?? initialDebug,
          };
          set(next, false, 'session/setWatcherConfig');
        },

        // Clear session on logout
        clearSession: () => {
          try {
            sessionStorage.removeItem('accessToken');
            sessionStorage.removeItem('refreshToken');
            sessionStorage.removeItem('id');
            sessionStorage.removeItem('userId');
            sessionStorage.removeItem('email');
            sessionStorage.removeItem('first');
            sessionStorage.removeItem('last');
            sessionStorage.removeItem('roles');
            sessionStorage.removeItem('raw');
            sessionStorage.removeItem('sessionRestored');
            sessionStorage.removeItem('expiresAt');
          } catch (e) {
            // ignore storage errors

            console.warn('sessionStore: failed to clear sessionStorage', e);
          }

          set(
            {
              accessToken: null,
              refreshToken: null,
              id: null,
              userId: null,
              email: null,
              first: null,
              last: null,
              roles: null,
              raw: null,
              expiresAt: null,
              sessionRestored: false,
              isAuthenticated: false,
              session: null,
            },
            false,
            'session/clearSession',
          );

          // Hide any expiring dialog first, then notify logged out
          try {
            window.dispatchEvent(new Event('session-expiring-hide'));
          } catch {
            // ignore
          }

          try {
            window.dispatchEvent(new Event('session-logged-out'));
          } catch {
            // ignore
          }
        },
      };
    },
    { name: 'SessionStore' },
  ),
);
