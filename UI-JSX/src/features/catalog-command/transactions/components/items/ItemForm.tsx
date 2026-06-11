// src/features/catalog-command/transactions/components/items/ItemForm.tsx
import { useEffect, useState } from 'react';
import { useItem } from '../../hooks/useItem';
import { getSessionUserId } from '@/api/utils/userId';
import { InputText } from 'primereact/inputtext';
import { InputNumber } from 'primereact/inputnumber';
import { Dropdown } from 'primereact/dropdown';
import { Button } from 'primereact/button';
import { Item, TimePeriodDto } from '../../types/Item';

interface ItemFormProps {
  itemTypeId: number;
  defaultName: string;
  selectedItem: Item | null;
  clearSelection: () => void;
}

export function ItemForm({ itemTypeId, defaultName, selectedItem, clearSelection }: ItemFormProps) {
  const { create, update } = useItem();
  const userId = getSessionUserId();

  const [name, setName] = useState(defaultName);
  const [amount, setAmount] = useState<number | null>(0);
  const [fkPeriod, setFkPeriod] = useState<number | null>(null);
  const [periods] = useState<TimePeriodDto[]>([]);
  // Snapshot of selected item to avoid lint error
  const [editSnapshot, setEditSnapshot] = useState<Item | null>(null);

  // When selectedItem changes, update snapshot
  useEffect(() => {
    setEditSnapshot(selectedItem);
  }, [selectedItem]);

  // Initialize form fields from snapshot
  useEffect(() => {
    if (editSnapshot) {
      setName(editSnapshot.name ?? defaultName);
      setAmount(editSnapshot.amount);
      setFkPeriod(editSnapshot.TimePeriod?.Id ?? null);
    } else {
      setName(defaultName);
      setAmount(0);
      setFkPeriod(null);
    }
  }, [editSnapshot, defaultName]);

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();

    const payload: Item = {
      id: selectedItem?.id ?? 0,
      userId,
      name,
      amount: amount ?? 0,
      fkItemType: itemTypeId,
      fkPeriod,
      dateRangeReq: false,
    };

    if (selectedItem) {
      await update(selectedItem.id!, payload);
    } else {
      await create(payload);
    }

    clearSelection();
  }

  return (
    <form onSubmit={handleSubmit} className="flex flex-col gap-4">
      <div className="flex flex-col gap-1">
        <label>Name</label>
        <InputText value={name} onChange={e => setName(e.target.value)} />
      </div>

      <div className="flex flex-col gap-1">
        <label>Amount</label>
        <InputNumber
          value={amount}
          onValueChange={e => setAmount(e.value ?? 0)}
          mode="currency"
          currency="USD"
        />
      </div>

      <div className="flex flex-col gap-1">
        <label>Time Period</label>
        <Dropdown
          value={fkPeriod}
          onChange={e => setFkPeriod(e.value)}
          options={periods.map(p => ({
            label: p.Name,
            value: p.Id,
          }))}
          placeholder="Select Time Period"
        />
      </div>

      <div className="flex gap-2">
        <Button type="submit" label={selectedItem ? 'Update' : 'Add'} />
        {selectedItem && (
          <Button type="button" label="Cancel" severity="secondary" onClick={clearSelection} />
        )}
      </div>
    </form>
  );
}
