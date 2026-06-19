// src/features/catalog-command/transactions/components/items/occurrence/quarterly/QuarterlyForm.tsx
import React, { useEffect, useState } from 'react';
import { Card } from 'primereact/card';
import { Toast } from 'primereact/toast';
import { InputText } from 'primereact/inputtext';
import { InputNumber } from 'primereact/inputnumber';
import { Button } from 'primereact/button';

import type { Item } from '../../../../types/Item';
import { getSessionUserId } from '@/app/state/sessionHelpers';
import MonthDayRow from '../../../common/MonthDayRow';

interface QuarterlyFormProps {
  itemType: number;
  initial: Item | null;
  create: (payload: Item) => Promise<Item>;
  update: (id: number, payload: Item) => Promise<Item>;
  onSaved: () => void;
}

export default function QuarterlyForm({
  itemType,
  initial,
  create,
  update,
  onSaved,
}: QuarterlyFormProps) {
  const toastRef = React.useRef<Toast | null>(null);

  const [name, setName] = useState(initial?.name ?? '');
  const [amount, setAmount] = useState<number | null>(initial?.amount ?? null);

  const [q1Month, setQ1Month] = useState<number | null>(initial?.quarterly1Month ?? null);
  const [q1Day, setQ1Day] = useState<number | null>(initial?.quarterly1Day ?? null);

  const [q2Month, setQ2Month] = useState<number | null>(initial?.quarterly2Month ?? null);
  const [q2Day, setQ2Day] = useState<number | null>(initial?.quarterly2Day ?? null);

  const [q3Month, setQ3Month] = useState<number | null>(initial?.quarterly3Month ?? null);
  const [q3Day, setQ3Day] = useState<number | null>(initial?.quarterly3Day ?? null);

  const [q4Month, setQ4Month] = useState<number | null>(initial?.quarterly4Month ?? null);
  const [q4Day, setQ4Day] = useState<number | null>(initial?.quarterly4Day ?? null);

  const [saving, setSaving] = useState(false);

  useEffect(() => {
    setName(initial?.name ?? '');
    setAmount(initial?.amount ?? null);

    setQ1Month(initial?.quarterly1Month ?? null);
    setQ1Day(initial?.quarterly1Day ?? null);

    setQ2Month(initial?.quarterly2Month ?? null);
    setQ2Day(initial?.quarterly2Day ?? null);

    setQ3Month(initial?.quarterly3Month ?? null);
    setQ3Day(initial?.quarterly3Day ?? null);

    setQ4Month(initial?.quarterly4Month ?? null);
    setQ4Day(initial?.quarterly4Day ?? null);
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

      const quarters = [
        { m: q1Month, d: q1Day, label: '1st Quarter' },
        { m: q2Month, d: q2Day, label: '2nd Quarter' },
        { m: q3Month, d: q3Day, label: '3rd Quarter' },
        { m: q4Month, d: q4Day, label: '4th Quarter' },
      ];

      for (const q of quarters) {
        if (q.m === null || q.d === null) {
          toastRef.current?.show({
            severity: 'warn',
            summary: 'Validation',
            detail: `${q.label}: Month and Day are required.`,
          });
          setSaving(false);
          return;
        }
      }

      const payload: Item = {
        id: initial?.id,
        userId,
        name: name.trim(),
        amount,
        fkItemType: itemType,
        fkPeriod: 7, // ⭐ QUARTERLY

        quarterly1Month: q1Month,
        quarterly1Day: q1Day,

        quarterly2Month: q2Month,
        quarterly2Day: q2Day,

        quarterly3Month: q3Month,
        quarterly3Day: q3Day,

        quarterly4Month: q4Month,
        quarterly4Day: q4Day,

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

          {/* Quarter Rows */}
          <MonthDayRow
            label="1st Quarter"
            month={q1Month}
            day={q1Day}
            setMonth={setQ1Month}
            setDay={setQ1Day}
          />

          <MonthDayRow
            label="2nd Quarter"
            month={q2Month}
            day={q2Day}
            setMonth={setQ2Month}
            setDay={setQ2Day}
          />

          <MonthDayRow
            label="3rd Quarter"
            month={q3Month}
            day={q3Day}
            setMonth={setQ3Month}
            setDay={setQ3Day}
          />

          <MonthDayRow
            label="4th Quarter"
            month={q4Month}
            day={q4Day}
            setMonth={setQ4Month}
            setDay={setQ4Day}
          />
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
