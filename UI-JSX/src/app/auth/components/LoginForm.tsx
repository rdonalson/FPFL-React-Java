// src/app/auth/LoginForm.tsx
import { useForm, Controller } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { loginSchema, LoginFormValues } from '../schemas/loginSchema';
import { loginApi } from '@/app/auth/api/authApi';
import { Password } from 'primereact/password';

interface LoginFormProps {
  onSuccess: (session: any) => void;
}

export function LoginForm({ onSuccess }: LoginFormProps) {
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
    const session = await loginApi(values.email, values.password);
    onSuccess(session);
  };

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="flex flex-column gap-3">
      {/* Email */}
      <div>
        <label>Email</label>
        <input {...register('email')} className="p-inputtext w-full" />
        {errors.email && <small className="p-error">{errors.email.message}</small>}
      </div>

      {/* Password — FIXED */}
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

      <button type="submit" className="p-button p-component w-full" disabled={isSubmitting}>
        {isSubmitting ? 'Logging in...' : 'Login'}
      </button>
    </form>
  );
}
