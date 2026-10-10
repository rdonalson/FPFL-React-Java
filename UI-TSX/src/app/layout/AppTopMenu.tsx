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
  const { first, last, clearSession, isAuthenticated, setSession, id, email } = useSessionStore();

  // The shared guest account must not be able to change its password
  const isGuest = (email ?? '').toLowerCase() === DEMO_EMAIL;

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
      className="app-topbar flex align-items-center justify-content-between px-3 py-2"
      style={{ borderBottom: '1px solid var(--surface-border)' }}
    >
      {/* Left side */}
      <div className="app-topbar-left flex align-items-center gap-3">
        <button
          type="button"
          className="p-button p-button-text"
          onClick={onToggleSidebar}
          title="Toggle sidebar"
        >
          <i className="pi pi-bars text-xl" />
        </button>

        <div className="app-topbar-title text-xl font-bold">FPFL Platform</div>
      </div>

      {/* Right side */}
      <div className="app-topbar-right flex align-items-center gap-3">
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
            <button
              className="p-button p-button-text gap-2"
              onClick={handleDemoLogin}
              disabled={loading}
              title="Guest Login"
              aria-label="Guest Login"
            >
              <i className={loading ? 'pi pi-spin pi-spinner' : 'pi pi-user'} />
              <span className="app-topbar-label">{loading ? 'Loading...' : 'Guest Login'}</span>
            </button>

            <button
              className="p-button p-button-text gap-2"
              onClick={() => setShowLogin(true)}
              title="Login"
              aria-label="Login"
            >
              <i className="pi pi-sign-in" />
              <span className="app-topbar-label">Login</span>
            </button>

            <button
              className="p-button p-button-text gap-2"
              onClick={() => setShowRegister(true)}
              title="Register"
              aria-label="Register"
            >
              <i className="pi pi-user-plus" />
              <span className="app-topbar-label">Register</span>
            </button>
          </>
        )}

        {/* Logged in */}
        {isAuthenticated && (
          <div className="flex align-items-center gap-2">
            <span className="app-topbar-username font-medium">
              {first} {last}
            </span>

            {!isGuest && (
              <button
                className="p-button p-button-text gap-2"
                onClick={() => setShowChangePassword(true)}
                title="Change Password"
                aria-label="Change Password"
              >
                <i className="pi pi-key" />
                <span className="app-topbar-label">Change Password</span>
              </button>
            )}

            <button
              className="p-button p-button-text p-button-danger gap-2"
              onClick={() => {
                clearSession();
                navigate('/');
              }}
              title="Logout"
              aria-label="Logout"
            >
              <i className="pi pi-sign-out" />
              <span className="app-topbar-label">Logout</span>
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
