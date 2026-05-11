// src/app/layout/AppTopMenu.tsx
import { useState } from 'react';
import { useSessionStore } from '@/app/state/sessionStore';
import { useThemeStore } from '@/app/state/themeStore';

import { LoginDialog } from '@/app/auth/components/LoginDialog';
import { RegisterDialog } from '@/app/auth/components/RegisterDialog';
import { useNavigate } from 'react-router-dom';

interface AppTopMenuProps {
  onToggleSidebar: () => void;
}

export function AppTopMenu({ onToggleSidebar }: AppTopMenuProps) {
  const [showLogin, setShowLogin] = useState(false);
  const [showRegister, setShowRegister] = useState(false);
  const navigate = useNavigate();
  const { dark, toggleTheme } = useThemeStore();
  const { accessToken, first, last, clearSession } = useSessionStore();

  const isLoggedIn = Boolean(accessToken);

  return (
    <div
      className="flex align-items-center justify-content-between px-3 py-2"
      style={{ borderBottom: '1px solid var(--surface-border)' }}
    >
      {/* Left side: Sidebar toggle + App title */}
      <div className="flex align-items-center gap-3">
        <button className="p-button p-button-text" onClick={onToggleSidebar} title="Open menu">
          <i className="pi pi-bars text-xl" />
        </button>

        <div className="text-xl font-bold">FPFL Platform</div>
      </div>

      {/* Right side: Buttons */}
      <div className="flex align-items-center gap-3">
        {/* Theme toggle */}
        <button className="p-button p-button-text" onClick={toggleTheme} title="Toggle theme">
          <i className={dark ? 'pi pi-sun' : 'pi pi-moon'} />
        </button>

        {/* If NOT logged in → show Login + Register */}
        {!isLoggedIn && (
          <>
            <button className="p-button p-button-text" onClick={() => setShowLogin(true)}>
              Login
            </button>

            <button className="p-button p-button-text" onClick={() => setShowRegister(true)}>
              Register
            </button>
          </>
        )}

        {/* If logged in → show user + logout */}
        {isLoggedIn && (
          <div className="flex align-items-center gap-2">
            <span className="font-medium">
              {first} {last}
            </span>

            <button
              className="p-button p-button-text p-button-danger"
              onClick={() => {
                clearSession();
                navigate('/');
              }}
            >
              Logout
            </button>
          </div>
        )}
      </div>

      {/* Dialogs */}
      <LoginDialog visible={showLogin} onHide={() => setShowLogin(false)} />
      <RegisterDialog visible={showRegister} onHide={() => setShowRegister(false)} />
    </div>
  );
}
