import { beforeEach, describe, expect, it, vi } from 'vitest';
import { fireEvent, screen } from '@testing-library/react';
import type { ComponentType } from 'react';

const itemApi = vi.hoisted(() => ({
  getItemsForUserAndType: vi.fn(),
  createItem: vi.fn(),
  updateItem: vi.fn(),
  deleteItem: vi.fn(),
}));
vi.mock('@/features/catalog-command/transactions/api/itemApi', () => itemApi);
vi.mock('@/app/state/sessionHelpers', () => ({ getSessionUserId: () => 'user-1' }));

import AddOneTimePage from './one-time/AddOneTimePage';
import EditOneTimePage from './one-time/EditOneTimePage';
import AddDailyPage from './daily/AddDailyPage';
import EditDailyPage from './daily/EditDailyPage';
import AddWeeklyPage from './weekly/AddWeeklyPage';
import EditWeeklyPage from './weekly/EditWeeklyPage';
import AddEveryTwoWeeksPage from './every-two-weeks/AddEveryTwoWeeksPage';
import EditEveryTwoWeeksPage from './every-two-weeks/EditEveryTwoWeeksPage';
import AddBiMonthlyPage from './bi-monthly/AddBiMonthlyPage';
import EditBiMonthlyPage from './bi-monthly/EditBiMonthlyPage';
import AddMonthlyPage from './monthly/AddMonthlyPage';
import EditMonthlyPage from './monthly/EditMonthlyPage';
import AddQuarterlyPage from './quarterly/AddQuarterlyPage';
import EditQuarterlyPage from './quarterly/EditQuarterlyPage';
import AddSemiAnnualPage from './semi-annual/AddSemiAnnualPage';
import EditSemiAnnualPage from './semi-annual/EditSemiAnnualPage';
import AddAnnualPage from './annual/AddAnnualPage';
import EditAnnualPage from './annual/EditAnnualPage';
import AddNthWeekdayPage from './nth-weekday/AddNthWeekdayPage';
import EditNthWeekdayPage from './nth-weekday/EditNthWeekdayPage';
import { renderWithProviders } from '@/test/render';

type PageProps = { itemType: number };

const pages: {
  title: string;
  period: number;
  Add: ComponentType<PageProps>;
  Edit: ComponentType<PageProps>;
}[] = [
  { title: 'One Time', period: 1, Add: AddOneTimePage, Edit: EditOneTimePage },
  { title: 'Daily', period: 2, Add: AddDailyPage, Edit: EditDailyPage },
  { title: 'Weekly', period: 3, Add: AddWeeklyPage, Edit: EditWeeklyPage },
  { title: 'Every Two Weeks', period: 4, Add: AddEveryTwoWeeksPage, Edit: EditEveryTwoWeeksPage },
  { title: 'Bi-Monthly', period: 5, Add: AddBiMonthlyPage, Edit: EditBiMonthlyPage },
  { title: 'Monthly', period: 6, Add: AddMonthlyPage, Edit: EditMonthlyPage },
  { title: 'Quarterly', period: 7, Add: AddQuarterlyPage, Edit: EditQuarterlyPage },
  { title: 'Semi-Annual', period: 8, Add: AddSemiAnnualPage, Edit: EditSemiAnnualPage },
  { title: 'Annual', period: 9, Add: AddAnnualPage, Edit: EditAnnualPage },
  { title: 'Nth-Weekday', period: 10, Add: AddNthWeekdayPage, Edit: EditNthWeekdayPage },
];

const item = (period: number) => ({
  id: 42,
  userId: 'user-1',
  name: 'Car payment',
  amount: 385.2,
  dateRangeReq: false,
  TimePeriod: { Id: period, Name: 'x' },
});

beforeEach(() => vi.clearAllMocks());

describe.each(pages)('$title pages', ({ title, period, Add, Edit }) => {
  it('Add shows the credit or debit title', () => {
    renderWithProviders(<Add itemType={1} />);
    expect(screen.getByRole('heading', { name: `Add ${title} Credit` })).toBeInTheDocument();
  });

  it('Add goes back to the list on Cancel', () => {
    renderWithProviders(<Add itemType={2} />, {
      route: '/command/transactions/debits/new/x',
      path: '/command/transactions/debits/new/x',
    });
    expect(screen.getByRole('heading', { name: `Add ${title} Debit` })).toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: 'Cancel' }));
    expect(screen.getByTestId('navigated-away')).toBeInTheDocument();
  });

  it('Edit loads the item into the form', async () => {
    itemApi.getItemsForUserAndType.mockResolvedValue([item(period)]);
    renderWithProviders(<Edit itemType={2} />, {
      route: `/command/transactions/debits/${period}/42/edit`,
      path: '/command/transactions/debits/:period/:id/edit',
    });

    expect(await screen.findByRole('heading', { name: `Edit ${title} Debit` })).toBeInTheDocument();
    expect(screen.getAllByRole('textbox')[0]).toHaveValue('Car payment');
    expect(itemApi.getItemsForUserAndType).toHaveBeenCalledWith('user-1', 2);
  });

  it('Edit says when the item does not exist', async () => {
    itemApi.getItemsForUserAndType.mockImplementation(async () => [item(period)]); // a new list each call, like the API
    renderWithProviders(<Edit itemType={2} />, {
      route: `/x/${period}/999/edit`,
      path: '/x/:period/:id/edit',
    });

    expect(await screen.findByText('Item not found')).toBeInTheDocument();
    // …and stops asking: it used to refetch the list forever
    await new Promise(r => setTimeout(r, 300));
    expect(itemApi.getItemsForUserAndType).toHaveBeenCalledTimes(1);
  });
});
