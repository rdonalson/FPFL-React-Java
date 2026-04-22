// src/app/router/AuthGate.tsx
import React from 'react';
import { Outlet, Navigate } from 'react-router-dom';
import { useSessionStore } from '@/app/state/sessionStore';
import { useAuthOnStartup } from '@/app/hooks/useAuthOnStartup';
import { Splash } from '@/app/pages/Splash';

const AuthGate: React.FC = () => {
  const { isChecking } = useAuthOnStartup();
  const token = useSessionStore(s => s.token);

  if (isChecking) {
    return <Splash />;
  }

  // If not authenticated, redirect to login
  if (!token) {
    return <Navigate to="/login" replace />;
  }

  // Authenticated — render nested routes
  return <Outlet />;
};

export default AuthGate;
