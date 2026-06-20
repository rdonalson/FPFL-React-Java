import { InputText } from 'primereact/inputtext';
import { InputNumber } from 'primereact/inputnumber';

interface HeaderFieldsProps {
  name: string;
  amount: number | null;
  onNameChange: (v: string) => void;
  onAmountChange: (v: number | null) => void;
}

export function HeaderFields({ name, amount, onNameChange, onAmountChange }: HeaderFieldsProps) {
  return (
    <div className="w-full flex flex-col md:flex-row gap-3">
      {/* Name: 2/3 width on desktop, full width on mobile */}
      <div className="flex flex-col w-full md:basis-2/3">
        <label className="mb-2">Name</label>
        <InputText value={name} onChange={e => onNameChange(e.target.value)} className="w-full" />
      </div>

      {/* Amount: 1/3 width on desktop, full width on mobile */}
      <div className="flex flex-col w-full md:basis-1/3">
        <label className="mb-2">Amount</label>
        <InputNumber
          value={amount}
          onValueChange={e => onAmountChange(e.value as number)}
          mode="currency"
          currency="USD"
          locale="en-US"
          className="w-full"
        />
      </div>
    </div>
  );
}
