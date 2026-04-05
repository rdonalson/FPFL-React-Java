// src/layout/AppTopMenu.tsx
import { Button } from 'primereact/button';
import { useState } from 'react';

interface AppTopMenuProps {
  onToggleSidebar: () => void;
}

export function AppTopMenu({ onToggleSidebar }: AppTopMenuProps) {
  const [dark, setDark] = useState(false);

  const toggleTheme = () => {
    const next = !dark;
    setDark(next);

    // Swap PrimeReact theme (Vite-friendly)
    const themeLink = document.getElementById('theme-css') as HTMLLinkElement;
    themeLink.href = next
      ? '/themes/lara-dark-indigo/theme.css'
      : '/themes/lara-light-indigo/theme.css';

    console.log('Switched theme to:', next ? 'Dark' : 'Light');
    console.log('Updated theme link href to:', themeLink.href);
  };

  return (
    <div className="flex items-center justify-between px-4 py-2 shadow-1 surface-card">
      {/* HAMBURGER */}
      <Button icon="pi pi-bars" text rounded onClick={onToggleSidebar} className="mr-2" />

      <h2 className="text-lg font-semibold">My App</h2>

      {/* LIGHT / DARK TOGGLE */}
      <Button icon={dark ? 'pi pi-sun' : 'pi pi-moon'} text rounded onClick={toggleTheme} />
    </div>
  );
}
