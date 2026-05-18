import { useForm, Controller } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { loginSchema, LoginFormValues } from '../schemas/loginSchema';
import { authApi } from '@/app/auth/api/authApi';
import { Password } from 'primereact/password';
import { Message } from 'primereact/message';
import { useState } from 'react';

interface LoginFormProps {
  onSuccess: (session: any) => void;
}

export function LoginForm({ onSuccess }: LoginFormProps) {
  const [apiError, setApiError] = useState<string | null>(null);

  const {
    control,
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<LoginFormValues>({
    resolver: zodResolver(loginSchema),
    defaultValues: {
      email: '',
      password: '',
    },
  });

  const onSubmit = async (values: LoginFormValues) => {
    setApiError(null);

    try {
      const session = await authApi.login(values.email, values.password);
      onSuccess(session);
    } catch (err: any) {
      if (err.status === 401) {
        setApiError(err.message); // "Invalid credentials"
        return;
      }

      console.error(err);
    }
  };

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="flex flex-column gap-3">
      {/* Email */}
      <div>
        <label>Email</label>
        <input {...register('email')} className="p-inputtext w-full" />
        {errors.email && <small className="p-error">{errors.email.message}</small>}
      </div>

      {/* Password */}
      <div>
        <label>Password</label>

        <Controller
          name="password"
          control={control}
          render={({ field }) => (
            <Password
              value={field.value || ''}
              onChange={e => field.onChange(e.target.value)}
              toggleMask
              className={errors.password ? 'p-invalid' : ''}
              inputClassName="w-full"
            />
          )}
        />

        {errors.password && <small className="p-error">{errors.password.message}</small>}
      </div>

      {/* API Error */}
      {apiError && <Message severity="error" text={apiError} className="mt-1" />}

      <button type="submit" className="p-button p-component w-full" disabled={isSubmitting}>
        {isSubmitting ? 'Logging in...' : 'Login'}
      </button>
    </form>
  );
}
