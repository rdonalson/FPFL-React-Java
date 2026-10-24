import { beforeEach, describe, expect, it, vi } from 'vitest';
import { act, render, screen } from '@testing-library/react';
import { MemoryRouter, Route, Routes, useLocation } from 'react-router-dom';

vi.mock('@/app/auth/api/authApi', () => ({
  authApi: { refresh: vi.fn().mockResolvedValue(null), login: vi.fn(), register: vi.fn() },
}));
// App's own behavior, not every page: swap the router for a probe
vi.mock('@/app/router/AppRouter', () => ({
  AppRouter: () => {
    const location = useLocation();
    return <div data-testid="path">{location.pathname}</div>;
  },
}));

import App from './App';
import { useThemeStore } from './state/themeStore';
import { signIn, signOut } from '@/test/session';

beforeEach(() => signOut());

function renderApp(path = '/query/display') {
  render(
    <MemoryRouter initialEntries={[path]}>
      <Routes>
        <Route path="*" element={<App />} />
      </Routes>
    </MemoryRouter>,
  );
}

describe('App', () => {
  it('applies the saved theme on startup', () => {
    useThemeStore.setState({ dark: true });
    renderApp();
    expect(document.documentElement.getAttribute('data-theme')).toBe('dark');
  });

  it('returns home when the session ends', () => {
    signIn();
    renderApp('/query/display');
    expect(screen.getByTestId('path')).toHaveTextContent('/query/display');

    act(() => {
      window.dispatchEvent(new Event('session-logged-out'));
    });
    expect(screen.getByTestId('path')).toHaveTextContent(/^\/$/);
  });
});
