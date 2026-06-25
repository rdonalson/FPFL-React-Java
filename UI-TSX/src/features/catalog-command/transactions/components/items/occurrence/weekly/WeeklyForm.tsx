// src/features/catalog-command/transactions/components/items/occurrence/weekly/WeeklyForm.tsx
import React, { useEffect, useState } from 'react';
import { Card } from 'primereact/card';
import { Toast } from 'primereact/toast';

import type { Item } from '../../../../types/Item';
import { getSessionUserId } from '@/app/state/sessionHelpers';

import { WeekdayRadioGroup } from '@/features/catalog-command/transactions/components/common/WeekdayRadioGroup';
import { TimeFrameSelector } from '@/features/catalog-command/transactions/components/common/TimeFrameSelector';
import { HeaderFields } from '@/features/catalog-command/transactions/components/common/HeaderFields';
import { FormLayout } from '@/features/catalog-command/transactions/components/common/FormLayout';

import { parseDateOnlyString, toDateOnlyString } from '@/shared/utils/dateUtils';

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

  const [dateRangeReq, setDateRangeReq] = useState(initial?.dateRangeReq ?? false);
  const [beginDate, setBeginDate] = useState<Date | null>(
    initial?.beginDate ? parseDateOnlyString(initial.beginDate) : null,
  );
  const [endDate, setEndDate] = useState<Date | null>(
    initial?.endDate ? parseDateOnlyString(initial.endDate) : null,
  );

  const [saving, setSaving] = useState(false);

  useEffect(() => {
    setName(initial?.name ?? '');
    setAmount(initial?.amount ?? null);
    setWeeklyDow(initial?.weeklyDow ?? null);

    setDateRangeReq(initial?.dateRangeReq ?? false);
    setBeginDate(initial?.beginDate ? parseDateOnlyString(initial.beginDate) : null);
    setEndDate(initial?.endDate ? parseDateOnlyString(initial.endDate) : null);
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
        fkPeriod: 3, // WEEKLY
        weeklyDow,

        dateRangeReq,
        beginDate: dateRangeReq ? toDateOnlyString(beginDate) : null,
        endDate: dateRangeReq ? toDateOnlyString(endDate) : null,
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

            {/* Weekday Selector */}
            <div className="col-span-1 sm:col-span-2">
              <label className="block mb-2">Select Weekday</label>
              <WeekdayRadioGroup value={weeklyDow} onChange={setWeeklyDow} />
            </div>

            {/* Date Range Selector */}
            <div className="col-span-1 sm:col-span-2">
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
          </FormLayout>
        </Card>
      </div>
    </>
  );
}
