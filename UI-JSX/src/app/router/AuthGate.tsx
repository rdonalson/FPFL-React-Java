// src/app/router/AuthGate.tsx
import React from 'react';
import { Outlet, Navigate } from 'react-router-dom';
import { useSessionStore } from '@/app/state/sessionStore';
import { useAuthOnStartup } from '@/app/hooks/useAuthOnStartup';

export default function AuthGate() {
  // Run startup session restore
  useAuthOnStartup();

  // Pull current auth state
  const accessToken = useSessionStore(s => s.accessToken);

  // If no token → redirect to login
  if (!accessToken) {
    return <Navigate to="/login" replace />;
  }

  // Otherwise allow access to protected routes
  return <Outlet />;
}
