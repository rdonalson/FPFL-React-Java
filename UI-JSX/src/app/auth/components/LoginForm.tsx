// src/app/auth/LoginForm.tsx
import React, { useRef, useState } from 'react';
import { Toast } from 'primereact/toast';
import { InputText } from 'primereact/inputtext';
import { Button } from 'primereact/button';
import { authApi } from '@/app/auth/api/authApi';
import { unwrap } from '@/api/utils/responseHelpers';
import type { AuthResponse } from '@/app/auth/types/AuthResponse';

interface LoginFormProps {
  onSuccess: (session: AuthResponse) => void;
  initialEmail?: string;
}

export function LoginForm({ onSuccess, initialEmail = '' }: LoginFormProps) {
  const toast = useRef<Toast | null>(null);

  const [email, setEmail] = useState<string>(initialEmail);
  const [password, setPassword] = useState<string>('');
  const [loading, setLoading] = useState<boolean>(false);
  const [showPassword, setShowPassword] = useState<boolean>(false);

  const handleSubmit = async (e?: React.FormEvent) => {
    e?.preventDefault();
    setLoading(true);

    try {
      const raw = await authApi.login(email, password);
      const result = unwrap(raw, null, true);

      onSuccess(result as AuthResponse);
      toast.current?.show({
        severity: 'success',
        summary: 'Signed in',
        detail: 'Welcome back',
        life: 2000,
      });
    } catch (err: any) {
      const message = err?.message ?? String(err);
      toast.current?.show({
        severity: 'error',
        summary: 'Login failed',
        detail: message,
        life: 4000,
      });
    } finally {
      setLoading(false);
    }
  };

  const handleDemo = async () => {
    setLoading(true);
    try {
      const raw = await authApi.login('guest.user@gmail.com', 'Password@2');
      const result = unwrap(raw, null, true);
      onSuccess(result as AuthResponse);
      toast.current?.show({
        severity: 'success',
        summary: 'Signed in',
        detail: 'Demo session started',
        life: 2000,
      });
    } catch (err: any) {
      toast.current?.show({
        severity: 'error',
        summary: 'Demo login failed',
        detail: err?.message ?? String(err),
        life: 4000,
      });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      <Toast ref={toast} />

      <form className="p-fluid" onSubmit={handleSubmit} aria-label="Login form">
        <div className="p-field">
          <label htmlFor="login-email" className="p-d-block">
            Email
          </label>
          <InputText
            id="login-email"
            value={email}
            onChange={e => setEmail((e.target as HTMLInputElement).value)}
            placeholder="you@example.com"
            autoComplete="username"
          />
        </div>

        <div className="p-field" style={{ marginTop: 12 }}>
          <label htmlFor="login-password" className="p-d-block">
            Password
          </label>

          <div className="p-inputgroup">
            <InputText
              id="login-password"
              type={showPassword ? 'text' : 'password'}
              value={password}
              onChange={e => setPassword((e.target as HTMLInputElement).value)}
              placeholder="Enter your password"
              autoComplete="current-password"
            />
            <Button
              type="button"
              icon={showPassword ? 'pi pi-eye-slash' : 'pi pi-eye'}
              className="p-button-text"
              onClick={() => setShowPassword(s => !s)}
              aria-label={showPassword ? 'Hide password' : 'Show password'}
            />
          </div>
        </div>

        <div
          className="p-field"
          style={{ marginTop: 18, display: 'flex', gap: 8, justifyContent: 'flex-end' }}
        >
          <Button
            type="button"
            label="Demo"
            icon="pi pi-user"
            className="p-button-text"
            onClick={handleDemo}
            disabled={loading}
          />
          <Button
            type="submit"
            label={loading ? 'Signing in...' : 'Sign in'}
            icon="pi pi-sign-in"
            disabled={loading}
          />
        </div>
      </form>
    </div>
  );
}
