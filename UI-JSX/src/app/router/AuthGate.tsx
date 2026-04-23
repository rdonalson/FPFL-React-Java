// src/app/router/AuthGate.tsx
import React from 'react';
import { Outlet, Navigate } from 'react-router-dom';
import { useSessionStore } from '@/app/state/sessionStore';
import { useAuthOnStartup } from '@/app/hooks/useAuthOnStartup';

/**
 * AuthGate responsibilities:
 * - run the startup auth check (useAuthOnStartup)
 * - while checking, render a minimal placeholder (null or a small spinner)
 * - if not authenticated, redirect to the public Home page ("/")
 * - if authenticated, render nested protected routes via <Outlet />
 *
 * Note: Home is intentionally public in the router so the topbar and home page
 * are visible before login. Protected routes are nested under this gate.
 */
const AuthGate: React.FC = () => {
  const { isChecking } = useAuthOnStartup();
  const token = useSessionStore(s => s.token);

  // While the startup check runs, render nothing (or a tiny placeholder).
  // This prevents flashing protected UI while the session is being restored.
  if (isChecking) {
    return null;
  }

  // If not authenticated, redirect to the public home page.
  if (!token) {
    return <Navigate to="/" replace />;
  }

  // Authenticated — render nested protected routes
  return <Outlet />;
};

export default AuthGate;
