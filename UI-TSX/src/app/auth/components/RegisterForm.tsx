// src/app/auth/components/RegisterForm.tsx
import { useRef, useState } from 'react';
import { Toast } from 'primereact/toast';
import { InputText } from 'primereact/inputtext';
import { Button } from 'primereact/button';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';

import { registerSchema, RegisterFormValues } from '../schemas/registerSchema';
import { authApi } from '@/app/auth/api/authApi';

interface RegisterFormProps {
  onSuccess: (session: any) => void;
}

export function RegisterForm({ onSuccess }: RegisterFormProps) {
  const toast = useRef<Toast | null>(null);

  const [showPassword, setShowPassword] = useState(false);
  const [showConfirm, setShowConfirm] = useState(false);

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<RegisterFormValues>({
    resolver: zodResolver(registerSchema),
  });

  const onSubmit = async (values: RegisterFormValues) => {
    try {
      const session = await authApi.register(
        values.first,
        values.last,
        values.email,
        values.password,
      );

      toast.current?.show({
        severity: 'success',
        summary: 'Account Created',
        detail: 'Welcome!',
        life: 2500,
      });

      setTimeout(() => onSuccess(session), 600);
    } catch (err: any) {
      toast.current?.show({
        severity: 'error',
        summary: 'Registration failed',
        detail: err?.message ?? 'Unknown error',
        life: 4000,
      });
    }
  };

  return (
    <div style={{ width: '100%' }}>
      <Toast ref={toast} />

      <form className="auth-form-grid" onSubmit={handleSubmit(onSubmit)} style={{ width: '100%' }}>
        {/* First Name */}
        <label htmlFor="first" className="form-label">
          First Name
        </label>
        <div className="form-field">
          <InputText id="first" {...register('first')} style={{ width: '100%' }} />
          {errors.first && <small className="p-error">{errors.first.message}</small>}
        </div>

        {/* Last Name */}
        <label htmlFor="last" className="form-label">
          Last Name
        </label>
        <div className="form-field">
          <InputText id="last" {...register('last')} style={{ width: '100%' }} />
          {errors.last && <small className="p-error">{errors.last.message}</small>}
        </div>

        {/* Email */}
        <label htmlFor="email" className="form-label">
          Email
        </label>
        <div className="form-field">
          <InputText
            id="email"
            {...register('email')}
            autoComplete="email"
            style={{ width: '100%' }}
          />
          {errors.email && <small className="p-error">{errors.email.message}</small>}
        </div>

        {/* Password */}
        <label htmlFor="password" className="form-label">
          Password
        </label>
        <div className="form-field" style={{ display: 'flex', gap: 8 }}>
          <InputText
            id="password"
            type={showPassword ? 'text' : 'password'}
            {...register('password')}
            autoComplete="new-password"
            style={{ flex: 1 }}
          />
          <Button
            type="button"
            icon={showPassword ? 'pi pi-eye-slash' : 'pi pi-eye'}
            className="p-button-text"
            onClick={() => setShowPassword(s => !s)}
          />
        </div>
        {errors.password && (
          <small className="p-error" style={{ gridColumn: '2 / 3' }}>
            {errors.password.message}
          </small>
        )}

        {/* Confirm Password */}
        <label htmlFor="confirmPassword" className="form-label">
          Confirm Password
        </label>
        <div className="form-field" style={{ display: 'flex', gap: 8 }}>
          <InputText
            id="confirmPassword"
            type={showConfirm ? 'text' : 'password'}
            {...register('confirmPassword')}
            autoComplete="new-password"
            style={{ flex: 1 }}
          />
          <Button
            type="button"
            icon={showConfirm ? 'pi pi-eye-slash' : 'pi pi-eye'}
            className="p-button-text"
            onClick={() => setShowConfirm(s => !s)}
          />
        </div>
        {errors.confirmPassword && (
          <small className="p-error" style={{ gridColumn: '2 / 3' }}>
            {errors.confirmPassword.message}
          </small>
        )}

        {/* Actions */}
        <div
          className="form-actions"
          style={{
            gridColumn: '1 / -1',
            display: 'flex',
            justifyContent: 'flex-end',
            marginTop: 16,
          }}
        >
          <Button
            type="submit"
            label={isSubmitting ? 'Registering...' : 'Register'}
            icon="pi pi-check"
            disabled={isSubmitting}
          />
        </div>
      </form>
    </div>
  );
}
