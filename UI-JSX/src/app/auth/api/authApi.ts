// src/app/auth/api/authApi.ts
import { AuthClient } from '@/api/generated/AuthClient';
import { unwrap } from '@/api/utils/responseHelpers';
import { useSessionStore } from '@/app/state/sessionStore';

import type { AuthResponse } from '../types/AuthResponse';
import type { RefreshResponse } from '../types/RefreshResponse';

export const authApi = {
  async login(email: string, password: string): Promise<AuthResponse> {
    const res = await AuthClient.login({ email, password });
    const data = unwrap<AuthResponse>(res, null, true) as AuthResponse;

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

    return data;
  },

  async refresh(refreshToken: string): Promise<RefreshResponse | null> {
    if (!refreshToken) return null;

    const res = await AuthClient.refresh(refreshToken);
    const data = unwrap<RefreshResponse>(res, null);

    if (data) {
      const session = useSessionStore.getState().session!;
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
    }

    return data;
  },
};
