// src/app/layout/AppTopMenu.tsx
import { Button } from 'primereact/button';
import { useState } from 'react';
import { APP_TITLE } from '../config/seoConfig';

interface AppTopMenuProps {
  onToggleSidebar: () => void;
}

export default function AppTopMenu({ onToggleSidebar }: AppTopMenuProps) {
  const [dark, setDark] = useState(false);

  const toggleTheme = () => {
    const next = !dark;
    setDark(next);

    const themeLink = document.getElementById('theme-css') as HTMLLinkElement;
    const base = import.meta.env.BASE_URL;

    themeLink.href = next
      ? `${base}themes/lara-dark-indigo/theme.css`
      : `${base}themes/lara-light-indigo/theme.css`;
  };

  return (
    <div className="flex items-center justify-between px-4 py-2 shadow-1 surface-card">
      <Button icon="pi pi-bars" text rounded onClick={onToggleSidebar} />

      <h2 className="text-lg font-semibold">{APP_TITLE}</h2>

      <Button icon={dark ? 'pi pi-sun' : 'pi pi-moon'} text rounded onClick={toggleTheme} />
    </div>
  );
}
