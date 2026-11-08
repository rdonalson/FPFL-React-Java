// src/features/accounts/components/Segmented.tsx

export interface SegmentedOption<T> {
  label: string;
  value: T;
  icon?: string;
}

interface SegmentedProps<T> {
  options: readonly SegmentedOption<T>[];
  value: T;
  onChange: (value: T) => void;
  /** Names the group for screen readers. */
  ariaLabel: string;
  size?: 'normal' | 'small';
}

/**
 * A row of joined toggle buttons where exactly one is chosen. Plain buttons rather than
 * PrimeReact's SelectButton: the app theme paints every .p-button solid green, which made each
 * option look selected.
 */
export function Segmented<T extends string | number>({
  options,
  value,
  onChange,
  ariaLabel,
  size = 'normal',
}: SegmentedProps<T>) {
  return (
    <div
      role="radiogroup"
      aria-label={ariaLabel}
      className={`fl-segmented ${size === 'small' ? 'fl-segmented--small' : ''}`}
    >
      {options.map(option => {
        const chosen = option.value === value;
        return (
          <button
            key={String(option.value)}
            type="button"
            role="radio"
            aria-checked={chosen}
            className={`fl-segmented__option ${chosen ? 'fl-segmented__option--chosen' : ''}`}
            onClick={() => onChange(option.value)}
          >
            {option.icon && <i className={option.icon} aria-hidden="true" />}
            {option.label}
          </button>
        );
      })}
    </div>
  );
}
