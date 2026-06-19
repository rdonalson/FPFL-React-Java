// src/features/catalog-command/transactions/components/items/occurrence/daily/DailyForm.tsx
import React, { useEffect, useState } from 'react';
import { InputText } from 'primereact/inputtext';
import { InputNumber } from 'primereact/inputnumber';
import { Button } from 'primereact/button';
import { Card } from 'primereact/card';
import { Toast } from 'primereact/toast';

import type { Item } from '../../../../types/Item';
import { getSessionUserId } from '@/app/state/sessionHelpers';

interface DailyFormProps {
  itemType: number; // 1 = Credit, 2 = Debit
  initial?: Item | null;
  onSaved?: () => void;
  create: (payload: Item) => Promise<Item>;
  update: (id: number, payload: Item) => Promise<Item>;
}

export default function DailyForm({ itemType, initial, onSaved, create, update }: DailyFormProps) {
  const toastRef = React.useRef<Toast | null>(null);

  const [name, setName] = useState<string>(initial?.name ?? '');
  const [amount, setAmount] = useState<number | null>(initial?.amount ?? null);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    setName(initial?.name ?? '');
    setAmount(initial?.amount ?? null);
  }, [initial]);

  async function handleSave(e?: React.FormEvent) {
    e?.preventDefault();
    setSaving(true);

    try {
      // Validate itemType
      if (!itemType || typeof itemType !== 'number' || itemType <= 0) {
        toastRef.current?.show({
          severity: 'error',
          summary: 'Save failed',
          detail: 'Missing item type',
        });
        setSaving(false);
        return;
      }

      const userId = getSessionUserId();
      if (!userId) {
        toastRef.current?.show({
          severity: 'error',
          summary: 'Save failed',
          detail: 'No user session',
        });
        setSaving(false);
        return;
      }

      if (!name?.trim()) {
        toastRef.current?.show({
          severity: 'warn',
          summary: 'Validation',
          detail: 'Name is required.',
        });
        setSaving(false);
        return;
      }

      if (amount === null || Number.isNaN(amount)) {
        toastRef.current?.show({
          severity: 'warn',
          summary: 'Validation',
          detail: 'Amount is required.',
        });
        setSaving(false);
        return;
      }

      // ⭐ Daily occurrence has NO beginDate or endDate
      const payload: Item = {
        id: initial?.id,
        userId,
        name: name.trim(),
        amount,
        fkPeriod: 2, // ⭐ DAILY
        fkItemType: itemType, // required by backend
        dateRangeReq: false, // required by backend for validation
      } as Item;

      console.debug('DailyForm payload:', payload);

      if (initial && initial.id) {
        await update(initial.id, payload);
        toastRef.current?.show({ severity: 'success', summary: 'Saved', detail: 'Item updated.' });
      } else {
        await create(payload);
        toastRef.current?.show({ severity: 'success', summary: 'Saved', detail: 'Item created.' });
      }

      onSaved?.();
    } catch (err: any) {
      console.error('Save failed', err);
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
          <div>
            <label className="block mb-2">Name</label>
            <InputText value={name} onChange={e => setName(e.target.value)} />
          </div>

          <div>
            <label className="block mb-2">Amount</label>
            <InputNumber
              value={amount}
              onValueChange={e => setAmount(e.value as number)}
              mode="currency"
              currency="USD"
              locale="en-US"
              showButtons={false}
            />
          </div>

          {/* ⭐ No date field for Daily */}
        </div>
      </Card>

      <div className="mt-3 flex gap-2">
        <Button label="Save" icon="pi pi-check" type="submit" loading={saving} />
        <Button
          label="Cancel"
          icon="pi pi-times"
          className="p-button-secondary"
          onClick={() => onSaved?.()}
        />
      </div>
    </form>
  );
}
