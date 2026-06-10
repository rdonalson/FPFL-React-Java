// src/app/router/AdminRouteGuard.tsx
import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '@/app/auth/hooks/useAuth';

export function AdminRouteGuard() {
  const { hasRole, isAuthenticated } = useAuth();

  if (!isAuthenticated) {
    return <Navigate to="/" replace />;
  }

  if (!hasRole('ROLE_ADMIN')) {
    return <Navigate to="/" replace />;
  }

  return <Outlet />;
}
