// src/app/auth/components/RegisterForm.tsx
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { registerSchema, RegisterFormValues } from '../schemas/registerSchema';
import { registerApi } from '@/app/auth/api/authApi';

interface RegisterFormProps {
  onSuccess: (session: any) => void;
}

export function RegisterForm({ onSuccess }: RegisterFormProps) {
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<RegisterFormValues>({
    resolver: zodResolver(registerSchema),
  });

  const onSubmit = async (values: RegisterFormValues) => {
    const session = await registerApi(values.first, values.last, values.email, values.password);

    onSuccess(session);
  };

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="flex flex-column gap-3">
      <div>
        <label>First Name</label>
        <input {...register('first')} className="p-inputtext w-full" />
        {errors.first && <small className="p-error">{errors.first.message}</small>}
      </div>

      <div>
        <label>Last Name</label>
        <input {...register('last')} className="p-inputtext w-full" />
        {errors.last && <small className="p-error">{errors.last.message}</small>}
      </div>

      <div>
        <label>Email</label>
        <input {...register('email')} className="p-inputtext w-full" />
        {errors.email && <small className="p-error">{errors.email.message}</small>}
      </div>

      <div>
        <label>Password</label>
        <input type="password" {...register('password')} className="p-inputtext w-full" />
        {errors.password && <small className="p-error">{errors.password.message}</small>}
      </div>

      <button type="submit" className="p-button p-component w-full" disabled={isSubmitting}>
        {isSubmitting ? 'Registering...' : 'Register'}
      </button>
    </form>
  );
}
