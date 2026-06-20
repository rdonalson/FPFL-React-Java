// src/features/catalog-command/transactions/components/items/occurrence/bi-monthly/BiMonthlyForm.tsx
import React, { useEffect, useState } from 'react';
import { Card } from 'primereact/card';
import { Toast } from 'primereact/toast';
import { InputText } from 'primereact/inputtext';
import { InputNumber } from 'primereact/inputnumber';
import { Dropdown } from 'primereact/dropdown';
import { Button } from 'primereact/button';

import type { Item } from '../../../../types/Item';
import { getSessionUserId } from '@/app/state/sessionHelpers';

interface BiMonthlyFormProps {
  itemType: number;
  initial: Item | null;
  create: (payload: Item) => Promise<Item>;
  update: (id: number, payload: Item) => Promise<Item>;
  onSaved: () => void;
}

export default function BiMonthlyForm({
  itemType,
  initial,
  create,
  update,
  onSaved,
}: BiMonthlyFormProps) {
  const toastRef = React.useRef<Toast | null>(null);

  const [name, setName] = useState(initial?.name ?? '');
  const [amount, setAmount] = useState<number | null>(initial?.amount ?? null);
  const [biMonthlyDay1, setBiMonthlyDay1] = useState<number | null>(initial?.biMonthlyDay1 ?? null);
  const [biMonthlyDay2, setBiMonthlyDay2] = useState<number | null>(initial?.biMonthlyDay2 ?? null);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    setName(initial?.name ?? '');
    setAmount(initial?.amount ?? null);
    setBiMonthlyDay1(initial?.biMonthlyDay1 ?? null);
    setBiMonthlyDay2(initial?.biMonthlyDay2 ?? null);
  }, [initial]);

  const dayOptions = Array.from({ length: 27 }, (_, i) => ({
    label: `${i + 1}`,
    value: i + 1,
  }));

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

      if (biMonthlyDay1 === null || biMonthlyDay2 === null) {
        toastRef.current?.show({
          severity: 'warn',
          summary: 'Validation',
          detail: 'Both bi-monthly days are required.',
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
        fkPeriod: 5, // ⭐ BI-MONTHLY
        biMonthlyDay1,
        biMonthlyDay2,
        dateRangeReq: false,
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

          <div className="grid grid-cols-2 gap-3 mt-2 inline-dropdown-borders">
            {/* Bi-Monthly Day 1 */}
            <div>
              <label className="block mb-2">Day 1</label>
              <Dropdown
                value={biMonthlyDay1}
                options={dayOptions}
                onChange={e => setBiMonthlyDay1(e.value)}
                placeholder="Select Day"
                className="w-full"
              />
            </div>

            {/* Bi-Monthly Day 2 */}
            <div>
              <label className="block mb-2">Day 2</label>
              <Dropdown
                value={biMonthlyDay2}
                options={dayOptions}
                onChange={e => setBiMonthlyDay2(e.value)}
                placeholder="Select Day"
                className="w-full"
              />
            </div>
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
