import { beforeEach, describe, expect, it, vi } from 'vitest';
import { act, fireEvent, render, screen, waitFor } from '@testing-library/react';

const api = vi.hoisted(() => ({ login: vi.fn(), register: vi.fn(), refresh: vi.fn() }));
vi.mock('@/app/auth/api/authApi', () => ({ authApi: api }));
const authClient = vi.hoisted(() => ({ changePassword: vi.fn() }));
vi.mock('@/api/generated/AuthClient', () => ({ AuthClient: authClient }));

import { LoginForm } from './LoginForm';
import { RegisterForm } from './RegisterForm';
import { ChangePasswordForm } from './ChangePasswordForm';
import { SessionExpireDialog } from './SessionExpireDialog';
import { useSessionStore } from '@/app/state/sessionStore';

const type = (label: string, value: string) =>
  fireEvent.change(screen.getByLabelText(label), { target: { value } });

beforeEach(() => {
  vi.clearAllMocks();
  useSessionStore.getState().clearSession();
});

describe('LoginForm', () => {
  it('signs in with what was typed', async () => {
    const onSuccess = vi.fn();
    api.login.mockResolvedValue({ accessToken: 'a', userId: 'u' });
    render(<LoginForm onSuccess={onSuccess} initialEmail="rick@example.com" />);

    expect(screen.getByLabelText('Email')).toHaveValue('rick@example.com');
    type('Password', 'secret');
    fireEvent.click(screen.getByRole('button', { name: 'Sign in' }));

    await waitFor(() => expect(onSuccess).toHaveBeenCalledWith({ accessToken: 'a', userId: 'u' }));
    expect(api.login).toHaveBeenCalledWith('rick@example.com', 'secret');
  });

  it('shows or hides the password', () => {
    render(<LoginForm onSuccess={vi.fn()} />);
    expect(screen.getByLabelText('Password')).toHaveAttribute('type', 'password');
    fireEvent.click(screen.getByRole('button', { name: 'Show password' }));
    expect(screen.getByLabelText('Password')).toHaveAttribute('type', 'text');
  });

  it('reports a failed login and does not sign in', async () => {
    const onSuccess = vi.fn();
    api.login.mockRejectedValue(new Error('Invalid email or password'));
    render(<LoginForm onSuccess={onSuccess} />);

    fireEvent.click(screen.getByRole('button', { name: 'Sign in' }));

    expect(await screen.findByText('Invalid email or password')).toBeInTheDocument();
    expect(onSuccess).not.toHaveBeenCalled();
  });

  it('the Demo button uses the guest account', async () => {
    const onSuccess = vi.fn();
    api.login.mockResolvedValue({ accessToken: 'a' });
    render(<LoginForm onSuccess={onSuccess} />);

    fireEvent.click(screen.getByRole('button', { name: 'Demo' }));

    await waitFor(() => expect(onSuccess).toHaveBeenCalled());
    expect(api.login).toHaveBeenCalledWith('guest.user@gmail.com', expect.any(String));
  });
});

describe('RegisterForm', () => {
  it('validates before calling the API', async () => {
    render(<RegisterForm onSuccess={vi.fn()} />);

    fireEvent.click(screen.getByRole('button', { name: 'Register' }));

    expect(await screen.findByText('First name is required')).toBeInTheDocument();
    expect(screen.getByText('Invalid email')).toBeInTheDocument();
    expect(api.register).not.toHaveBeenCalled();
  });

  it('flags mismatched passwords', async () => {
    render(<RegisterForm onSuccess={vi.fn()} />);
    type('First Name', 'Rick');
    type('Last Name', 'D');
    type('Email', 'rick@example.com');
    type('Password', 'Password@2');
    type('Confirm Password', 'Password@3');

    fireEvent.click(screen.getByRole('button', { name: 'Register' }));

    expect(await screen.findByText('Passwords do not match')).toBeInTheDocument();
    expect(api.register).not.toHaveBeenCalled();
  });

  it('registers and hands back the new session', async () => {
    vi.useFakeTimers({ shouldAdvanceTime: true });
    const onSuccess = vi.fn();
    api.register.mockResolvedValue({ userId: 'new' });
    render(<RegisterForm onSuccess={onSuccess} />);
    type('First Name', 'Rick');
    type('Last Name', 'D');
    type('Email', 'rick@example.com');
    type('Password', 'Password@2');
    type('Confirm Password', 'Password@2');

    fireEvent.click(screen.getByRole('button', { name: 'Register' }));

    await waitFor(() =>
      expect(api.register).toHaveBeenCalledWith('Rick', 'D', 'rick@example.com', 'Password@2'),
    );
    await act(() => vi.advanceTimersByTimeAsync(700));
    expect(onSuccess).toHaveBeenCalledWith({ userId: 'new' });
    vi.useRealTimers();
  });
});

