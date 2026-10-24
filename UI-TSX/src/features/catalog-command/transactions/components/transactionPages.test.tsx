import { beforeEach, describe, expect, it, vi } from 'vitest';
import { fireEvent, screen, waitFor, within } from '@testing-library/react';

const itemApi = vi.hoisted(() => ({
  getItemsForUserAndType: vi.fn(),
  createItem: vi.fn(),
  updateItem: vi.fn(),
  deleteItem: vi.fn(),
}));
vi.mock('@/features/catalog-command/transactions/api/itemApi', () => itemApi);
const initialApi = vi.hoisted(() => ({
  fetchInitialAmount: vi.fn(),
  createInitialAmount: vi.fn(),
  updateInitialAmount: vi.fn(),
}));
vi.mock('@/features/catalog-command/transactions/api/initialAmountApi', () => initialApi);
const timePeriodApi = vi.hoisted(() => ({ fetchAll: vi.fn() }));
vi.mock('@/features/catalog-command/admin/timePeriod/api/timePeriodApi', () => ({ timePeriodApi }));
const session = vi.hoisted(() => ({ userId: 'user-1' as string | null }));
vi.mock('@/app/state/sessionHelpers', () => ({ getSessionUserId: () => session.userId }));

import ItemsListPage from './items/list/ItemsListPage';
import SelectPeriodPage from './items/occurrence/SelectPeriodPage';
import InitialAmountPage from './initial-amount/InitialAmountPage';
import { renderWithProviders, typeNumber } from '@/test/render';
import { Route, Routes, useLocation } from 'react-router-dom';

/** Shows where the page navigated to. */
function Where() {
  return <div data-testid="where">{useLocation().pathname}</div>;
}
const withWhere = (ui: React.ReactElement) => (
  <Routes>
    <Route path="/list" element={ui} />
    <Route path="*" element={<Where />} />
  </Routes>
);

const items = [
  {
    id: 1,
    userId: 'user-1',
    name: 'Rent',
    amount: 1600,
    TimePeriod: { Id: 6, Name: 'Monthly' },
    dateRangeReq: false,
  },
  {
    id: 2,
    userId: 'user-1',
    name: 'Gym',
    amount: 45.5,
    TimePeriod: { Id: 3, Name: 'Weekly' },
    dateRangeReq: false,
  },
];

beforeEach(() => {
  vi.clearAllMocks();
  session.userId = 'user-1';
});

describe('ItemsListPage', () => {
  it("lists the user's debits with amount and period", async () => {
    itemApi.getItemsForUserAndType.mockResolvedValue(items);
    renderWithProviders(<ItemsListPage itemType={2} />);

    expect(await screen.findByText('Rent')).toBeInTheDocument();
    expect(screen.getByRole('heading', { name: 'Debits' })).toBeInTheDocument();
    expect(screen.getByText('$1,600.00')).toBeInTheDocument();
    expect(screen.getByText('$45.50')).toBeInTheDocument();
    expect(screen.getByText('Weekly')).toBeInTheDocument();
    expect(itemApi.getItemsForUserAndType).toHaveBeenCalledWith('user-1', 2);
  });

  it('New goes to the period picker, Edit to the right period form', async () => {
    itemApi.getItemsForUserAndType.mockResolvedValue(items);
    renderWithProviders(withWhere(<ItemsListPage itemType={1} />), { route: '/list' });

    fireEvent.click(await screen.findByRole('button', { name: 'New Credit' }));
    expect(screen.getByTestId('where')).toHaveTextContent('/command/transactions/credits/new');
  });

  it('Edit opens the form for the item period', async () => {
    itemApi.getItemsForUserAndType.mockResolvedValue(items);
    renderWithProviders(withWhere(<ItemsListPage itemType={2} />), { route: '/list' });

    const row = (await screen.findByText('Gym')).closest('tr')!;
    fireEvent.click(within(row).getByRole('button', { name: 'Edit' }));
    expect(screen.getByTestId('where')).toHaveTextContent('/command/transactions/debits/3/2/edit');
  });

  it('deletes an item after confirming', async () => {
    itemApi.getItemsForUserAndType.mockResolvedValue(items);
    itemApi.deleteItem.mockResolvedValue(undefined);
    renderWithProviders(<ItemsListPage itemType={2} />);

    const row = (await screen.findByText('Rent')).closest('tr')!;
    fireEvent.click(within(row).getByRole('button', { name: 'Delete' }));
    expect(
      await screen.findByText('Delete "Rent"? This action cannot be undone.'),
    ).toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: 'Yes' }));

    await waitFor(() => expect(itemApi.deleteItem).toHaveBeenCalledWith(1));
    await waitFor(() => expect(screen.queryByText('Rent')).not.toBeInTheDocument());
    expect(screen.getByText('Gym')).toBeInTheDocument();
  });

  it('shows a load error', async () => {
    itemApi.getItemsForUserAndType.mockRejectedValue(new Error('Database failure'));
    renderWithProviders(<ItemsListPage itemType={1} />);
    expect(await screen.findByText('Database failure')).toBeInTheDocument();
  });

  it('warns when nobody is signed in', async () => {
    session.userId = null;
    renderWithProviders(<ItemsListPage itemType={1} />);
    expect(await screen.findByText('No user session found.')).toBeInTheDocument();
    expect(itemApi.getItemsForUserAndType).not.toHaveBeenCalled();
  });
});

