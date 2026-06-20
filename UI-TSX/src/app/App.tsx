//import './App.scss';
import { AppRouter } from '@/app/router/AppRouter';
import { useThemeStore } from './state/themeStore';
import { useNavigate } from 'react-router-dom';
import { useEffect } from 'react';
import { useAuth } from '@/app/auth/hooks/useAuth';
import { useTokenWatcher } from './auth/hooks/useTokenWatcher';
import { SessionExpireDialog } from '@/app/auth/components/SessionExpireDialog';
import { useSessionStore } from './state/sessionStore';
import { BreakpointOverlay } from '@/components/BreakpointOverlay';

function App() {
  const { showDialog, setShowDialog } = useTokenWatcher();
  const { dark, setTheme } = useThemeStore();
  const navigate = useNavigate();
  const isAuthenticated = useSessionStore(s => s.isAuthenticated);

  // Restore session on startup
  useAuth();

  // Listen for logout events to redirect to home
  useEffect(() => {
    const handler = () => navigate('/');
    window.addEventListener('session-logged-out', handler);
    return () => window.removeEventListener('session-logged-out', handler);
  }, [navigate]);

  // Hydrate theme on first load
  useEffect(() => {
    setTheme(dark);
  }, [dark, setTheme]);

  return (
    <>
      <AppRouter />
      <BreakpointOverlay />
      {/* 🔥 Session Expiring Dialog */}
      {isAuthenticated && showDialog && (
        <SessionExpireDialog visible={showDialog} onHide={() => setShowDialog(false)} />
      )}
    </>
  );
}

export default App;
