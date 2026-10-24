import { beforeEach, describe, expect, it, vi } from 'vitest';
import { act, fireEvent, render, screen, waitFor } from '@testing-library/react';

const api = vi.hoisted(() => ({ login: vi.fn(), register: vi.fn(), refresh: vi.fn() }));
vi.mock('@/app/auth/api/authApi', () => ({ authApi: api }));
const initial = vi.hoisted(() => ({ createInitialAmount: vi.fn() }));
vi.mock('@/features/catalog-command/transactions/api/initialAmountApi', () => initial);

import { LoginDialog } from './LoginDialog';
import { RegisterDialog } from './RegisterDialog';
import { ChangePasswordDialog } from './ChangePasswordDialog';
import { useSessionStore } from '@/app/state/sessionStore';
import { signOut } from '@/test/session';

const auth = {
  accessToken: 'a',
  refreshToken: 'r',
  id: 1,
  userId: 'new-user',
  email: 'new@example.com',
  first: 'New',
  last: 'User',
  roles: ['ROLE_USER'],
};
const type = (label: string, value: string) =>
  fireEvent.change(screen.getByLabelText(label), { target: { value } });

beforeEach(() => {
  vi.clearAllMocks();
  signOut();
});

describe('LoginDialog', () => {
  it('stores the session and closes after signing in', async () => {
    api.login.mockResolvedValue(auth);
    const onHide = vi.fn();
    render(<LoginDialog visible onHide={onHide} />);

    expect(await screen.findByText('Login')).toBeInTheDocument();
    type('Email', 'new@example.com');
    type('Password', 'pw');
    fireEvent.click(screen.getByRole('button', { name: 'Sign in' }));

    await waitFor(() => expect(onHide).toHaveBeenCalled());
    expect(useSessionStore.getState().userId).toBe('new-user');
  });
});

describe('RegisterDialog', () => {
  it('signs the new user in and gives them a $0 initial amount', async () => {
    vi.useFakeTimers({ shouldAdvanceTime: true });
    api.register.mockResolvedValue({ ...auth, raw: auth });
    initial.createInitialAmount.mockResolvedValue({ id: 1, amount: 0 });
    const onHide = vi.fn();
    render(<RegisterDialog visible onHide={onHide} />);

    expect(await screen.findByText('Create Account')).toBeInTheDocument();
    type('First Name', 'New');
    type('Last Name', 'User');
    type('Email', 'new@example.com');
    type('Password', 'Password@2');
    type('Confirm Password', 'Password@2');
    fireEvent.click(screen.getByRole('button', { name: 'Register' }));

    await waitFor(() => expect(api.register).toHaveBeenCalled());
    await act(() => vi.advanceTimersByTimeAsync(700));
    await waitFor(() => expect(onHide).toHaveBeenCalled());
    expect(initial.createInitialAmount).toHaveBeenCalledWith(0);
    expect(useSessionStore.getState().userId).toBe('new-user');
    vi.useRealTimers();
  });
});

describe('ChangePasswordDialog', () => {
  it('shows the change password form', async () => {
    render(<ChangePasswordDialog visible onHide={vi.fn()} id={1} />);
    expect(await screen.findByLabelText('Current Password')).toBeInTheDocument();
  });
});
