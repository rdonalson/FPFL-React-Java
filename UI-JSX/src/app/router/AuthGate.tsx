// src/app/router/AuthGate.tsx
import { Navigate, Outlet } from 'react-router-dom';
import { useSessionStore } from '@/app/state/sessionStore';

export default function AuthGate() {
  // Use the new reactive getter
  const isAuthenticated = useSessionStore(s => s.isAuthenticated);

  // If not authenticated → redirect to home
  if (!isAuthenticated) {
    return <Navigate to="/" replace />;
  }

  // Otherwise allow access to protected routes
  return <Outlet />;
}
