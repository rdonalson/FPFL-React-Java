// src/features/catalog-command/transactions/components/common/WeekdayRadioGroup.tsx

import React from 'react';
import { RadioButton } from 'primereact/radiobutton';
import { WEEKDAYS } from '@/features/catalog-command/transactions/constants/weekdays';

interface WeekdayRadioGroupProps {
  value: number | null;
  onChange: (value: number) => void;
}

export default function WeekdayRadioGroup({ value, onChange }: WeekdayRadioGroupProps) {
  return (
    <div className="flex flex-wrap gap-3">
      {WEEKDAYS.map(day => (
        <div key={day.value} className="flex align-items-center">
          <RadioButton
            inputId={`weekday-${day.value}`}
            name="weekday"
            value={day.value}
            checked={value === day.value}
            onChange={e => onChange(e.value)}
          />
          <label htmlFor={`weekday-${day.value}`} className="ml-2">
            {day.label}
          </label>
        </div>
      ))}
    </div>
  );
}
