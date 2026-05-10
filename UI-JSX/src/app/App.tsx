import './App.css';
import { AppRouter } from '@/app/router/AppRouter';
import { useThemeStore } from './state/themeStore';
import { useEffect } from 'react';

function App() {
  const { dark, setTheme } = useThemeStore();

  // Hydrate theme on first load
  useEffect(() => {
    setTheme(dark);
  }, []);

  return <AppRouter />;
}

export default App;
