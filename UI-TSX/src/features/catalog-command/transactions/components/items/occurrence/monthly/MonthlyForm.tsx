// src/features/catalog-command/transactions/components/items/occurrence/monthly/MonthlyForm.tsx
import React, { useEffect, useState } from 'react';
import { Card } from 'primereact/card';
import { Toast } from 'primereact/toast';

import type { Item } from '../../../../types/Item';
import { getSessionUserId } from '@/app/state/sessionHelpers';
import { HeaderFields } from '@/features/catalog-command/transactions/components/common/HeaderFields';
import { TimeFrameSelector } from '@/features/catalog-command/transactions/components/common/TimeFrameSelector';
import { FormLayout } from '@/features/catalog-command/transactions/components/common/FormLayout';
import { Dropdown } from 'primereact/dropdown';
import { DAY_NUMBERS } from '@/features/catalog-command/transactions/constants/days';

interface MonthlyFormProps {
  itemType: number;
  initial: Item | null;
  create: (payload: Item) => Promise<Item>;
  update: (id: number, payload: Item) => Promise<Item>;
  onSaved: () => void;
}

export default function MonthlyForm({
  itemType,
  initial,
  create,
  update,
  onSaved,
}: MonthlyFormProps) {
  const toastRef = React.useRef<Toast | null>(null);

  const [name, setName] = useState(initial?.name ?? '');
  const [amount, setAmount] = useState<number | null>(initial?.amount ?? null);
  const [monthlyDom, setMonthlyDom] = useState<number | null>(initial?.monthlyDom ?? null);

  // Date range support (consistent across forms)
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
    setMonthlyDom(initial?.monthlyDom ?? null);

    setDateRangeReq(initial?.dateRangeReq ?? false);
    setBeginDate(initial?.beginDate ? new Date(initial.beginDate) : null);
    setEndDate(initial?.endDate ? new Date(initial.endDate) : null);
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

      if (monthlyDom === null) {
        toastRef.current?.show({
          severity: 'warn',
          summary: 'Validation',
          detail: 'Please select a day of the month.',
        });
        setSaving(false);
        return;
      }

      // Date range validation when enabled
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
        fkPeriod: 6, // MONTHLY
        monthlyDom,
        weeklyDow: null,

        // Date range fields for consistency with other forms
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

            {/* Day of Month Selector */}
            <div className="col-span-1 sm:col-span-2 mt-4">
              <label className="block mb-2">Select Day of the Month</label>
              <Dropdown
                value={monthlyDom}
                options={[...DAY_NUMBERS]}
                onChange={e => setMonthlyDom(e.value)}
                placeholder="Day"
                className="responsive-half"
              />
            </div>

            {/* Time frame / Date Range selector */}
            <div className="col-span-1 sm:col-span-2 mt-4">
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
