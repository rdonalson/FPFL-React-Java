// src/app/layout/AppTopMenu.tsx
import { useState } from 'react';
import { NavLink, Link, useLocation, useNavigate } from 'react-router-dom';
import * as DropdownMenu from '@radix-ui/react-dropdown-menu';
import { useSessionStore } from '@/app/state/sessionStore';
import { useThemeStore } from '@/app/state/themeStore';
import { LoginDialog } from '@/app/auth/components/LoginDialog';
import { RegisterDialog } from '@/app/auth/components/RegisterDialog';
import { ChangePasswordDialog } from '@/app/auth/components/ChangePasswordDialog';
import { DEMO_EMAIL, useGuestLogin } from '@/app/auth/hooks/useGuestLogin';
import { menuModel } from './model/menuModel';
import { filterMenuByRoles } from './model/filterMenu';

interface AppTopMenuProps {
  onToggleSidebar: () => void;
}

export function AppTopMenu({ onToggleSidebar }: AppTopMenuProps) {
  const [showLogin, setShowLogin] = useState(false);
  const [showRegister, setShowRegister] = useState(false);
  const [showChangePassword, setShowChangePassword] = useState(false);

  const navigate = useNavigate();
  const location = useLocation();
  const { loginAsGuest, loading } = useGuestLogin();

  const { dark, toggleTheme } = useThemeStore();
  const { first, last, clearSession, isAuthenticated, id, email, roles } = useSessionStore();

  // The shared guest account must not be able to change its password
  const isGuest = (email ?? '').toLowerCase() === DEMO_EMAIL;

  // Same role-filtered model as the sidebar: "Actions" become inline links, "Admin" a dropdown
  const sections = isAuthenticated ? filterMenuByRoles(menuModel, roles ?? []) : [];
  const actions = sections.find(s => s.label === 'Actions')?.items ?? [];
  const admin = sections.find(s => s.label === 'Admin')?.items ?? [];
  const adminActive = admin.some(a => a.to === location.pathname);

  return (
    <div className="app-topbar fl-topbar">
      {/* Left side */}
      <div className="app-topbar-left fl-topbar-left">
        {isAuthenticated && (
          <button
            type="button"
            className="fl-icon-btn fl-menu-toggle"
            onClick={onToggleSidebar}
            title="Open menu"
            aria-label="Open menu"
          >
            <i className="pi pi-bars" />
          </button>
        )}

        <Link to="/" className="fl-brand" aria-label="FPFL Platform home">
          <span className="app-topbar-title fl-brand-mark">FPFL</span>
          <span className="fl-brand-sub">Platform</span>
        </Link>

        {isAuthenticated && (
          <nav className="fl-nav" aria-label="Main">
            {actions.map(item => (
              <NavLink
                key={item.to}
                to={item.to!}
                end={item.to === '/'}
                className={({ isActive }) => `fl-nav-link${isActive ? ' is-active' : ''}`}
              >
                {item.label}
              </NavLink>
            ))}

            {admin.length > 0 && (
              <DropdownMenu.Root>
                <DropdownMenu.Trigger asChild>
                  <button type="button" className={`fl-nav-link${adminActive ? ' is-active' : ''}`}>
                    Admin <i className="pi pi-angle-down" aria-hidden="true" />
                  </button>
                </DropdownMenu.Trigger>
                <DropdownMenu.Portal>
                  <DropdownMenu.Content className="fl-dropdown" align="start" sideOffset={6}>
                    {admin.map(item => (
                      <DropdownMenu.Item key={item.to} asChild>
                        <Link to={item.to!} className="fl-dropdown-item">
                          {item.icon && <i className={item.icon} aria-hidden="true" />}
                          {item.label}
                        </Link>
                      </DropdownMenu.Item>
                    ))}
                  </DropdownMenu.Content>
                </DropdownMenu.Portal>
              </DropdownMenu.Root>
            )}
          </nav>
        )}
      </div>

      {/* Right side */}
      <div className="app-topbar-right fl-topbar-right">
        <button
          type="button"
          className="fl-icon-btn"
          onClick={toggleTheme}
          title={dark ? 'Switch to light mode' : 'Switch to dark mode'}
          aria-label={dark ? 'Switch to light mode' : 'Switch to dark mode'}
        >
          <i className={dark ? 'pi pi-sun' : 'pi pi-moon'} />
        </button>

        {/* Not logged in */}
        {!isAuthenticated && (
          <>
            <button
              type="button"
              className="fl-btn"
              onClick={loginAsGuest}
              disabled={loading}
              title="Guest Login"
              aria-label="Guest Login"
            >
              <i className={loading ? 'pi pi-spin pi-spinner' : 'pi pi-user'} />
              <span className="app-topbar-label">{loading ? 'Loading...' : 'Guest Login'}</span>
            </button>

            <button
              type="button"
              className="fl-btn"
              onClick={() => setShowRegister(true)}
              title="Register"
              aria-label="Register"
            >
              <i className="pi pi-user-plus" />
              <span className="app-topbar-label">Register</span>
            </button>

            <button
              type="button"
              className="fl-btn fl-btn--solid"
              onClick={() => setShowLogin(true)}
              title="Login"
              aria-label="Login"
            >
              <i className="pi pi-sign-in" />
              <span className="app-topbar-label">Login</span>
            </button>
          </>
        )}

        {/* Logged in */}
        {isAuthenticated && (
          <>
            <span className="app-topbar-username fl-username">
              {first} {last}
            </span>

            {!isGuest && (
              <button
                type="button"
                className="fl-btn"
                onClick={() => setShowChangePassword(true)}
                title="Change Password"
                aria-label="Change Password"
              >
                <i className="pi pi-key" />
                <span className="app-topbar-label">Change Password</span>
              </button>
            )}

            <button
              type="button"
              className="fl-btn"
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
          </>
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
