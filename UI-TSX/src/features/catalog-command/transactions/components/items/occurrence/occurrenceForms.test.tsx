import { beforeEach, describe, expect, it, vi } from 'vitest';
import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import type { ComponentType } from 'react';

vi.mock('@/app/state/sessionHelpers', () => ({ getSessionUserId: () => 'user-1' }));

import OneTimeForm from './one-time/OneTimeForm';
import DailyForm from './daily/DailyForm';
import WeeklyForm from './weekly/WeeklyForm';
import EveryTwoWeeksForm from './every-two-weeks/EveryTwoWeeksForm';
import BiMonthlyForm from './bi-monthly/BiMonthlyForm';
import MonthlyForm from './monthly/MonthlyForm';
import QuarterlyForm from './quarterly/QuarterlyForm';
import SemiAnnualForm from './semi-annual/SemiAnnualForm';
import AnnualForm from './annual/AnnualForm';
import NthWeekdayForm from './nth-weekday/NthWeekdayForm';
import type { Item } from '../../../types/Item';
import { typeNumber } from '@/test/render';

type FormProps = {
  itemType: number;
  initial: Item | null;
  create: (payload: Item) => Promise<Item>;
  update: (id: number, payload: Item) => Promise<Item>;
  onSaved: () => void;
};

interface FormCase {
  name: string;
  Form: ComponentType<FormProps>;
  fkPeriod: number;
  /** The period's own fields, as saved. */
  fields: Partial<Item>;
  /** What it says when only a name and amount are filled in (null: that's enough). */
  missing: string | null;
  /** Supports the optional "Use Time Frame" range with begin/end checks. */
  range: boolean;
}

const cases: FormCase[] = [
  {
    name: 'OneTimeForm',
    Form: OneTimeForm,
    fkPeriod: 1,
    fields: { beginDate: '2026-11-01' },
    missing: 'Please select a date.',
    range: false,
  },
  { name: 'DailyForm', Form: DailyForm, fkPeriod: 2, fields: {}, missing: null, range: true },
  {
    name: 'WeeklyForm',
    Form: WeeklyForm,
    fkPeriod: 3,
    fields: { weeklyDow: 5 },
    missing: 'Please select a weekday.',
    range: true,
  },
  {
    name: 'EveryTwoWeeksForm',
    Form: EveryTwoWeeksForm,
    fkPeriod: 4,
    fields: { beginDate: '2026-10-02', everyOtherWeekDow: 5 },
    missing: 'Inception date is required.',
    range: false,
  },
  {
    name: 'BiMonthlyForm',
    Form: BiMonthlyForm,
    fkPeriod: 5,
    fields: { biMonthlyDay1: 1, biMonthlyDay2: 15 },
    missing: 'Both bi-monthly days are required.',
    range: true,
  },
  {
    name: 'MonthlyForm',
    Form: MonthlyForm,
    fkPeriod: 6,
    fields: { monthlyDom: 15 },
    missing: 'Please select a day of the month.',
    range: true,
  },
  {
    name: 'QuarterlyForm',
    Form: QuarterlyForm,
    fkPeriod: 7,
    fields: {
      quarterly1Month: 1,
      quarterly1Day: 15,
      quarterly2Month: 4,
      quarterly2Day: 15,
      quarterly3Month: 7,
      quarterly3Day: 15,
      quarterly4Month: 10,
      quarterly4Day: 15,
    },
    missing: '1st Quarter: Month and Day are required.',
    range: true,
  },
  {
    name: 'SemiAnnualForm',
    Form: SemiAnnualForm,
    fkPeriod: 8,
    fields: { semiAnnual1Month: 1, semiAnnual1Day: 1, semiAnnual2Month: 7, semiAnnual2Day: 1 },
    missing: '1st Annum: Month and Day are required.',
    range: true,
  },
  {
    name: 'AnnualForm',
    Form: AnnualForm,
    fkPeriod: 9,
    fields: { annualMoy: 4, annualDom: 15 },
    missing: 'Annual Month and Day are required.',
    range: true,
  },
  {
    name: 'NthWeekdayForm',
    Form: NthWeekdayForm,
    fkPeriod: 10,
    fields: { nthDow: 2, nthIndex: 1 },
    missing: 'Please select a weekday.',
    range: true,
  },
];

