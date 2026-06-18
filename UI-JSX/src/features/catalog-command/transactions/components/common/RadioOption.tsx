// src/features/catalog-command/transactions/components/common/RadioOption.tsx
import { RadioButton } from 'primereact/radiobutton';

export interface RadioOptionProps {
  label: string;
  value: number;
  selected: number | null;
  onChange: (value: number | null) => void;
  className?: string;
}

export function RadioOption({
  label,
  value,
  selected,
  onChange,
  className = '',
}: RadioOptionProps) {
  return (
    <div className={`flex items-center gap-2 cursor-pointer ${className}`}>
      <RadioButton
        inputId={`weekday-${value}`}
        value={value}
        checked={selected === value}
        onChange={e => onChange(e.value as number)}
      />
      <label htmlFor={`weekday-${value}`} className="cursor-pointer select-none">
        {label}
      </label>
    </div>
  );
}
