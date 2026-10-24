import { beforeEach, describe, expect, it } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import AuthGate from './AuthGate';
import { AdminRouteGuard } from './AdminRouteGuard';
import { useSessionStore } from '@/app/state/sessionStore';

function renderAt(path: string) {
  render(
    <MemoryRouter initialEntries={[path]}>
      <Routes>
        <Route path="/" element={<div>Home page</div>} />
        <Route element={<AuthGate />}>
          <Route path="/query/display" element={<div>Display page</div>} />
          <Route element={<AdminRouteGuard />}>
            <Route path="/status" element={<div>Status page</div>} />
          </Route>
        </Route>
      </Routes>
    </MemoryRouter>,
  );
}

function signIn(roles: string[]) {
  sessionStorage.setItem('accessToken', 'access');
  sessionStorage.setItem('roles', JSON.stringify(roles));
  useSessionStore.getState().setSession({
    accessToken: 'access',
    refreshToken: '',
    id: 1,
    userId: 'u',
    email: 'e',
    first: 'f',
    last: 'l',
    roles,
    raw: {},
  });
}

beforeEach(() => {
  sessionStorage.clear();
  useSessionStore.getState().clearSession();
});

describe('route guards', () => {
  it('send signed-out visitors home', () => {
    renderAt('/query/display');
    expect(screen.getByText('Home page')).toBeInTheDocument();
  });

  it('let signed-in users into authenticated pages', () => {
    signIn(['ROLE_USER']);
    renderAt('/query/display');
    expect(screen.getByText('Display page')).toBeInTheDocument();
  });

  it('keep non-admins out of admin pages', () => {
    signIn(['ROLE_USER']);
    renderAt('/status');
    expect(screen.getByText('Home page')).toBeInTheDocument();
  });

  it('let admins into admin pages', () => {
    signIn(['ROLE_USER', 'ROLE_ADMIN']);
    renderAt('/status');
    expect(screen.getByText('Status page')).toBeInTheDocument();
  });
});
