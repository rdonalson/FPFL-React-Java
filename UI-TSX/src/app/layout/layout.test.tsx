import { beforeEach, describe, expect, it, vi } from 'vitest';
import { fireEvent, render, screen, within } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { HelmetProvider } from 'react-helmet-async';

const api = vi.hoisted(() => ({
  login: vi.fn(),
  refresh: vi.fn().mockResolvedValue(null),
  register: vi.fn(),
}));
vi.mock('@/app/auth/api/authApi', () => ({ authApi: api }));

import { AppTopMenu } from './AppTopMenu';
import AppSidebar from './AppSidebar';
import AppLayout from './AppLayout';
import AppFooter from './AppFooter';
import { useSessionStore } from '@/app/state/sessionStore';
import { useThemeStore } from '@/app/state/themeStore';
import { signIn, signOut } from '@/test/session';

function renderTopMenu(path = '/') {
  const onToggle = vi.fn();
  render(
    <MemoryRouter initialEntries={[path]}>
      <Routes>
        <Route path="*" element={<AppTopMenu onToggleSidebar={onToggle} />} />
      </Routes>
    </MemoryRouter>,
  );
  return onToggle;
}

beforeEach(() => {
  vi.clearAllMocks();
  signOut();
});

describe('AppTopMenu', () => {
  it('offers Guest Login, Register and Login when signed out', () => {
    renderTopMenu();

    expect(screen.getByRole('button', { name: 'Guest Login' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Register' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Login' })).toBeInTheDocument();
    expect(screen.queryByRole('navigation', { name: 'Main' })).not.toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'Open menu' })).not.toBeInTheDocument();
  });

  it('opens the login dialog', async () => {
    renderTopMenu();
    fireEvent.click(screen.getByRole('button', { name: 'Login' }));
    expect(await screen.findByLabelText('Email')).toBeInTheDocument();
  });

  it('shows the user, the Actions links and Change Password when signed in', () => {
    signIn(['ROLE_USER']);
    const onToggle = renderTopMenu('/query/display');

    expect(screen.getByText('Rick Donalson')).toBeInTheDocument();
    const nav = screen.getByRole('navigation', { name: 'Main' });
    expect(within(nav).getByRole('link', { name: 'Display' })).toHaveClass('is-active');
    expect(within(nav).getByRole('link', { name: 'Debt Paydown' })).toHaveAttribute(
      'href',
      '/command/transactions/debt-paydown',
    );
    expect(within(nav).queryByRole('button', { name: /Admin/ })).not.toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Change Password' })).toBeInTheDocument();

    fireEvent.click(screen.getByRole('button', { name: 'Open menu' }));
    expect(onToggle).toHaveBeenCalled();
  });

  it('gives admins the Admin menu', () => {
    signIn(['ROLE_USER', 'ROLE_ADMIN']);
    renderTopMenu('/status');
    expect(screen.getByRole('button', { name: /Admin/ })).toHaveClass('is-active');
  });

  it('hides Change Password from the shared guest account', () => {
    signIn(['ROLE_USER'], { email: 'Guest.User@gmail.com' });
    renderTopMenu();
    expect(screen.queryByRole('button', { name: 'Change Password' })).not.toBeInTheDocument();
  });

  it('Logout ends the session', () => {
    signIn();
    renderTopMenu();
    fireEvent.click(screen.getByRole('button', { name: 'Logout' }));
    expect(useSessionStore.getState().isAuthenticated).toBe(false);
  });

  it('toggles dark mode', () => {
    useThemeStore.getState().setTheme(false);
    renderTopMenu();
    fireEvent.click(screen.getByRole('button', { name: 'Switch to dark mode' }));
    expect(useThemeStore.getState().dark).toBe(true);
    expect(screen.getByRole('button', { name: 'Switch to light mode' })).toBeInTheDocument();
  });
});

describe('AppSidebar', () => {
  it('renders nothing when signed out', () => {
    const { container } = render(
      <MemoryRouter>
        <AppSidebar visible onHide={vi.fn()} />
      </MemoryRouter>,
    );
    expect(container).toBeEmptyDOMElement();
  });

  it('lists the role-filtered sections and closes when a link is picked', async () => {
    signIn(['ROLE_USER', 'ROLE_ADMIN']);
    const onHide = vi.fn();
    render(
      <MemoryRouter>
        <AppSidebar visible onHide={onHide} />
      </MemoryRouter>,
    );

    expect(await screen.findByText('Actions')).toBeInTheDocument();
    expect(screen.getByText('Admin')).toBeInTheDocument();
    fireEvent.click(screen.getByRole('link', { name: /Credits/ }));
    expect(onHide).toHaveBeenCalled();
  });

  it('regular users only get Actions', async () => {
    signIn(['ROLE_USER']);
    render(
      <MemoryRouter>
        <AppSidebar visible onHide={vi.fn()} />
      </MemoryRouter>,
    );
    expect(await screen.findByText('Actions')).toBeInTheDocument();
    expect(screen.queryByText('Time Periods')).not.toBeInTheDocument();
  });
});

describe('AppLayout', () => {
  it('wraps the page in the header, main and footer', () => {
    render(
      <HelmetProvider>
        <MemoryRouter initialEntries={['/docs']}>
          <Routes>
            <Route element={<AppLayout />}>
              <Route path="/docs" element={<p>Page body</p>} />
            </Route>
          </Routes>
        </MemoryRouter>
      </HelmetProvider>,
    );

    expect(screen.getByRole('link', { name: 'FPFL Platform home' })).toBeInTheDocument();
    expect(screen.getByRole('main')).toHaveTextContent('Page body');
    expect(screen.getByRole('main')).toHaveClass('p-4');
    expect(screen.getByText('© 2026 Rick Donalson')).toBeInTheDocument();
  });
});

describe('AppFooter', () => {
  it('shows the credit and stack', () => {
    render(<AppFooter />);
    expect(screen.getByText(/Spring Boot/)).toBeInTheDocument();
  });
});
