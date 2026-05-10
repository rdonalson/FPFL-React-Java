// src/app/auth/LoginDialog.tsx
import { useRef } from 'react';
import { Dialog } from 'primereact/dialog';
import { InputText } from 'primereact/inputtext';
import { Password } from 'primereact/password';
import { Button } from 'primereact/button';
import { Toast } from 'primereact/toast';
import { useNavigate } from 'react-router-dom';

import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';

import { loginSchema, LoginFormValues } from '../schemas/loginSchema';
//C:\Users\rickd\source\repos\FPFL-React-Java\UI-JSX\src\api\authApi.ts
import { loginApi } from '../api/authApi';
import { normalizeAuthResponse } from '@/api/utils/normalizeAuthResponse';
import { useSessionStore } from '@/app/state/sessionStore';

interface LoginDialogProps {
  visible: boolean;
  onHide: () => void;
}

export default function LoginDialog({ visible, onHide }: LoginDialogProps) {
  const navigate = useNavigate();
  const toast = useRef<Toast>(null);

  const setSession = useSessionStore(s => s.setSession);

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<LoginFormValues>({
    resolver: zodResolver(loginSchema),
  });

  async function onSubmit(values: LoginFormValues) {
    try {
      // 1. Call backend
      const result = await loginApi(values.email, values.password);

      // 2. Normalize backend → session shape
      const normalized = normalizeAuthResponse(result);

      // 3. Store in session
      setSession(normalized);

      // 4. Toast + redirect
      toast.current?.show({
        severity: 'success',
        summary: 'Logged in',
        detail: `Welcome back, ${normalized.first}`,
        life: 2000,
      });

      onHide();
      navigate('/');
    } catch (err: any) {
      toast.current?.show({
        severity: 'error',
        summary: 'Login failed',
        detail: err?.message ?? 'Invalid email or password',
        life: 4000,
      });
    }
  }

  return (
    <Dialog header="Login" visible={visible} onHide={onHide} modal style={{ width: '25rem' }}>
      <Toast ref={toast} />

      <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-3">
        <div>
          <span className="p-float-label">
            <InputText
              id="email"
              {...register('email')}
              className={errors.email ? 'p-invalid' : ''}
            />
            <label htmlFor="email">Email</label>
          </span>
          {errors.email && <small className="p-error">{errors.email.message}</small>}
        </div>

        <div>
          <span className="p-float-label">
            <Password
              id="password"
              {...register('password')}
              toggleMask
              className={errors.password ? 'p-invalid' : ''}
            />
            <label htmlFor="password">Password</label>
          </span>
          {errors.password && <small className="p-error">{errors.password.message}</small>}
        </div>

        <Button
          type="submit"
          label={isSubmitting ? 'Logging in...' : 'Login'}
          icon="pi pi-check"
          loading={isSubmitting}
        />
      </form>
    </Dialog>
  );
}
