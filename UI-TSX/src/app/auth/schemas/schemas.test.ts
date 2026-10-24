import { describe, expect, it } from 'vitest';
import { loginSchema } from './loginSchema';
import { registerSchema } from './registerSchema';

const messages = (result: { success: boolean; error?: { issues: { message: string }[] } }) =>
  result.success ? [] : result.error!.issues.map(i => i.message);

describe('loginSchema', () => {
  it('needs a valid email and a password', () => {
    expect(loginSchema.safeParse({ email: 'rick@example.com', password: 'x' }).success).toBe(true);
    expect(messages(loginSchema.safeParse({ email: 'nope', password: '' }))).toEqual([
      'Invalid email',
      'Password is required',
    ]);
  });
});

describe('registerSchema', () => {
  const valid = {
    first: 'Rick',
    last: 'D',
    email: 'rick@example.com',
    password: 'Password@2',
    confirmPassword: 'Password@2',
  };

  it('accepts a complete registration', () => {
    expect(registerSchema.safeParse(valid).success).toBe(true);
  });

  it('requires names, a real email and 8+ character passwords', () => {
    const result = registerSchema.safeParse({
      first: '',
      last: '',
      email: 'x',
      password: 'short',
      confirmPassword: 'short',
    });
    expect(messages(result)).toEqual(
      expect.arrayContaining([
        'First name is required',
        'Last name is required',
        'Invalid email',
        'Password must be at least 8 characters',
        'Please confirm your password',
      ]),
    );
  });

  it('flags mismatched passwords on the confirm field', () => {
    const result = registerSchema.safeParse({ ...valid, confirmPassword: 'Password@3' });
    expect(result.success).toBe(false);
    expect(result.error!.issues[0]).toMatchObject({
      message: 'Passwords do not match',
      path: ['confirmPassword'],
    });
  });
});
