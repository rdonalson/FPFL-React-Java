// src/app/auth/api/authApi.ts
import { AuthClient } from '@/api/generated/AuthClient';
import { unwrap } from '@/api/utils/responseHelpers';
import { useSessionStore } from '@/app/state/sessionStore';

import type { AuthResponse } from '../types/AuthResponse';
import type { RefreshResponse } from '../types/RefreshResponse';

// Decode JWT exp → expiresAt (ms)
function extractExpiresAt(token: string): number | null {
  try {
    const [, payload] = token.split('.');
    const decoded = JSON.parse(atob(payload));
    if (!decoded.exp) return null;
    return decoded.exp * 1000; // seconds → ms
  } catch {
    return null;
  }
}

export const authApi = {
  async login(email: string, password: string): Promise<AuthResponse> {
    const res = await AuthClient.login({ email, password });
    const data = unwrap<AuthResponse>(res, null, true) as AuthResponse;

    // Decode expiry from JWT
    const expiresAt = extractExpiresAt(data.accessToken);

    // Store session
    useSessionStore.getState().setSession({
      accessToken: data.accessToken,
      refreshToken: data.refreshToken,
      id: data.id,
      userId: data.userId,
      email: data.email,
      first: data.first,
      last: data.last,
      roles: data.roles,
      raw: data,
    });

    // NEW: mark restored + set expiry
    useSessionStore.getState().setSessionRestored(true);
    useSessionStore.getState().setExpiresAt(expiresAt);

    return data;
  },

  async register(
    first: string,
    last: string,
    email: string,
    password: string,
  ): Promise<AuthResponse> {
    const res = await AuthClient.register({ first, last, email, password });
    const data = unwrap<AuthResponse>(res, null, true) as AuthResponse;

    const expiresAt = extractExpiresAt(data.accessToken);

    useSessionStore.getState().setSession({
      accessToken: data.accessToken,
      refreshToken: data.refreshToken,
      id: data.id,
      userId: data.userId,
      email: data.email,
      first: data.first,
      last: data.last,
      roles: data.roles,
      raw: data,
    });

    useSessionStore.getState().setSessionRestored(true);
    useSessionStore.getState().setExpiresAt(expiresAt);

    return data;
  },

  async refresh(refreshToken: string): Promise<RefreshResponse | null> {
    if (!refreshToken) return null;

    const res = await AuthClient.refresh(refreshToken);
    const data = unwrap<RefreshResponse>(res, null);

    if (data) {
      const session = useSessionStore.getState().session!;
      const expiresAt = extractExpiresAt(data.accessToken);

      useSessionStore.getState().setSession({
        accessToken: data.accessToken,
        refreshToken: data.refreshToken,
        id: session.id!,
        userId: session.userId!,
        email: session.email!,
        first: session.first!,
        last: session.last!,
        roles: session.roles!,
        raw: data,
      });

      useSessionStore.getState().setSessionRestored(true);
      useSessionStore.getState().setExpiresAt(expiresAt);
    }

    return data;
  },
};
