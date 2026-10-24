import { beforeEach, describe, expect, it, vi } from 'vitest';
import { fireEvent, render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import type { Item } from '@/features/catalog-command/transactions/types/Item';
import type { Debt } from '../types/Debt';
import DebtPaydownPage from './DebtPaydownPage';

// The page talks to the API only through these modules
const api = vi.hoisted(() => ({
  listDebts: vi.fn(),
  createDebt: vi.fn(),
  updateDebt: vi.fn(),
  deleteDebt: vi.fn(),
  getItemsForUserAndType: vi.fn(),
}));

vi.mock('@/features/debt-paydown/api/debtApi', () => ({
  listDebts: api.listDebts,
  createDebt: api.createDebt,
  updateDebt: api.updateDebt,
  deleteDebt: api.deleteDebt,
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

const card = item(10, 'Chase Visa', 2, 200);
const paycheck = item(20, 'Paycheck', 1, 2000);

const visaDebt: Debt = {
  id: 1,
  itemId: 10,
  name: 'Chase Visa',
  payment: 200,
  period: 'Monthly',
  balance: 1000,
  apr: 0,
  balanceDate: '2026-10-17',
  allocations: [
    {
      id: 5,
      creditItemId: 20,
      creditName: 'Paycheck',
      creditAmount: 2000,
      creditPeriod: 'Monthly',
      percent: 25,
      perOccurrence: 500,
    },
  ],
};

function setUp(debts: Debt[], debits: Item[], credits: Item[]) {
  api.listDebts.mockResolvedValue(debts);
  api.getItemsForUserAndType.mockImplementation(async (_user: string, type: number) =>
    type === 2 ? debits : credits,
  );
  render(
    <MemoryRouter>
      <DebtPaydownPage />
    </MemoryRouter>,
  );
}

describe('DebtPaydownPage', () => {
  beforeEach(() => vi.clearAllMocks());

  it('shows each tracked debt with what is owed and its allocations', async () => {
    setUp([visaDebt], [card], [paycheck]);

    expect(await screen.findByRole('heading', { name: 'Chase Visa' })).toBeInTheDocument();
    expect(screen.getByText('$1,000.00')).toBeInTheDocument();
    expect(screen.getByText(/as of Oct 17, 2026/)).toBeInTheDocument();
    expect(screen.getByText('25%')).toBeInTheDocument();
    expect(screen.getByText('$500.00 each')).toBeInTheDocument();

    // Every debit is already tracked, so there is nothing new to track
    expect(screen.getByRole('button', { name: 'Track a debt' })).toBeDisabled();
  });

  it('asks for a debit first when there are none', async () => {
    setUp([], [], [paycheck]);

    expect(
      await screen.findByText(/first \(for example your credit card payment\)/),
    ).toBeInTheDocument();
    expect(screen.getByRole('link', { name: 'debit' })).toHaveAttribute(
      'href',
      '/command/transactions/debits',
    );
  });

  it("checks the form before saving and doesn't call the API with a problem", async () => {
    setUp([], [card], [paycheck]);

    const [trackButton] = await screen.findAllByRole('button', { name: 'Track a debt' });
    fireEvent.click(trackButton);
    fireEvent.click(await screen.findByRole('button', { name: 'Track debt' }));

    expect(await screen.findByText('Choose the debit you are paying down.')).toBeInTheDocument();
    expect(api.createDebt).not.toHaveBeenCalled();
  });

  it('shows the load error when the API fails', async () => {
    api.listDebts.mockRejectedValue(new Error('Database failure while fetching debts'));
    api.getItemsForUserAndType.mockResolvedValue([]);
    render(
      <MemoryRouter>
        <DebtPaydownPage />
      </MemoryRouter>,
    );

    expect(await screen.findByText('Database failure while fetching debts')).toBeInTheDocument();
  });
});
