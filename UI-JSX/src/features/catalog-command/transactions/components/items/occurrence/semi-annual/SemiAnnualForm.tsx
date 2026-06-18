// src/features/catalog-command/transactions/components/items/occurrence/semiannual/SemiAnnualForm.tsx
import React, { useEffect, useState } from 'react';
import { Card } from 'primereact/card';
import { Toast } from 'primereact/toast';
import { InputText } from 'primereact/inputtext';
import { InputNumber } from 'primereact/inputnumber';
import { Button } from 'primereact/button';

import type { Item } from '../../../../types/Item';
import MonthDayRow from '../../../common/MonthDayRow';
import { getSessionUserId } from '@/app/state/sessionHelpers';

interface SemiAnnualFormProps {
  itemType: number;
  initial: Item | null;
  create: (payload: Item) => Promise<Item>;
  update: (id: number, payload: Item) => Promise<Item>;
  onSaved: () => void;
}

export default function SemiAnnualForm({
  itemType,
  initial,
  create,
  update,
  onSaved,
}: SemiAnnualFormProps) {
  const toastRef = React.useRef<Toast | null>(null);

  const [name, setName] = useState(initial?.name ?? '');
  const [amount, setAmount] = useState<number | null>(initial?.amount ?? null);

  const [semi1Month, setSemi1Month] = useState<number | null>(initial?.semiAnnual1Month ?? null);
  const [semi1Day, setSemi1Day] = useState<number | null>(initial?.semiAnnual1Day ?? null);

  const [semi2Month, setSemi2Month] = useState<number | null>(initial?.semiAnnual2Month ?? null);
  const [semi2Day, setSemi2Day] = useState<number | null>(initial?.semiAnnual2Day ?? null);

  const [saving, setSaving] = useState(false);

  useEffect(() => {
    setName(initial?.name ?? '');
    setAmount(initial?.amount ?? null);

    setSemi1Month(initial?.semiAnnual1Month ?? null);
    setSemi1Day(initial?.semiAnnual1Day ?? null);

    setSemi2Month(initial?.semiAnnual2Month ?? null);
    setSemi2Day(initial?.semiAnnual2Day ?? null);
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

      const annums = [
        { m: semi1Month, d: semi1Day, label: '1st Annum' },
        { m: semi2Month, d: semi2Day, label: '2nd Annum' },
      ];

      for (const a of annums) {
        if (a.m === null || a.d === null) {
          toastRef.current?.show({
            severity: 'warn',
            summary: 'Validation',
            detail: `${a.label}: Month and Day are required.`,
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
        fkPeriod: 8, // ⭐ SEMI-ANNUAL

        semiAnnual1Month: semi1Month,
        semiAnnual1Day: semi1Day,

        semiAnnual2Month: semi2Month,
        semiAnnual2Day: semi2Day,

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

          {/* Semi-Annual Rows */}
          <MonthDayRow
            label="1st Annum"
            month={semi1Month}
            day={semi1Day}
            setMonth={setSemi1Month}
            setDay={setSemi1Day}
          />

          <MonthDayRow
            label="2nd Annum"
            month={semi2Month}
            day={semi2Day}
            setMonth={setSemi2Month}
            setDay={setSemi2Day}
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
