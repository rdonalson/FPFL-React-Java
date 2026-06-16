// src/features/catalog-command/transactions/components/common/WeekdayRadioGroup.tsx

import React from 'react';
import { SelectButton } from 'primereact/selectbutton';
import { WEEKDAYS } from '@/features/catalog-command/transactions/constants/weekdays';

interface WeekdayRadioGroupProps {
  value: number | null;
  onChange: (value: number) => void;
}

export default function WeekdayRadioGroup({ value, onChange }: WeekdayRadioGroupProps) {
  return (
    <SelectButton
      value={value}
      onChange={e => onChange(e.value)}
      options={WEEKDAYS}
      optionLabel="label"
      optionValue="value"
      unselectable={false} // ensures only ONE weekday can be selected
      className="w-full"
    />
  );
}