describe('SelectPeriodPage', () => {
  it('lists the periods from the API in id order and opens the chosen form', async () => {
    timePeriodApi.fetchAll.mockResolvedValue([
      { id: 6, name: 'Monthly' },
      { id: 1, name: 'One Time Occurrence' },
    ]);
    renderWithProviders(withWhere(<SelectPeriodPage itemType={2} />), { route: '/list' });

    expect(screen.getByRole('heading', { name: 'New Debit' })).toBeInTheDocument();
    const select = await screen.findByRole('button', { name: 'Select Monthly' });
    const names = screen.getAllByText(/^(Monthly|One Time Occurrence)$/).map(n => n.textContent);
    expect(names).toEqual(['One Time Occurrence', 'Monthly']);

    fireEvent.click(select);
    expect(screen.getByTestId('where')).toHaveTextContent('/command/transactions/debits/new/6');
  });

  it('falls back to the built-in periods when the API fails', async () => {
    timePeriodApi.fetchAll.mockRejectedValue(new Error('down'));
    renderWithProviders(<SelectPeriodPage itemType={1} />);

    expect(
      await screen.findByText(
        'Could not load time periods from the server. Showing default options.',
      ),
    ).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Select Nth-Weekday' })).toBeInTheDocument();
    expect(screen.getAllByRole('button', { name: /^Select / })).toHaveLength(10);
  });
});

describe('InitialAmountPage', () => {
  it('offers to create one when there is none', async () => {
    initialApi.fetchInitialAmount.mockResolvedValue(null);
    initialApi.createInitialAmount.mockResolvedValue({ id: 5, amount: 250 });
    renderWithProviders(<InitialAmountPage />);

    expect(
      await screen.findByText('Create Initial Amount', { selector: 'h4' }),
    ).toBeInTheDocument();
    typeNumber(screen.getByRole('spinbutton'), 250);
    fireEvent.click(screen.getByRole('button', { name: 'Create Initial Amount' }));

    await waitFor(() => expect(initialApi.createInitialAmount).toHaveBeenCalledWith(250));
    expect(await screen.findByRole('button', { name: 'Edit' })).toBeInTheDocument();
  });

  it('edits the existing amount', async () => {
    initialApi.fetchInitialAmount.mockResolvedValue({ id: 5, amount: 100 });
    initialApi.updateInitialAmount.mockResolvedValue({ id: 5, amount: 300 });
    renderWithProviders(<InitialAmountPage />);

    const input = await screen.findByRole('spinbutton');
    expect(input).toBeDisabled();

    fireEvent.click(screen.getByRole('button', { name: 'Edit' }));
    expect(input).not.toBeDisabled();
    typeNumber(input, 300);
    fireEvent.click(screen.getByRole('button', { name: 'Save' }));

    await waitFor(() => expect(initialApi.updateInitialAmount).toHaveBeenCalledWith(5, 300));
    expect(await screen.findByText('Initial Amount updated')).toBeInTheDocument();
  });

  it('Cancel puts the saved amount back', async () => {
    initialApi.fetchInitialAmount.mockResolvedValue({ id: 5, amount: 100 });
    renderWithProviders(<InitialAmountPage />);

    fireEvent.click(await screen.findByRole('button', { name: 'Edit' }));
    typeNumber(screen.getByRole('spinbutton'), 999);
    fireEvent.click(screen.getByRole('button', { name: 'Cancel' }));

    expect(screen.getByRole('button', { name: 'Edit' })).toBeInTheDocument();
    expect(initialApi.updateInitialAmount).not.toHaveBeenCalled();
    expect(screen.getByRole('spinbutton')).toHaveDisplayValue(/100/);
  });

  it('shows a load error', async () => {
    initialApi.fetchInitialAmount.mockRejectedValue(new Error('UserId missing from session'));
    renderWithProviders(<InitialAmountPage />);
    expect(await screen.findByText('UserId missing from session')).toBeInTheDocument();
  });
});
