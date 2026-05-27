// src/app/layout/AppTopMenu.tsx
import { useState } from 'react';
import { useSessionStore } from '@/app/state/sessionStore';
import { useThemeStore } from '@/app/state/themeStore';
import { LoginDialog } from '@/app/auth/components/LoginDialog';
import { RegisterDialog } from '@/app/auth/components/RegisterDialog';
import { ChangePasswordDialog } from '@/app/auth/components/ChangePasswordDialog';
import { authApi } from '@/app/auth/api/authApi';
import { unwrap } from '@/api/utils/responseHelpers';
import { useNavigate } from 'react-router-dom';

const DEMO_EMAIL = 'guest.user@gmail.com';
const DEMO_PASSWORD = 'Password@2';

interface AppTopMenuProps {
  onToggleSidebar: () => void;
}

export function AppTopMenu({ onToggleSidebar }: AppTopMenuProps) {
  const [showLogin, setShowLogin] = useState(false);
  const [showRegister, setShowRegister] = useState(false);
  const [showChangePassword, setShowChangePassword] = useState(false);
  const [loading, setLoading] = useState(false);

  const navigate = useNavigate();

  const { dark, toggleTheme } = useThemeStore();
  const { first, last, clearSession, isAuthenticated, setSession, id } = useSessionStore();

  async function handleDemoLogin() {
    try {
      setLoading(true);

      const raw = await authApi.login(DEMO_EMAIL, DEMO_PASSWORD);
      const result = unwrap(raw);

      if (result?.accessToken) {
        setSession({
          accessToken: result.accessToken,
          refreshToken: result.refreshToken ?? null,
          id: result.id,
          userId: result.userId,
          email: result.email,
          first: result.first,
          last: result.last,
          roles: result.roles ?? [],
          raw: result,
        });

        navigate('/');
      } else {
        console.error('Unexpected demo login response', result);
      }
    } catch (err) {
      console.error('Demo login failed', err);
    } finally {
      setLoading(false);
    }
  }

  return (
    <div
      className="flex align-items-center justify-content-between px-3 py-2"
      style={{ borderBottom: '1px solid var(--surface-border)' }}
    >
      {/* Left side */}
      <div className="flex align-items-center gap-3">
        <button
          type="button"
          className="p-button p-button-text"
          onClick={onToggleSidebar}
          title="Toggle sidebar"
        >
          <i className="pi pi-bars text-xl" />
        </button>

        <div className="text-xl font-bold">FPFL Platform</div>
      </div>

      {/* Right side */}
      <div className="flex align-items-center gap-3">
        {/* Theme toggle */}
        <button
          type="button"
          className="p-button p-button-text"
          onClick={toggleTheme}
          title={dark ? 'Switch to light mode' : 'Switch to dark mode'}
        >
          <i className={dark ? 'pi pi-sun' : 'pi pi-moon'} />
        </button>

        {/* Not logged in */}
        {!isAuthenticated && (
          <>
            <button className="p-button p-button-text" onClick={handleDemoLogin} disabled={loading}>
              {loading ? 'Loading...' : 'Guest Login'}
            </button>

            <button className="p-button p-button-text" onClick={() => setShowLogin(true)}>
              Login
            </button>

            <button className="p-button p-button-text" onClick={() => setShowRegister(true)}>
              Register
            </button>
          </>
        )}

        {/* Logged in */}
        {isAuthenticated && (
          <div className="flex align-items-center gap-2">
            <span className="font-medium">
              {first} {last}
            </span>

            <button className="p-button p-button-text" onClick={() => setShowChangePassword(true)}>
              Change Password
            </button>

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
      <ChangePasswordDialog
        visible={showChangePassword}
        onHide={() => setShowChangePassword(false)}
        id={id ?? 0} // <-- Long id from Zustand
      />
    </div>
  );
}
