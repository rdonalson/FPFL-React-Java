// src/features/catalog-command/transactions/components/items/occurrence/onetime/OneTimeForm.tsx
import React, { useEffect, useState } from 'react';
import { Card } from 'primereact/card';
import { Toast } from 'primereact/toast';
import { Calendar } from 'primereact/calendar';

import type { Item } from '../../../../types/Item';
import { getSessionUserId } from '@/app/state/sessionHelpers';
import { HeaderFields } from '@/features/catalog-command/transactions/components/common/HeaderFields';
import { FormLayout } from '@/features/catalog-command/transactions/components/common/FormLayout';
import { parseDateOnlyString, toDateOnlyString } from '@/shared/utils/dateUtils';

interface OneTimeFormProps {
  itemType: number;
  initial: Item | null;
  create: (payload: Item) => Promise<Item>;
  update: (id: number, payload: Item) => Promise<Item>;
  onSaved: () => void;
}

export default function OneTimeForm({
  itemType,
  initial,
  create,
  update,
  onSaved,
}: OneTimeFormProps) {
  const toastRef = React.useRef<Toast | null>(null);

  const [name, setName] = useState(initial?.name ?? '');
  const [amount, setAmount] = useState<number | null>(initial?.amount ?? null);

  // One-time occurrence uses beginDate only
  const [beginDate, setBeginDate] = useState<Date | null>(
    initial?.beginDate ? parseDateOnlyString(initial.beginDate) : null,
  );

  const [saving, setSaving] = useState(false);

  useEffect(() => {
    setName(initial?.name ?? '');
    setAmount(initial?.amount ?? null);
    setBeginDate(initial?.beginDate ? parseDateOnlyString(initial.beginDate) : null);
  }, [initial]);

  async function handleSave() {
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
          detail: 'Please select a date.',
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
        fkPeriod: 1, // One-time occurrence
        weeklyDow: null,

        // One-time always uses a date range with only beginDate
        dateRangeReq: true,
        beginDate: toDateOnlyString(beginDate),
        endDate: null,
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
    <>
      <Toast ref={toastRef} />

      <div className="p-0 md:p-4 w-full">
        <Card className="w-full">
          <FormLayout saving={saving} onCancel={onSaved} onSave={handleSave}>
            {/* Name + Amount */}
            <div className="col-span-1 sm:col-span-2">
              <HeaderFields
                name={name}
                amount={amount}
                onNameChange={setName}
                onAmountChange={setAmount}
              />
            </div>

            {/* Occurrence Date */}
            <div className="col-span-1 sm:col-span-2 mt-2">
              <label className="block mb-2">Date of Occurrence</label>
              <Calendar
                value={beginDate}
                onChange={e => setBeginDate(e.value ?? null)}
                showIcon
                placeholder="Select date"
              />
            </div>
          </FormLayout>
        </Card>
      </div>
    </>
  );
}