function setup(Form: ComponentType<FormProps>, initial: Item | null, itemType = 2) {
  const props = {
    itemType,
    initial,
    create: vi.fn(async (p: Item) => ({ ...p, id: 99 })),
    update: vi.fn(async (_id: number, p: Item) => p),
    onSaved: vi.fn(),
  };
  render(<Form {...props} />);
  return props;
}

const save = () => fireEvent.click(screen.getByRole('button', { name: 'Save' }));

function fillHeader(name: string, amount: number) {
  fireEvent.change(screen.getAllByRole('textbox')[0], { target: { value: name } });
  typeNumber(screen.getByRole('spinbutton'), amount);
}

describe.each(cases)('$name', ({ Form, fkPeriod, fields, missing, range }) => {
  const existing: Item = {
    id: 42,
    userId: 'user-1',
    name: 'Car payment',
    amount: 385.2,
    dateRangeReq: false,
    ...fields,
  } as Item;

  beforeEach(() => vi.clearAllMocks());

  it('requires a name first', async () => {
    const props = setup(Form, null);
    save();
    expect(await screen.findByText('Name is required.')).toBeInTheDocument();
    expect(props.create).not.toHaveBeenCalled();
  });

  it('then an amount', async () => {
    const props = setup(Form, null);
    fireEvent.change(screen.getAllByRole('textbox')[0], { target: { value: 'Rent' } });
    save();
    expect(await screen.findByText('Amount is required.')).toBeInTheDocument();
    expect(props.create).not.toHaveBeenCalled();
  });

  it(
    missing ? `then its schedule: "${missing}"` : 'creates with just a name and amount',
    async () => {
      const props = setup(Form, null, 1);
      fillHeader('  Rent  ', 1600);
      save();

      if (missing) {
        expect(await screen.findByText(missing)).toBeInTheDocument();
        expect(props.create).not.toHaveBeenCalled();
      } else {
        await waitFor(() => expect(props.create).toHaveBeenCalled());
        expect(props.create.mock.calls[0][0]).toMatchObject({
          userId: 'user-1',
          name: 'Rent',
          amount: 1600,
          fkItemType: 1,
          fkPeriod,
        });
        expect(props.onSaved).toHaveBeenCalled();
      }
    },
  );

  it(`saves an edit with period ${fkPeriod} and its schedule fields`, async () => {
    const props = setup(Form, existing);
    expect(screen.getAllByRole('textbox')[0]).toHaveValue('Car payment');

    save();

    await waitFor(() => expect(props.update).toHaveBeenCalled());
    const [id, payload] = props.update.mock.calls[0];
    expect(id).toBe(42);
    expect(payload).toMatchObject({
      id: 42,
      userId: 'user-1',
      name: 'Car payment',
      amount: 385.2,
      fkItemType: 2,
      fkPeriod,
      ...fields,
    });
    expect(props.create).not.toHaveBeenCalled();
    expect(props.onSaved).toHaveBeenCalled();
  });

  it('reports a failed save and stays on the form', async () => {
    const props = setup(Form, existing);
    props.update.mockRejectedValueOnce(new Error('Time Period must be between 1 and 10'));
    save();
    expect(await screen.findByText('Time Period must be between 1 and 10')).toBeInTheDocument();
    expect(props.onSaved).not.toHaveBeenCalled();
  });

  it('Cancel leaves without saving', () => {
    const props = setup(Form, existing);
    fireEvent.click(screen.getByRole('button', { name: 'Cancel' }));
    expect(props.onSaved).toHaveBeenCalled();
    expect(props.update).not.toHaveBeenCalled();
  });

  if (range) {
    it('saves the time frame when one is set', async () => {
      const props = setup(Form, {
        ...existing,
        dateRangeReq: true,
        beginDate: '2026-01-01',
        endDate: '2026-12-31',
      });
      save();
      await waitFor(() => expect(props.update).toHaveBeenCalled());
      expect(props.update.mock.calls[0][1]).toMatchObject({
        dateRangeReq: true,
        beginDate: '2026-01-01',
        endDate: '2026-12-31',
      });
    });

    it('refuses a time frame that ends before it begins', async () => {
      const props = setup(Form, {
        ...existing,
        dateRangeReq: true,
        beginDate: '2026-12-31',
        endDate: '2026-01-01',
      });
      save();
      expect(
        await screen.findByText('Begin date must be before or equal to end date.'),
      ).toBeInTheDocument();
      expect(props.update).not.toHaveBeenCalled();
    });

    it('needs both dates when the time frame is on', async () => {
      const props = setup(Form, {
        ...existing,
        dateRangeReq: true,
        beginDate: '2026-01-01',
        endDate: null,
      });
      save();
      expect(
        await screen.findByText('Please select both begin and end dates for the date range.'),
      ).toBeInTheDocument();
      expect(props.update).not.toHaveBeenCalled();
    });

    it('drops the dates when the time frame is off', async () => {
      const props = setup(Form, {
        ...existing,
        dateRangeReq: false,
        beginDate: '2026-01-01',
        endDate: '2026-12-31',
      });
      save();
      await waitFor(() => expect(props.update).toHaveBeenCalled());
      expect(props.update.mock.calls[0][1]).toMatchObject({
        dateRangeReq: false,
        beginDate: null,
        endDate: null,
      });
    });
  }
});

