import { beforeEach, describe, expect, it, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { HelmetProvider } from 'react-helmet-async';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';

// Pages load their data on mount; keep the network out of a routing test
vi.mock('@/app/auth/api/authApi', () => ({
  authApi: { login: vi.fn(), refresh: vi.fn().mockResolvedValue(null), register: vi.fn() },
}));
vi.mock('@/features/catalog-command/transactions/api/itemApi', () => ({
  getItemsForUserAndType: vi.fn().mockResolvedValue([]),
  getItemById: vi.fn().mockResolvedValue(null),
  createItem: vi.fn(),
  updateItem: vi.fn(),
  deleteItem: vi.fn(),
  fetchItemTypes: vi.fn().mockResolvedValue([]),
  fetchTimePeriods: vi.fn().mockResolvedValue([]),
}));
vi.mock('@/features/debt-paydown/api/debtApi', () => ({
  listDebts: vi.fn().mockResolvedValue([]),
  createDebt: vi.fn(),
  updateDebt: vi.fn(),
  deleteDebt: vi.fn(),
}));
vi.mock('@/features/catalog-command/admin/timePeriod/api/timePeriodApi', () => ({
  timePeriodApi: { fetchAll: vi.fn().mockResolvedValue([{ id: 6, name: 'Monthly' }]) },
}));
vi.mock('@/features/catalog-command/transactions/api/initialAmountApi', () => ({
  fetchInitialAmount: vi.fn().mockResolvedValue(null),
  createInitialAmount: vi.fn(),
  updateInitialAmount: vi.fn(),
}));

import { AppRouter } from './AppRouter';
import { signIn, signOut } from '@/test/session';

function renderAt(path: string) {
  render(
    <QueryClientProvider
      client={new QueryClient({ defaultOptions: { queries: { retry: false } } })}
    >
      <HelmetProvider>
        <MemoryRouter initialEntries={[path]}>
          <AppRouter />
        </MemoryRouter>
      </HelmetProvider>
    </QueryClientProvider>,
  );
}

beforeEach(() => signOut());

describe('AppRouter', () => {
  it('serves the public pages to everyone', async () => {
    renderAt('/docs');
    expect(await screen.findByText('What this system does')).toBeInTheDocument();
  });

  it('sends signed-out visitors from a protected page to home', async () => {
    renderAt('/command/transactions/debt-paydown');
    expect(await screen.findByRole('button', { name: 'Try it as a guest' })).toBeInTheDocument();
  });

  it.each([
    ['/query/display', 'Display'],
    ['/command/transactions/credits', 'Credits'],
    ['/command/transactions/debits', 'Debits'],
    ['/command/transactions/debt-paydown', 'Debt Paydown'],
    ['/command/transactions/credits/new', 'New Credit'],
  ])('opens %s for signed-in users', async (path, heading) => {
    signIn();
    renderAt(path);
    expect(await screen.findByRole('heading', { name: new RegExp(heading) })).toBeInTheDocument();
  });

  it('keeps admin pages for admins', async () => {
    signIn(['ROLE_USER']);
    renderAt('/command/admin/item-types');
    expect(await screen.findByRole('button', { name: /Open forecast/ })).toBeInTheDocument(); // landed on home
    expect(screen.queryByText('Item Types')).not.toBeInTheDocument();
  });

  it('shows Not Found for unknown paths', () => {
    renderAt('/nope');
    expect(screen.getByText('Not Found')).toBeInTheDocument();
  });
});
