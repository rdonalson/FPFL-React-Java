import React from 'react';
import { Dropdown } from 'primereact/dropdown';

import { MONTHS } from '@/features/catalog-command/transactions/constants/months';
import { DAY_NUMBERS } from '@/features/catalog-command/transactions/constants/days';

interface MonthDayRowProps {
  label: string;
  month: number | null;
  day: number | null;
  setMonth: (value: number | null) => void;
  setDay: (value: number | null) => void;

  /** Optional: default true. When true, uses 2-column inline layout */
  inline?: boolean;
}

export default function MonthDayRow({
  label,
  month,
  day,
  setMonth,
  setDay,
  inline = true,
}: MonthDayRowProps) {
  return (
    <div
      className={
        inline
          ? 'grid grid-cols-2 gap-3 inline-dropdown-borders'
          : 'grid grid-cols-1 md:grid-cols-2 gap-3'
      }
    >
      <div>
        <label className="block mb-2">{label} Month</label>
        <Dropdown
          value={month}
          options={[...MONTHS]}
          onChange={e => setMonth(e.value)}
          placeholder="Month"
          className="w-full"
        />
      </div>

      <div>
        <label className="block mb-2">{label} Day</label>
        <Dropdown
          value={day}
          options={[...DAY_NUMBERS]}
          onChange={e => setDay(e.value)}
          placeholder="Day"
          className="w-full"
        />
      </div>
    </div>
  );
}
