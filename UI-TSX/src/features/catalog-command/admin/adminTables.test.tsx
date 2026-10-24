import { beforeEach, describe, expect, it, vi } from 'vitest';
import { fireEvent, screen, waitFor, within } from '@testing-library/react';

const itemTypeApi = vi.hoisted(() => ({
  fetchAll: vi.fn(),
  fetchById: vi.fn(),
  create: vi.fn(),
  update: vi.fn(),
  remove: vi.fn(),
}));
const timePeriodApi = vi.hoisted(() => ({
  fetchAll: vi.fn(),
  fetchById: vi.fn(),
  create: vi.fn(),
  update: vi.fn(),
  remove: vi.fn(),
}));
vi.mock('./itemType/api/itemTypeApi', () => ({ itemTypeApi }));
vi.mock('./timePeriod/api/timePeriodApi', () => ({ timePeriodApi }));

import { ItemTypeTablePage } from './itemType/components/ItemTypeTablePage';
import { TimePeriodTablePage } from './timePeriod/components/TimePeriodTablePage';
import { renderWithProviders, typeNumber } from '@/test/render';

const suites = [
  {
    title: 'ItemTypeTablePage',
    Page: ItemTypeTablePage,
    api: itemTypeApi,
    rows: [
      { id: 1, name: 'Credit' },
      { id: 2, name: 'Debit' },
    ],
    noun: 'Item Type',
  },
  {
    title: 'TimePeriodTablePage',
    Page: TimePeriodTablePage,
    api: timePeriodApi,
    rows: [
      { id: 1, name: 'One Time Occurrence' },
      { id: 6, name: 'Monthly' },
    ],
    noun: 'Time Period',
  },
] as const;

describe.each(suites)('$title', ({ Page, api, rows, noun }) => {
  beforeEach(() => {
    vi.clearAllMocks();
    api.fetchAll.mockResolvedValue([...rows]);
  });

  const dialog = () => screen.getByRole('dialog');

  it('lists what the API returns', async () => {
    renderWithProviders(<Page />);
    expect(await screen.findByText(rows[1].name)).toBeInTheDocument();
    expect(screen.getByText(rows[0].name)).toBeInTheDocument();
  });

  it('creates a new one and reloads the list', async () => {
    api.create.mockResolvedValue({ id: 11, name: 'Hourly' });
    renderWithProviders(<Page />);
    await screen.findByText(rows[0].name);

    fireEvent.click(screen.getByRole('button', { name: 'New' }));
    expect(await screen.findByText(`Create ${noun}`)).toBeInTheDocument();
    typeNumber(within(dialog()).getByRole('spinbutton'), 11);
    fireEvent.change(within(dialog()).getByRole('textbox'), { target: { value: 'Hourly' } });
    fireEvent.click(within(dialog()).getByRole('button', { name: 'Create' }));

    await waitFor(() => expect(api.create).toHaveBeenCalledWith({ id: 11, name: 'Hourly' }));
    expect(await screen.findByText(`${noun} created`)).toBeInTheDocument();
    await waitFor(() => expect(api.fetchAll).toHaveBeenCalledTimes(2));
  });

  it("won't create without an id and a name", async () => {
    renderWithProviders(<Page />);
    await screen.findByText(rows[0].name);

    fireEvent.click(screen.getByRole('button', { name: 'New' }));
    await screen.findByText(`Create ${noun}`);
    fireEvent.click(within(dialog()).getByRole('button', { name: 'Create' }));

    expect(api.create).not.toHaveBeenCalled();
    expect(dialog()).toBeInTheDocument();
  });

  it('renames a row', async () => {
    api.update.mockResolvedValue({ ...rows[1], name: 'Renamed' });
    renderWithProviders(<Page />);
    await screen.findByText(rows[1].name);

    const row = screen.getByText(rows[1].name).closest('tr')!;
    fireEvent.click(within(row).getAllByRole('button')[0]);
    expect(await screen.findByText(`Edit ${noun}`)).toBeInTheDocument();
    const name = within(dialog()).getByRole('textbox');
    expect(name).toHaveValue(rows[1].name);
    fireEvent.change(name, { target: { value: 'Renamed' } });
    fireEvent.click(within(dialog()).getByRole('button', { name: 'Update' }));

    await waitFor(() =>
      expect(api.update).toHaveBeenCalledWith({ id: rows[1].id, name: 'Renamed' }),
    );
    expect(await screen.findByText(`${noun} updated`)).toBeInTheDocument();
  });

  it('deletes a row after confirming', async () => {
    api.remove.mockResolvedValue(undefined);
    renderWithProviders(<Page />);
    await screen.findByText(rows[1].name);

    const row = screen.getByText(rows[1].name).closest('tr')!;
    fireEvent.click(within(row).getAllByRole('button')[1]);
    expect(
      await screen.findByText(`Are you sure you want to delete this ${noun}?`),
    ).toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: 'Yes' }));

    await waitFor(() => expect(api.remove).toHaveBeenCalledWith(rows[1].id));
    expect(await screen.findByText(`${noun} deleted`)).toBeInTheDocument();
  });

  it('says so when a save fails', async () => {
    api.remove.mockRejectedValue(new Error('in use'));
    renderWithProviders(<Page />);
    await screen.findByText(rows[1].name);

    fireEvent.click(
      within(screen.getByText(rows[1].name).closest('tr')!).getAllByRole('button')[1],
    );
    fireEvent.click(await screen.findByRole('button', { name: 'Yes' }));

    expect(await screen.findByText(`Failed to delete ${noun}`)).toBeInTheDocument();
  });
});
