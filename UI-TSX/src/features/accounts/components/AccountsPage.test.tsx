import { beforeEach, describe, expect, it, vi } from 'vitest';
import { fireEvent, render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import type { Item } from '@/features/catalog-command/transactions/types/Item';
import type { Account } from '../types/Account';
import AccountsPage from './AccountsPage';

// The page talks to the API only through these modules
const api = vi.hoisted(() => ({
  listAccounts: vi.fn(),
  createAccount: vi.fn(),
  updateAccount: vi.fn(),
  deleteAccount: vi.fn(),
  getItemsForUserAndType: vi.fn(),
}));

vi.mock('@/features/accounts/api/accountApi', () => ({
  listAccounts: api.listAccounts,
  createAccount: api.createAccount,
  updateAccount: api.updateAccount,
  deleteAccount: api.deleteAccount,
}));
vi.mock('@/features/catalog-command/transactions/api/itemApi', () => ({
  getItemsForUserAndType: api.getItemsForUserAndType,
}));
vi.mock('@/app/state/sessionHelpers', () => ({ getSessionUserId: () => 'user-1' }));

const item = (id: number, name: string, type: number, amount: number): Item => ({
  id,
  userId: 'user-1',
  name,
  amount,
  ItemType: { Id: type },
  TimePeriod: { Id: 6, Name: 'Monthly' },
  dateRangeReq: false,
});

const paycheck = item(20, 'Paycheck', 1, 2000);
const visaPayment = item(10, 'Visa payment', 2, 200);

const visa: Account = {
  id: 1,
  name: 'Chase Visa',
  kind: 'creditCard',
  openingBalance: 1000,
  balanceDate: '2026-10-17',
  apr: 24.99,
  creditLimit: 5000,
  purchases: [],
  payments: [
    {
      id: 5,
      itemId: 20,
      itemTypeId: 1,
      itemName: 'Paycheck',
      itemAmount: 2000,
      itemPeriod: 'Monthly',
      amount: null,
      percent: 25,
      perOccurrence: 500,
    },
    {
      id: 6,
      itemId: 10,
      itemTypeId: 2,
      itemName: 'Visa payment',
      itemAmount: 200,
      itemPeriod: 'Monthly',
      amount: null,
      percent: 100,
      perOccurrence: 200,
    },
  ],
};

const renderPage = () =>
  render(
    <MemoryRouter>
      <AccountsPage />
    </MemoryRouter>,
  );

describe('AccountsPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    api.getItemsForUserAndType.mockImplementation((_user: string, type: number) =>
      Promise.resolve(type === 1 ? [paycheck] : [visaPayment]),
    );
  });

  it('shows each account with what it owes and what pays it', async () => {
    api.listAccounts.mockResolvedValue([visa]);
    renderPage();

    expect(await screen.findByRole('heading', { name: /Chase Visa/ })).toBeInTheDocument();
    expect(screen.getByText('$1,000.00')).toBeInTheDocument();
    expect(screen.getByText(/24\.99% APR/)).toBeInTheDocument();
    expect(screen.getByText(/\$5,000\.00 limit/)).toBeInTheDocument();
    expect(screen.getByText('25%')).toBeInTheDocument();
    expect(screen.getByText('All')).toBeInTheDocument(); // the debit pays its whole amount
    expect(screen.getByText('$500.00 each')).toBeInTheDocument();
  });

  it('offers to add the first card or loan', async () => {
    api.listAccounts.mockResolvedValue([]);
    renderPage();

    expect(
      await screen.findByText(/Add a credit card or loan with what you owe/),
    ).toBeInTheDocument();
  });

  it('checks the form before saving', async () => {
    api.listAccounts.mockResolvedValue([]);
    renderPage();

    fireEvent.click((await screen.findAllByRole('button', { name: 'Add a card or loan' }))[0]);
    fireEvent.click(await screen.findByRole('button', { name: 'Add account' }));

    expect(await screen.findByText('Enter a name for the card or loan.')).toBeInTheDocument();
    expect(api.createAccount).not.toHaveBeenCalled();
  });
});
