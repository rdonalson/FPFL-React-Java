import React, { useEffect, useState } from 'react';
import { Card } from 'primereact/card';
import { Toast } from 'primereact/toast';
import { InputText } from 'primereact/inputtext';
import { InputNumber } from 'primereact/inputnumber';
import { Button } from 'primereact/button';

import type { Item } from '../../../../types/Item';
import MonthDayRow from '../../../common/MonthDayRow';
import { getSessionUserId } from '@/app/state/sessionHelpers';

interface AnnualFormProps {
  itemType: number;
  initial: Item | null;
  create: (payload: Item) => Promise<Item>;
  update: (id: number, payload: Item) => Promise<Item>;
  onSaved: () => void;
}

export default function AnnualForm({
  itemType,
  initial,
  create,
  update,
  onSaved,
}: AnnualFormProps) {
  const toastRef = React.useRef<Toast | null>(null);

  const [name, setName] = useState(initial?.name ?? '');
  const [amount, setAmount] = useState<number | null>(initial?.amount ?? null);

  const [annualMoy, setAnnualMoy] = useState<number | null>(initial?.annualMoy ?? null);
  const [annualDom, setAnnualDom] = useState<number | null>(initial?.annualDom ?? null);

  const [saving, setSaving] = useState(false);

  useEffect(() => {
    setName(initial?.name ?? '');
    setAmount(initial?.amount ?? null);

    setAnnualMoy(initial?.annualMoy ?? null);
    setAnnualDom(initial?.annualDom ?? null);
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

      if (annualMoy === null || annualDom === null) {
        toastRef.current?.show({
          severity: 'warn',
          summary: 'Validation',
          detail: 'Annual Month and Day are required.',
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
        fkPeriod: 9, // ⭐ ANNUAL

        annualMoy,
        annualDom,

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

          {/* Annual Row */}
          <MonthDayRow
            label="Annual"
            month={annualMoy}
            day={annualDom}
            setMonth={setAnnualMoy}
            setDay={setAnnualDom}
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
