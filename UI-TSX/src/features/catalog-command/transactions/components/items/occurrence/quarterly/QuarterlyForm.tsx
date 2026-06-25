// src/features/catalog-command/transactions/components/items/occurrence/quarterly/QuarterlyForm.tsx
import React, { useEffect, useState } from 'react';
import { Card } from 'primereact/card';
import { Toast } from 'primereact/toast';

import type { Item } from '../../../../types/Item';
import { getSessionUserId } from '@/app/state/sessionHelpers';

import { HeaderFields } from '@/features/catalog-command/transactions/components/common/HeaderFields';
import { TimeFrameSelector } from '@/features/catalog-command/transactions/components/common/TimeFrameSelector';
import { FormLayout } from '@/features/catalog-command/transactions/components/common/FormLayout';

import MonthDayRow from '../../../common/MonthDayRow';
import { parseDateOnlyString, toDateOnlyString } from '@/shared/utils/dateUtils';

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

  // ⭐ NEW: Date range support (same as Monthly/BiMonthly/etc.)
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

    setQ1Month(initial?.quarterly1Month ?? null);
    setQ1Day(initial?.quarterly1Day ?? null);

    setQ2Month(initial?.quarterly2Month ?? null);
    setQ2Day(initial?.quarterly2Day ?? null);

    setQ3Month(initial?.quarterly3Month ?? null);
    setQ3Day(initial?.quarterly3Day ?? null);

    setQ4Month(initial?.quarterly4Month ?? null);
    setQ4Day(initial?.quarterly4Day ?? null);

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

      // ⭐ NEW: Date range validation
      if (dateRangeReq) {
        if (!beginDate || !endDate) {
          toastRef.current?.show({
            severity: 'warn',
            summary: 'Validation',
            detail: 'Please select both begin and end dates for the date range.',
          });
          setSaving(false);
          return;
        }
        if (beginDate > endDate) {
          toastRef.current?.show({
            severity: 'warn',
            summary: 'Validation',
            detail: 'Begin date must be before or equal to end date.',
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
        fkPeriod: 7, // QUARTERLY

        quarterly1Month: q1Month,
        quarterly1Day: q1Day,

        quarterly2Month: q2Month,
        quarterly2Day: q2Day,

        quarterly3Month: q3Month,
        quarterly3Day: q3Day,

        quarterly4Month: q4Month,
        quarterly4Day: q4Day,

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

            {/* ⭐ NEW: Date Range Selector */}
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
