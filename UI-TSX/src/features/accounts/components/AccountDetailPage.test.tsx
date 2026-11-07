import { beforeEach, describe, expect, it, vi } from 'vitest';
import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import type { Account } from '../types/Account';
import AccountDetailPage from './AccountDetailPage';

const api = vi.hoisted(() => ({
  getAccount: vi.fn(),
  addPurchase: vi.fn(),
  updatePurchase: vi.fn(),
  deletePurchase: vi.fn(),
}));

vi.mock('@/features/accounts/api/accountApi', () => api);

const visa: Account = {
  id: 1,
  name: 'Chase Visa',
  kind: 'creditCard',
  openingBalance: 1000,
  balanceDate: '2026-10-17',
  apr: 0,
  creditLimit: null,
  payments: [],
  purchases: [
    {
      id: 7,
      accountId: 1,
      name: 'Groceries',
      amount: 50,
      fkPeriod: 3,
      period: 'Weekly',
      beginDate: null,
      endDate: null,
      weeklyDow: 1,
      dateRangeReq: false,
    },
  ],
};

const renderAt = (path: string) =>
  render(
    <MemoryRouter initialEntries={[path]}>
      <Routes>
        <Route path="/command/transactions/accounts/:id" element={<AccountDetailPage />} />
      </Routes>
    </MemoryRouter>,
  );

describe('AccountDetailPage', () => {
  beforeEach(() => vi.clearAllMocks());

  it("lists the account's purchases with their schedule", async () => {
    api.getAccount.mockResolvedValue(visa);
    renderAt('/command/transactions/accounts/1');

    expect(await screen.findByRole('heading', { name: /Chase Visa/ })).toBeInTheDocument();
    expect(api.getAccount).toHaveBeenCalledWith(1);
    expect(screen.getByText('Groceries')).toBeInTheDocument();
    expect(screen.getByText('Every Monday')).toBeInTheDocument();
    expect(screen.getByText('$50.00')).toBeInTheDocument();
  });

  it('checks a new purchase before saving it', async () => {
    api.getAccount.mockResolvedValue({ ...visa, purchases: [] });
    renderAt('/command/transactions/accounts/1');

    fireEvent.click(await screen.findByRole('button', { name: 'Add a purchase' }));
    fireEvent.click(await screen.findByRole('button', { name: 'Add purchase' }));

    expect(await screen.findByText('Enter what the purchase is for.')).toBeInTheDocument();
    expect(api.addPurchase).not.toHaveBeenCalled();
  });

  it('says so when the id is not an account', async () => {
    renderAt('/command/transactions/accounts/abc');
    await waitFor(() =>
      expect(screen.getByText('That account does not exist.')).toBeInTheDocument(),
    );
    expect(api.getAccount).not.toHaveBeenCalled();
  });
});
