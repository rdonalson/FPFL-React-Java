import React, { useEffect, useState } from 'react';
import { Card } from 'primereact/card';
import { Toast } from 'primereact/toast';
import { Button } from 'primereact/button';

import type { Item } from '../../../../types/Item';
import { getSessionUserId } from '@/app/state/sessionHelpers';
import { WeekdayRadioGroup } from '@/features/catalog-command/transactions/components/common/WeekdayRadioGroup';
import { TimeFrameSelector } from '@/features/catalog-command/transactions/components/common/TimeFrameSelector';
import { HeaderFields } from '@/features/catalog-command/transactions/components/common/HeaderFields';

interface WeeklyFormProps {
  itemType: number;
  initial: Item | null;
  create: (payload: Item) => Promise<Item>;
  update: (id: number, payload: Item) => Promise<Item>;
  onSaved: () => void;
}

export default function WeeklyForm({
  itemType,
  initial,
  create,
  update,
  onSaved,
}: WeeklyFormProps) {
  const toastRef = React.useRef<Toast | null>(null);

  const [name, setName] = useState(initial?.name ?? '');
  const [amount, setAmount] = useState<number | null>(initial?.amount ?? null);
  const [weeklyDow, setWeeklyDow] = useState<number | null>(initial?.weeklyDow ?? null);

  // Time frame state
  const [dateRangeReq, setDateRangeReq] = useState(initial?.dateRangeReq ?? false);
  const [beginDate, setBeginDate] = useState<Date | null>(
    initial?.beginDate ? new Date(initial.beginDate) : null,
  );
  const [endDate, setEndDate] = useState<Date | null>(
    initial?.endDate ? new Date(initial.endDate) : null,
  );

  const [saving, setSaving] = useState(false);

  useEffect(() => {
    setName(initial?.name ?? '');
    setAmount(initial?.amount ?? null);
    setWeeklyDow(initial?.weeklyDow ?? null);

    setDateRangeReq(initial?.dateRangeReq ?? false);
    setBeginDate(initial?.beginDate ? new Date(initial.beginDate) : null);
    setEndDate(initial?.endDate ? new Date(initial.endDate) : null);
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

      if (weeklyDow === null) {
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
        fkPeriod: 3,
        weeklyDow,

        dateRangeReq,
        beginDate: dateRangeReq && beginDate ? beginDate.toISOString() : null,
        endDate: dateRangeReq && endDate ? endDate.toISOString() : null,
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

      {/* Card goes full width on mobile */}
      <div className="p-0 md:p-4 w-full">
        <Card className="w-full">
          {/* HeaderFields replaces Name + Amount */}
          <HeaderFields
            name={name}
            amount={amount}
            onNameChange={setName}
            onAmountChange={setAmount}
          />

          {/* Weekday Selector */}
          <div className="mt-4">
            <label className="block mb-2">Select Weekday</label>
            <WeekdayRadioGroup value={weeklyDow} onChange={setWeeklyDow} />
          </div>

          {/* Time Frame Selector */}
          <div className="mt-4">
            <TimeFrameSelector
              dateRangeReq={dateRangeReq}
              beginDate={beginDate}
              endDate={endDate}
              onChange={v => {
                setDateRangeReq(v.dateRangeReq);
                setBeginDate(v.beginDate);
                setEndDate(v.endDate);
              }}
            />
          </div>
        </Card>
      </div>

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
