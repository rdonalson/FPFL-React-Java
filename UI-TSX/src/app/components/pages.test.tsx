import { beforeEach, describe, expect, it, vi } from 'vitest';
import { fireEvent, render, screen, waitFor, within } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';

const api = vi.hoisted(() => ({
  login: vi.fn(),
  refresh: vi.fn().mockResolvedValue(null),
  register: vi.fn(),
}));
vi.mock('@/app/auth/api/authApi', () => ({ authApi: api }));
const health = vi.hoisted(() => ({ fetchHealth: vi.fn(), fetchInfo: vi.fn() }));
vi.mock('@/api/status/healthApi', () => ({
  API_BASE_URL: 'https://api-jdk.ledger-finance.com',
  HEALTH_URL: 'https://api-jdk.ledger-finance.com/actuator/health',
  ...health,
}));

import { HomePage } from './HomePage';
import DocsPage from './DocsPage';
import StatusPage from './StatusPage';
import { signIn, signOut } from '@/test/session';

const inRouter = (ui: React.ReactNode) => render(<MemoryRouter>{ui}</MemoryRouter>);

const probe = <T,>(
  over: Partial<{
    ok: boolean;
    httpStatus: number;
    body: T;
    error: string;
    hint: string;
    blocked: boolean;
  }>,
) => ({
  ok: true,
  httpStatus: 200,
  latencyMs: 12,
  url: 'https://api-jdk.ledger-finance.com/actuator/health',
  checkedAt: new Date(),
  ...over,
});

beforeEach(() => {
  vi.clearAllMocks();
  signOut();
});

describe('HomePage', () => {
  it('invites signed-out visitors to try it as a guest', () => {
    inRouter(<HomePage />);
    expect(screen.getByRole('button', { name: 'Try it as a guest' })).toBeInTheDocument();
    expect(screen.getByText(/Log in, or try it as a guest/)).toBeInTheDocument();
  });

  it('the guest button signs in with the demo account', async () => {
    api.login.mockResolvedValue({ accessToken: 'a', userId: 'g', roles: [] });
    inRouter(<HomePage />);
    fireEvent.click(screen.getByRole('button', { name: 'Try it as a guest' }));
    await waitFor(() =>
      expect(api.login).toHaveBeenCalledWith('guest.user@gmail.com', expect.any(String)),
    );
  });

  it('lists every quick action, including Debt Paydown', () => {
    signIn();
    inRouter(<HomePage />);

    expect(screen.getByRole('button', { name: /Open forecast/ })).toBeInTheDocument();
    for (const [name, href] of [
      ['Display', '/query/display'],
      ['Initial Amount', '/command/transactions/initial-amount'],
      ['Credits', '/command/transactions/credits'],
      ['Debits', '/command/transactions/debits'],
      ['Debt Paydown', '/command/transactions/debt-paydown'],
    ]) {
      expect(screen.getByRole('link', { name: new RegExp(`^${name}`) })).toHaveAttribute(
        'href',
        href,
      );
    }
    expect(screen.queryByText('Admin')).not.toBeInTheDocument();
  });

  it('adds the Admin links for admins', () => {
    signIn(['ROLE_USER', 'ROLE_ADMIN']);
    inRouter(<HomePage />);
    expect(screen.getByText('Admin')).toBeInTheDocument();
    expect(screen.getByRole('link', { name: 'System Status' })).toHaveAttribute('href', '/status');
  });
});

describe('User Guide links', () => {
  it('the home page links the PDF guide from the hero and the New here? card', () => {
    inRouter(<HomePage />);
    expect(screen.getByRole('link', { name: /User Guide/ })).toHaveAttribute(
      'href',
      '/media/fpfl-user-guide.pdf',
    );
    const cardLink = screen.getByRole('link', { name: /Read the user guide/ });
    expect(cardLink).toHaveAttribute('href', '/media/fpfl-user-guide.pdf');
    expect(cardLink).toHaveAttribute('target', '_blank');
  });

  it('the docs page opens the guide in a new tab', () => {
    const open = vi.spyOn(window, 'open').mockImplementation(() => null);
    inRouter(<DocsPage />);
    fireEvent.click(screen.getByRole('button', { name: /User Guide/ }));
    expect(open).toHaveBeenCalledWith('/media/fpfl-user-guide.pdf', '_blank', 'noopener');
    open.mockRestore();
  });
});

describe('DocsPage', () => {
  it('opens on the overview and switches sections', async () => {
    inRouter(<DocsPage />);

    expect(screen.getByText('What this system does')).toBeInTheDocument();

    const tabs = screen.getByRole('tablist', { name: 'Documentation sections' });
    fireEvent.mouseDown(within(tabs).getByRole('tab', { name: /Ledger Engine/ }));
    fireEvent.click(within(tabs).getByRole('tab', { name: /Ledger Engine/ }));
    expect(await screen.findByText('How a forecast is built')).toBeInTheDocument();
    expect(screen.getByRole('heading', { name: 'Debt Paydown' })).toBeInTheDocument();

    fireEvent.mouseDown(within(tabs).getByRole('tab', { name: /Data Model/ }));
    fireEvent.click(within(tabs).getByRole('tab', { name: /Data Model/ }));
    expect(await screen.findByText('PostgreSQL entities')).toBeInTheDocument();
    expect(screen.getByText('DebtAllocation')).toBeInTheDocument();
  });
});

describe('StatusPage', () => {
  it('shows an UP API with its components', async () => {
    signIn(['ROLE_ADMIN']);
    health.fetchHealth.mockResolvedValue(
      probe({
        body: {
          status: 'UP',
          components: { db: { status: 'UP', details: { database: 'PostgreSQL' } } },
        },
      }),
    );
    health.fetchInfo.mockResolvedValue(probe({ body: { build: { version: '1.0.0' } } }));

    inRouter(<StatusPage />);

    expect(await screen.findByText('db')).toBeInTheDocument();
    expect(screen.getByText('database: PostgreSQL')).toBeInTheDocument();
    expect(screen.getByText('v1.0.0')).toBeInTheDocument();
    expect(screen.getByText('HTTP 200')).toBeInTheDocument();
    expect(screen.getByText(/User user-1 · ROLE_ADMIN/)).toBeInTheDocument();
  });

  it('explains a failed check', async () => {
    health.fetchHealth.mockResolvedValue(
      probe({
        ok: false,
        httpStatus: 0,
        error: 'Failed to fetch',
        hint: 'Nothing answered — check the API is running',
      }),
    );
    health.fetchInfo.mockResolvedValue(probe({ ok: false, httpStatus: 0 }));

    inRouter(<StatusPage />);

    expect(await screen.findByText('API check failed: Failed to fetch')).toBeInTheDocument();
    expect(screen.getByText(/Nothing answered/)).toBeInTheDocument();
    expect(
      screen.getByText('No component detail available while the API is unreachable.'),
    ).toBeInTheDocument();
  });

  it('can pause auto-refresh and refresh on demand', async () => {
    health.fetchHealth.mockResolvedValue(probe({ body: { status: 'UP' } }));
    health.fetchInfo.mockResolvedValue(probe({}));
    inRouter(<StatusPage />);
    await waitFor(() => expect(health.fetchHealth).toHaveBeenCalledTimes(1));

    fireEvent.click(screen.getByRole('button', { name: 'Auto 30s' }));
    expect(screen.getByRole('button', { name: 'Paused' })).toBeInTheDocument();

    fireEvent.click(screen.getByRole('button', { name: 'Refresh' }));
    await waitFor(() => expect(health.fetchHealth.mock.calls.length).toBeGreaterThanOrEqual(2));
  });
});
