import { describe, expect, it, vi } from 'vitest';
import { fireEvent, render, screen } from '@testing-library/react';
import { FormLayout } from './FormLayout';
import { HeaderFields } from './HeaderFields';
import { TimeFrameSelector } from './TimeFrameSelector';
import { WeekdayRadioGroup } from './WeekdayRadioGroup';
import { RadioOption } from './RadioOption';
import MonthDayRow from './MonthDayRow';
import { MONTHS } from '../../constants/months';
import { DAY_NUMBERS } from '../../constants/days';
import { WEEKDAYS } from '../../constants/weekdays';
import { typeNumber } from '@/test/render';

describe('FormLayout', () => {
  it('submits with Save and leaves with Cancel', () => {
    const onSave = vi.fn();
    const onCancel = vi.fn();
    render(
      <FormLayout onSave={onSave} onCancel={onCancel}>
        <span>fields</span>
      </FormLayout>,
    );
    fireEvent.click(screen.getByRole('button', { name: 'Save' }));
    fireEvent.click(screen.getByRole('button', { name: 'Cancel' }));
    expect(onSave).toHaveBeenCalledTimes(1);
    expect(onCancel).toHaveBeenCalledTimes(1);
  });
});

describe('HeaderFields', () => {
  it('reports name and amount changes', () => {
    const onName = vi.fn();
    const onAmount = vi.fn();
    render(
      <HeaderFields name="Rent" amount={1600} onNameChange={onName} onAmountChange={onAmount} />,
    );

    expect(screen.getByRole('textbox')).toHaveValue('Rent');
    fireEvent.change(screen.getByRole('textbox'), { target: { value: 'Mortgage' } });
    typeNumber(screen.getByRole('spinbutton'), 1700);

    expect(onName).toHaveBeenCalledWith('Mortgage');
    expect(onAmount).toHaveBeenCalledWith(1700);
  });
});

describe('TimeFrameSelector', () => {
  it('hides the dates until the time frame is turned on', () => {
    const onChange = vi.fn();
    const { rerender } = render(
      <TimeFrameSelector
        dateRangeReq={false}
        beginDate={null}
        endDate={null}
        onChange={onChange}
      />,
    );
    expect(screen.queryByText('Start')).not.toBeInTheDocument();

    fireEvent.click(screen.getByRole('checkbox'));
    expect(onChange).toHaveBeenCalledWith({ dateRangeReq: true, beginDate: null, endDate: null });

    rerender(
      <TimeFrameSelector dateRangeReq beginDate={null} endDate={null} onChange={onChange} />,
    );
    expect(screen.getByText('Start')).toBeInTheDocument();
    expect(screen.getByText('End')).toBeInTheDocument();
  });

  it('clears the dates when turned off', () => {
    const onChange = vi.fn();
    render(
      <TimeFrameSelector
        dateRangeReq
        beginDate={new Date(2026, 0, 1)}
        endDate={new Date(2026, 11, 31)}
        onChange={onChange}
      />,
    );
    fireEvent.click(screen.getByRole('checkbox'));
    expect(onChange).toHaveBeenCalledWith({ dateRangeReq: false, beginDate: null, endDate: null });
  });
});

describe('WeekdayRadioGroup and RadioOption', () => {
  it('offers every weekday and reports the pick', () => {
    const onChange = vi.fn();
    render(<WeekdayRadioGroup value={2} onChange={onChange} />);

    expect(screen.getAllByRole('radio')).toHaveLength(7);
    expect(screen.getByLabelText('Tue')).toBeChecked();
    fireEvent.click(screen.getByLabelText('Fri'));
    expect(onChange).toHaveBeenCalledWith(5);
  });

  it('a single option is checked only when selected', () => {
    render(<RadioOption label="Sun" value={0} selected={null} onChange={vi.fn()} />);
    expect(screen.getByLabelText('Sun')).not.toBeChecked();
  });
});

describe('MonthDayRow', () => {
  it('labels both dropdowns', () => {
    render(
      <MonthDayRow label="1st Quarter" month={1} day={15} setMonth={vi.fn()} setDay={vi.fn()} />,
    );
    expect(screen.getByText('1st Quarter Month')).toBeInTheDocument();
    expect(screen.getByText('1st Quarter Day')).toBeInTheDocument();
  });
});

describe('constants', () => {
  it('cover the calendar', () => {
    expect(MONTHS.map(m => m.value)).toEqual([1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12]);
    expect(WEEKDAYS.map(d => d.value)).toEqual([0, 1, 2, 3, 4, 5, 6]);
    // Day pickers stop at the 27th so a date exists in every month
    expect(DAY_NUMBERS[0].value).toBe(1);
    expect(DAY_NUMBERS[DAY_NUMBERS.length - 1].value).toBe(27);
  });
});
