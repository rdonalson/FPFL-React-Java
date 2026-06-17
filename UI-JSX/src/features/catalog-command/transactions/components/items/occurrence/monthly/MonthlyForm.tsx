// src/features/catalog-command/transactions/components/items/occurrence/monthly/MonthlyForm.tsx
import React, { useEffect, useState } from 'react';
import { Card } from 'primereact/card';
import { Toast } from 'primereact/toast';
import { InputText } from 'primereact/inputtext';
import { InputNumber } from 'primereact/inputnumber';
import { Dropdown } from 'primereact/dropdown';
import { Button } from 'primereact/button';

import type { Item } from '../../../../types/Item';
import { getSessionUserId } from '@/app/state/sessionHelpers';
import { DAY_NUMBERS } from '@/features/catalog-command/transactions/constants/days';

interface MonthlyFormProps {
  itemType: number;
  initial: Item | null;
  create: (payload: Item) => Promise<Item>;
  update: (id: number, payload: Item) => Promise<Item>;
  onSaved: () => void;
}

export default function MonthlyForm({
  itemType,
  initial,
  create,
  update,
  onSaved,
}: MonthlyFormProps) {
  const toastRef = React.useRef<Toast | null>(null);

  const [name, setName] = useState(initial?.name ?? '');
  const [amount, setAmount] = useState<number | null>(initial?.amount ?? null);
  const [monthlyDom, setMonthlyDom] = useState<number | null>(initial?.monthlyDom ?? null);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    setName(initial?.name ?? '');
    setAmount(initial?.amount ?? null);
    setMonthlyDom(initial?.monthlyDom ?? null);
  }, [initial]);

  async function handleSave(e?: React.FormEvent) {
    e?.preventDefault();
    setSaving(true);

    try {
      const userId = getSessionUserId();
      if (!userId) throw new Error('No user session');

      if (!name.trim()) {
        toastRef.current?.show({
          severity: 'warn',
          summary: 'Validation',
          detail: 'Name is required.',
        });
        setSaving(false);
        return;
      }

      if (amount === null) {
        toastRef.current?.show({
          severity: 'warn',
          summary: 'Validation',
          detail: 'Amount is required.',
        });
        setSaving(false);
        return;
      }

      if (monthlyDom === null) {
        toastRef.current?.show({
          severity: 'warn',
          summary: 'Validation',
          detail: 'Please select a day of the month.',
        });
        setSaving(false);
        return;
      }

      const payload: Item = {
        id: initial?.id,
        userId,
        name: name.trim(),
        amount,
        fkItemType: itemType,
        fkPeriod: 6, // ⭐ MONTHLY
        monthlyDom, // ⭐ correct backend field
      };

      if (initial?.id) {
        await update(initial.id, payload);
        toastRef.current?.show({
          severity: 'success',
          summary: 'Updated',
          detail: 'Item updated successfully.',
        });
      } else {
        await create(payload);
        toastRef.current?.show({
          severity: 'success',
          summary: 'Created',
          detail: 'Item created successfully.',
        });
      }

      onSaved();
    } catch (err: any) {
      toastRef.current?.show({
        severity: 'error',
        summary: 'Save failed',
        detail: err?.message ?? 'Unknown error',
      });
    } finally {
      setSaving(false);
    }
  }

  return (
    <form onSubmit={handleSave}>
      <Toast ref={toastRef} />

      <Card>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
          {/* Name */}
          <div>
            <label className="block mb-2">Name</label>
            <InputText value={name} onChange={e => setName(e.target.value)} className="w-full" />
          </div>

          {/* Amount */}
          <div>
            <label className="block mb-2">Amount</label>
            <InputNumber
              value={amount}
              onValueChange={e => setAmount(e.value as number)}
              mode="currency"
              currency="USD"
              locale="en-US"
              className="w-full"
            />
          </div>

          {/* Day of Month Selector */}
          <div className="col-span-2">
            <label className="block mb-2">Select Day of Month</label>
            <Dropdown
              value={monthlyDom}
              options={[...DAY_NUMBERS]} // ⭐ mutable copy for PrimeReact
              onChange={e => setMonthlyDom(e.value)}
              placeholder="Day"
              style={{ width: '120px' }} // ⭐ narrow like Bi-Monthly
            />
          </div>
        </div>
      </Card>

      <div className="mt-3 flex gap-2">
        <Button label="Save" icon="pi pi-check" type="submit" loading={saving} />
        <Button
          label="Cancel"
          icon="pi pi-times"
          className="p-button-secondary"
          type="button"
          onClick={onSaved}
        />
      </div>
    </form>
  );
}
