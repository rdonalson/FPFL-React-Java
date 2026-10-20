// src/app/auth/hooks/useGuestLogin.ts
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useSessionStore } from '@/app/state/sessionStore';
import { authApi } from '@/app/auth/api/authApi';
import { unwrap } from '@/api/utils/responseHelpers';

export const DEMO_EMAIL = 'guest.user@gmail.com';
const DEMO_PASSWORD = 'Password@2';

/** Signs in with the shared demo account. Used by the top bar and the home page hero. */
export function useGuestLogin() {
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();
  const setSession = useSessionStore(s => s.setSession);

  async function loginAsGuest() {
    try {
      setLoading(true);

      const raw = await authApi.login(DEMO_EMAIL, DEMO_PASSWORD);
      const result = unwrap(raw);

      if (result?.accessToken) {
        setSession({
          accessToken: result.accessToken,
          refreshToken: result.refreshToken ?? null,
          id: result.id,
          userId: result.userId,
          email: result.email,
          first: result.first,
          last: result.last,
          roles: result.roles ?? [],
          raw: result,
        });

        navigate('/');
      } else {
        console.error('Unexpected demo login response', result);
      }
    } catch (err) {
      console.error('Demo login failed', err);
    } finally {
      setLoading(false);
    }
  }

  return { loginAsGuest, loading };
}
