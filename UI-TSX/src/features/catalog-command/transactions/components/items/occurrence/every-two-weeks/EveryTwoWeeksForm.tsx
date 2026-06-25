// src/features/catalog-command/transactions/components/items/occurrence/every-two-weeks/EveryTwoWeeksForm.tsx
import React, { useEffect, useState } from 'react';
import { Card } from 'primereact/card';
import { Toast } from 'primereact/toast';
import { Calendar } from 'primereact/calendar';
import { Checkbox } from 'primereact/checkbox';

import type { Item } from '../../../../types/Item';
import { getSessionUserId } from '@/app/state/sessionHelpers';

import { HeaderFields } from '@/features/catalog-command/transactions/components/common/HeaderFields';
import { WeekdayRadioGroup } from '@/features/catalog-command/transactions/components/common/WeekdayRadioGroup';
import { FormLayout } from '@/features/catalog-command/transactions/components/common/FormLayout';

import { parseDateOnlyString, toDateOnlyString } from '@/shared/utils/dateUtils';

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

  // Required inception date
  const [beginDate, setBeginDate] = useState<Date | null>(
    initial?.beginDate ? parseDateOnlyString(initial.beginDate) : null,
  );

  // Optional end date
  const [endDate, setEndDate] = useState<Date | null>(
    initial?.endDate ? parseDateOnlyString(initial.endDate) : null,
  );

  // Weekday selector
  const [everyOtherWeekDow, setEveryOtherWeekDow] = useState<number | null>(
    initial?.everyOtherWeekDow ?? null,
  );

  // ⭐ NEW: Date range checkbox (controls only end date)
  const [dateRangeReq, setDateRangeReq] = useState(initial?.dateRangeReq ?? false);

  const [saving, setSaving] = useState(false);

  useEffect(() => {
    setName(initial?.name ?? '');
    setAmount(initial?.amount ?? null);

    setBeginDate(initial?.beginDate ? parseDateOnlyString(initial.beginDate) : null);
    setEndDate(initial?.endDate ? parseDateOnlyString(initial.endDate) : null);

    setEveryOtherWeekDow(initial?.everyOtherWeekDow ?? null);
    setDateRangeReq(initial?.dateRangeReq ?? false);
  }, [initial]);

  async function handleSave() {
    setSaving(true);

    try {
      const userId = getSessionUserId();
      if (!userId) throw new Error('No user session');

      // Validation
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

      // If checkbox is on, end date must be selected
      if (dateRangeReq && !endDate) {
        toastRef.current?.show({
          severity: 'warn',
          summary: 'Validation',
          detail: 'Please select an end date.',
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
        fkPeriod: 4, // EVERY TWO WEEKS

        beginDate: toDateOnlyString(beginDate),
        endDate: dateRangeReq ? toDateOnlyString(endDate) : null,

        everyOtherWeekDow,

        dateRangeReq,
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

            {/* Inception Date */}
            <div className="col-span-1 sm:col-span-2 ">
              <label className="block mb-2">Inception Date</label>
              <Calendar
                value={beginDate}
                onChange={e => setBeginDate(e.value as Date)}
                showIcon
                className="w-full"
              />
            </div>

            {/* Weekday Selector */}
            <div className="col-span-1 sm:col-span-2 ">
              <label className="block mb-2">Select Weekday</label>
              <WeekdayRadioGroup value={everyOtherWeekDow} onChange={setEveryOtherWeekDow} />
            </div>

            {/* Date Range Checkbox */}
            <div className="col-span-1 sm:col-span-2 mt-2 flex items-center gap-2">
              <Checkbox checked={dateRangeReq} onChange={e => setDateRangeReq(e.checked!)} />
              <label>Use Time Frame</label>
            </div>

            {/* End Date (only when checkbox is checked) */}
            {dateRangeReq && (
              <div className="col-span-1 sm:col-span-2">
                <label className="block mb-2">End Date</label>
                <Calendar
                  value={endDate}
                  onChange={e => setEndDate(e.value as Date)}
                  showIcon
                  className="w-full"
                />
              </div>
            )}
          </FormLayout>
        </Card>
      </div>
    </>
  );
}
