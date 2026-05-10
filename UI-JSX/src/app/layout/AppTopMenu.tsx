// src/app/layout/AppTopMenu.tsx
import React, { useState } from 'react';
import { Button } from 'primereact/button';
import { Dialog } from 'primereact/dialog';
import { InputText } from 'primereact/inputtext';
import { Password } from 'primereact/password';
import { useNavigate } from 'react-router-dom';

import { useSessionStore } from '@/app/state/sessionStore';
import { loginApi } from '@/api/authApi';
import { GUEST_USER_ID, GUEST_TOKEN } from '@/app/auth/constants';

export default function AppTopMenu({ onToggleSidebar }: { onToggleSidebar: () => void }) {
  const navigate = useNavigate();

  const { userId, setSession, clearSession } = useSessionStore();

  const [showLoginDialog, setShowLoginDialog] = useState(false);
  const [email, setEmail] = useState(''); // backend expects email
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);

  const isLoggedIn = !!userId;

  // ---------------------------
  // DEMO LOGIN
  // ---------------------------
  function handleDemoLogin() {
    setSession({
      token: GUEST_TOKEN,
      refreshToken: '',
      userId: GUEST_USER_ID,
      email: 'guest@example.com',
      roles: ['GUEST'],
      raw: {},
    });

    navigate('/');
  }

  // ---------------------------
  // REAL LOGIN
  // ---------------------------
  async function handleLoginSubmit() {
    try {
      setLoading(true);

      const result = await loginApi(email, password);

      // result is already normalized by loginApi
      setSession(result);

      setShowLoginDialog(false);
      navigate('/');
    } catch (err) {
      console.error('Login failed', err);
    } finally {
      setLoading(false);
    }
  }

  // ---------------------------
  // LOGOUT
  // ---------------------------
  function handleLogout() {
    clearSession();
    navigate('/');
  }

  return (
    <div className="flex items-center justify-between px-4 py-2 surface-card shadow-1">
      {/* Hamburger only when logged in */}
      {isLoggedIn && (
        <Button icon="pi pi-bars" className="p-button-text" onClick={onToggleSidebar} />
      )}

      <h1 className="text-xl font-semibold">My App</h1>

      <div className="flex gap-2">
        {!isLoggedIn && (
          <>
            <Button
              label="Demo Login"
              icon="pi pi-user"
              className="p-button-sm p-button-secondary"
              onClick={handleDemoLogin}
            />

            <Button
              label="Login"
              icon="pi pi-sign-in"
              className="p-button-sm"
              onClick={() => setShowLoginDialog(true)}
            />
          </>
        )}

        {isLoggedIn && (
          <Button
            label="Logout"
            icon="pi pi-sign-out"
            className="p-button-sm p-button-danger"
            onClick={handleLogout}
          />
        )}
      </div>

      {/* LOGIN DIALOG */}
      <Dialog
        header="Login"
        visible={showLoginDialog}
        onHide={() => setShowLoginDialog(false)}
        style={{ width: '25rem' }}
      >
        <div className="flex flex-col gap-3">
          <span className="p-float-label">
            <InputText id="email" value={email} onChange={e => setEmail(e.target.value)} />
            <label htmlFor="email">Email</label>
          </span>

          <span className="p-float-label">
            <Password
              id="password"
              value={password}
              onChange={e => setPassword(e.target.value)}
              toggleMask
            />
            <label htmlFor="password">Password</label>
          </span>

          <Button label="Login" icon="pi pi-check" loading={loading} onClick={handleLoginSubmit} />
        </div>
      </Dialog>
    </div>
  );
}
