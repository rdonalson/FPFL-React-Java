import './App.css';
import { AppRouter } from '@/app/router/AppRouter';
import { useThemeStore } from './state/themeStore';
import { useNavigate } from 'react-router-dom';
import { useEffect } from 'react';
import { useAuthOnStartup } from '@/app/hooks/useAuthOnStartup';

function App() {
  const { dark, setTheme } = useThemeStore();
  const navigate = useNavigate();

  // ⭐ Run session restore ONCE globally
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

  return <AppRouter />;
}

export default App;
