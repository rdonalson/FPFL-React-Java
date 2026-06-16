import { RadioOption } from './RadioOption.tsx';
import { WEEKDAYS } from '../../constants/weekdays';

export interface WeekdayRadioGroupProps {
  value: number | null;
  onChange: (value: number | null) => void;
  className?: string;
}

export function WeekdayRadioGroup({ value, onChange, className = '' }: WeekdayRadioGroupProps) {
  return (
    <div className={`flex gap-4 ${className}`}>
      {WEEKDAYS.map(d => (
        <RadioOption
          key={d.value}
          label={d.label}
          value={d.value}
          selected={value}
          onChange={onChange}
        />
      ))}
    </div>
  );
}