describe('ChangePasswordForm', () => {
  it('catches mismatched new passwords locally', async () => {
    render(<ChangePasswordForm id={7} onSuccess={vi.fn()} />);
    type('Current Password', 'old');
    type('New Password', 'new-one-1');
    type('Confirm Password', 'new-one-2');

    fireEvent.click(screen.getByRole('button', { name: 'Update Password' }));

    expect(await screen.findByText('New passwords do not match')).toBeInTheDocument();
    expect(authClient.changePassword).not.toHaveBeenCalled();
  });

  it('changes the password and closes', async () => {
    vi.useFakeTimers({ shouldAdvanceTime: true });
    const onSuccess = vi.fn();
    authClient.changePassword.mockResolvedValue({ status: 200, data: { ok: true } });
    render(<ChangePasswordForm id={7} onSuccess={onSuccess} />);
    type('Current Password', 'old');
    type('New Password', 'new-one-1');
    type('Confirm Password', 'new-one-1');

    fireEvent.click(screen.getByRole('button', { name: 'Update Password' }));

    await waitFor(() =>
      expect(authClient.changePassword).toHaveBeenCalledWith({
        id: 7,
        currentPassword: 'old',
        newPassword: 'new-one-1',
      }),
    );
    await act(() => vi.advanceTimersByTimeAsync(900));
    expect(onSuccess).toHaveBeenCalled();
    vi.useRealTimers();
  });

  it("shows the API's reason when it refuses", async () => {
    authClient.changePassword.mockRejectedValue(new Error('Current password is incorrect'));
    render(<ChangePasswordForm id={7} onSuccess={vi.fn()} />);
    type('Current Password', 'wrong');
    type('New Password', 'n');
    type('Confirm Password', 'n');

    fireEvent.click(screen.getByRole('button', { name: 'Update Password' }));

    expect(await screen.findAllByText('Current password is incorrect')).not.toHaveLength(0);
  });

  it('Cancel clears and closes', () => {
    const onSuccess = vi.fn();
    render(<ChangePasswordForm id={7} onSuccess={onSuccess} />);
    type('Current Password', 'old');
    fireEvent.click(screen.getByRole('button', { name: 'Cancel' }));
    expect(onSuccess).toHaveBeenCalled();
    expect(screen.getByLabelText('Current Password')).toHaveValue('');
  });
});

describe('SessionExpireDialog', () => {
  const session = {
    accessToken: 'a',
    refreshToken: 'r-1',
    id: 1,
    userId: 'u',
    email: 'e',
    first: 'f',
    last: 'l',
    roles: [],
    raw: {},
  };

  it('Stay Logged In refreshes the token and hides the dialog', async () => {
    useSessionStore.getState().setSession(session);
    api.refresh.mockResolvedValue({ accessToken: 'b' });
    const onHide = vi.fn();
    render(<SessionExpireDialog visible onHide={onHide} />);

    fireEvent.click(await screen.findByRole('button', { name: 'Stay Logged In' }));

    await waitFor(() => expect(onHide).toHaveBeenCalled());
    expect(api.refresh).toHaveBeenCalledWith('r-1');
  });

  it('Logout ends the session', async () => {
    useSessionStore.getState().setSession(session);
    render(<SessionExpireDialog visible onHide={vi.fn()} />);

    fireEvent.click(await screen.findByRole('button', { name: 'Logout' }));

    expect(useSessionStore.getState().isAuthenticated).toBe(false);
  });
});
