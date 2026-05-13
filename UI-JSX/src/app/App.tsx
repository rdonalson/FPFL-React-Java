import './App.css';
import { AppRouter } from '@/app/router/AppRouter';
import { useThemeStore } from './state/themeStore';
import { useNavigate } from 'react-router-dom';
import { useEffect } from 'react';
import { useAuthOnStartup } from '@/app/auth/hooks/useAuthOnStartup';
import { useTokenWatcher } from './auth/hooks/useTokenWatcher';
import { SessionExpireDialog } from '@/app/auth/components/SessionExpireDialog';

function App() {
  const { showDialog, setShowDialog } = useTokenWatcher();
  const { dark, setTheme } = useThemeStore();
  const navigate = useNavigate();

  // Restore session on startup
  useAuthOnStartup();

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

      {/* 🔥 Session Expiring Dialog */}
      <SessionExpireDialog visible={showDialog} onHide={() => setShowDialog(false)} />
    </>
  );
}

export default App;
