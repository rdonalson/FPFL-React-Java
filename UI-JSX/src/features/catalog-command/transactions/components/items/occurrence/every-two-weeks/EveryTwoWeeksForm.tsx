// src/features/catalog-command/transactions/components/items/occurrence/every-two-weeks/EveryTwoWeeksForm.tsx
import React, { useEffect, useState } from 'react';
import { Card } from 'primereact/card';
import { Toast } from 'primereact/toast';
import { InputText } from 'primereact/inputtext';
import { InputNumber } from 'primereact/inputnumber';
import { Calendar } from 'primereact/calendar';
import { Button } from 'primereact/button';

import type { Item } from '../../../../types/Item';
import { getSessionUserId } from '@/app/state/sessionHelpers';
import { WeekdayRadioGroup } from '@/features/catalog-command/transactions/components/common/WeekdayRadioGroup';

interface EveryTwoWeeksFormProps {
  itemType: number;
  initial: Item | null;
  create: (payload: Item) => Promise<Item>;
  update: (id: number, payload: Item) => Promise<Item>;
  onSaved: () => void;
}

export default function EveryTwoWeeksForm({
  itemType,
  initial,
  create,
  update,
  onSaved,
}: EveryTwoWeeksFormProps) {
  const toastRef = React.useRef<Toast | null>(null);

  const [name, setName] = useState(initial?.name ?? '');
  const [amount, setAmount] = useState<number | null>(initial?.amount ?? null);
  const [beginDate, setBeginDate] = useState<Date | null>(
    initial?.beginDate ? new Date(initial.beginDate) : null,
  );
  const [everyOtherWeekDow, setEveryOtherWeekDow] = useState<number | null>(
    initial?.everyOtherWeekDow ?? null,
  );
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    setName(initial?.name ?? '');
    setAmount(initial?.amount ?? null);
    setBeginDate(initial?.beginDate ? new Date(initial.beginDate) : null);
    setEveryOtherWeekDow(initial?.everyOtherWeekDow ?? null);
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

      if (!beginDate) {
        toastRef.current?.show({
          severity: 'warn',
          summary: 'Validation',
          detail: 'Inception date is required.',
        });
        setSaving(false);
        return;
      }

      if (everyOtherWeekDow === null) {
        toastRef.current?.show({
          severity: 'warn',
          summary: 'Validation',
          detail: 'Please select a weekday.',
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
        fkPeriod: 4, // ⭐ EVERY TWO WEEKS
        beginDate: beginDate.toISOString(),
        everyOtherWeekDow, // ⭐ correct backend field
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

          {/* Inception Date */}
          <div>
            <label className="block mb-2">Inception Date</label>
            <Calendar
              value={beginDate}
              onChange={e => setBeginDate(e.value as Date)}
              showIcon
              className="w-full"
            />
          </div>

          {/* Weekday Selector */}
          <div className="col-span-2">
            <label className="block mb-2">Select Weekday</label>
            <WeekdayRadioGroup value={everyOtherWeekDow} onChange={setEveryOtherWeekDow} />
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