describe('schedule-specific checks', () => {
  it('WeeklyForm saves the weekday picked', async () => {
    const props = setup(WeeklyForm, null);
    fillHeader('Groceries', 150);
    fireEvent.click(screen.getByLabelText('Mon'));
    save();
    await waitFor(() => expect(props.create).toHaveBeenCalled());
    expect(props.create.mock.calls[0][0]).toMatchObject({ fkPeriod: 3, weeklyDow: 1 });
  });

  it('BiMonthlyForm needs two different days', async () => {
    const props = setup(BiMonthlyForm, {
      id: 1,
      userId: 'user-1',
      name: 'Pay',
      amount: 10,
      biMonthlyDay1: 15,
      biMonthlyDay2: 15,
      dateRangeReq: false,
    } as Item);
    save();
    expect(await screen.findByText('Day 1 and Day 2 must be different.')).toBeInTheDocument();
    expect(props.update).not.toHaveBeenCalled();
  });

  it('NthWeekdayForm needs the week of the month', async () => {
    const props = setup(NthWeekdayForm, {
      id: 1,
      userId: 'user-1',
      name: 'Club',
      amount: 10,
      nthDow: 2,
      nthIndex: null,
      dateRangeReq: false,
    } as Item);
    save();
    expect(await screen.findByText('Please select the week index.')).toBeInTheDocument();
    expect(props.update).not.toHaveBeenCalled();
  });

  it('EveryTwoWeeksForm needs an end date when the time frame is on', async () => {
    const props = setup(EveryTwoWeeksForm, {
      id: 1,
      userId: 'user-1',
      name: 'Paycheck',
      amount: 2000,
      beginDate: '2026-10-02',
      everyOtherWeekDow: 5,
      dateRangeReq: true,
      endDate: null,
    } as Item);
    save();
    expect(await screen.findByText('Please select an end date.')).toBeInTheDocument();
    expect(props.update).not.toHaveBeenCalled();
  });

  it('OneTimeForm saves its date as a one-time range', async () => {
    const props = setup(OneTimeForm, {
      id: 1,
      userId: 'user-1',
      name: 'Bonus',
      amount: 500,
      beginDate: '2026-12-15',
      dateRangeReq: true,
    } as Item);
    save();
    await waitFor(() => expect(props.update).toHaveBeenCalled());
    expect(props.update.mock.calls[0][1]).toMatchObject({
      fkPeriod: 1,
      beginDate: '2026-12-15',
      endDate: null,
      dateRangeReq: true,
    });
  });
});
