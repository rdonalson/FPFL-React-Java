import React, { useState } from 'react';
import { Card } from 'primereact/card';
import { Toast } from 'primereact/toast';

import type { Item } from '../../../../types/Item';
import { getSessionUserId } from '@/app/state/sessionHelpers';

import { WeekdayRadioGroup } from '@/features/catalog-command/transactions/components/common/WeekdayRadioGroup';
import { TimeFrameSelector } from '@/features/catalog-command/transactions/components/common/TimeFrameSelector';
import { HeaderFields } from '@/features/catalog-command/transactions/components/common/HeaderFields';
import { FormLayout } from '@/features/catalog-command/transactions/components/common/FormLayout';

import { parseDateOnlyString, toDateOnlyString } from '@/shared/utils/dateUtils';

interface NthWeekdayFormProps {
  itemType: number;
  initial: Item | null;
  create: (payload: Item) => Promise<Item>;
  update: (id: number, payload: Item) => Promise<Item>;
  onSaved: () => void;
}

export default function NthWeekdayForm({
  itemType,
  initial,
  create,
  update,
  onSaved,
}: NthWeekdayFormProps) {
  const toastRef = React.useRef<Toast | null>(null);

  // ---------- INITIAL STATE (no useEffect needed) ----------
  const [name, setName] = useState(initial?.name ?? '');
  const [amount, setAmount] = useState<number | null>(initial?.amount ?? null);

  const [nthDow, setNthDow] = useState<number | null>(initial?.nthDow ?? null);
  const [nthIndex, setNthIndex] = useState<number | null>(initial?.nthIndex ?? null);

  const [dateRangeReq, setDateRangeReq] = useState(initial?.dateRangeReq ?? false);
  const [beginDate, setBeginDate] = useState<Date | null>(
    initial?.beginDate ? parseDateOnlyString(initial.beginDate) : null,
  );
  const [endDate, setEndDate] = useState<Date | null>(
    initial?.endDate ? parseDateOnlyString(initial.endDate) : null,
  );

  const [saving, setSaving] = useState(false);

  // ---------- SAVE HANDLER ----------
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

      if (nthDow === null) {
        toastRef.current?.show({
          severity: 'warn',
          summary: 'Validation',
          detail: 'Please select a weekday.',
        });
        setSaving(false);
        return;
      }

      if (nthIndex === null) {
        toastRef.current?.show({
          severity: 'warn',
          summary: 'Validation',
          detail: 'Please select the week index.',
        });
        setSaving(false);
        return;
      }

      // ---------- PAYLOAD ----------
      const payload: Item = {
        id: initial?.id,
        userId,
        name: name.trim(),
        amount,
        fkItemType: itemType,
        fkPeriod: 10, // NTH-WEEKDAY
        nthDow,
        nthIndex,

        dateRangeReq,
        beginDate: dateRangeReq ? toDateOnlyString(beginDate) : null,
        endDate: dateRangeReq ? toDateOnlyString(endDate) : null,
      };

      // ---------- CREATE OR UPDATE ----------
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

  // ---------- RENDER ----------
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
              <WeekdayRadioGroup value={nthDow} onChange={setNthDow} />
            </div>

            {/* Week Index Selector */}
            <div className="col-span-1 sm:col-span-2">
              <label className="block mb-2">Select Week of Month</label>
              <select
                className="p-inputtext w-full"
                value={nthIndex ?? ''}
                onChange={e => setNthIndex(Number(e.target.value))}
              >
                <option value="">-- Select --</option>
                <option value={1}>1st</option>
                <option value={2}>2nd</option>
                <option value={3}>3rd</option>
                <option value={4}>4th</option>
                <option value={-1}>Last</option>
              </select>
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
