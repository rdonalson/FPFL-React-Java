// src/features/catalog-command/transactions/components/items/occurrence/bi-monthly/BiMonthlyForm.tsx
import React, { useEffect, useState } from 'react';
import { Card } from 'primereact/card';
import { Toast } from 'primereact/toast';

import type { Item } from '../../../../types/Item';
import { getSessionUserId } from '@/app/state/sessionHelpers';
import { HeaderFields } from '@/features/catalog-command/transactions/components/common/HeaderFields';
import { TimeFrameSelector } from '@/features/catalog-command/transactions/components/common/TimeFrameSelector';
import { FormLayout } from '@/features/catalog-command/transactions/components/common/FormLayout';
import { Dropdown } from 'primereact/dropdown';

interface BiMonthlyFormProps {
  itemType: number;
  initial: Item | null;
  create: (payload: Item) => Promise<Item>;
  update: (id: number, payload: Item) => Promise<Item>;
  onSaved: () => void;
}

export default function BiMonthlyForm({
  itemType,
  initial,
  create,
  update,
  onSaved,
}: BiMonthlyFormProps) {
  const toastRef = React.useRef<Toast | null>(null);

  const [name, setName] = useState(initial?.name ?? '');
  const [amount, setAmount] = useState<number | null>(initial?.amount ?? null);
  const [biMonthlyDay1, setBiMonthlyDay1] = useState<number | null>(initial?.biMonthlyDay1 ?? null);
  const [biMonthlyDay2, setBiMonthlyDay2] = useState<number | null>(initial?.biMonthlyDay2 ?? null);

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
    setBiMonthlyDay1(initial?.biMonthlyDay1 ?? null);
    setBiMonthlyDay2(initial?.biMonthlyDay2 ?? null);

    setDateRangeReq(initial?.dateRangeReq ?? false);
    setBeginDate(initial?.beginDate ? new Date(initial.beginDate) : null);
    setEndDate(initial?.endDate ? new Date(initial.endDate) : null);
  }, [initial]);

  const dayOptions = Array.from({ length: 27 }, (_, i) => ({
    label: `${i + 1}`,
    value: i + 1,
  }));

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

      if (biMonthlyDay1 === null || biMonthlyDay2 === null) {
        toastRef.current?.show({
          severity: 'warn',
          summary: 'Validation',
          detail: 'Both bi-monthly days are required.',
        });
        setSaving(false);
        return;
      }

      if (biMonthlyDay1 === biMonthlyDay2) {
        toastRef.current?.show({
          severity: 'warn',
          summary: 'Validation',
          detail: 'Day 1 and Day 2 must be different.',
        });
        setSaving(false);
        return;
      }

      // If dateRangeReq is true, ensure begin/end are present and valid
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
        fkPeriod: 5, // BI-MONTHLY
        biMonthlyDay1,
        biMonthlyDay2,
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

            {/* Bi-Monthly Day selectors */}
            <div className="col-span-1 sm:col-span-2 mt-4">
              <div className="grid grid-cols-2 gap-3 inline-dropdown-borders">
                <div>
                  <label className="block mb-2">Day 1</label>
                  <Dropdown
                    value={biMonthlyDay1}
                    options={dayOptions}
                    onChange={e => setBiMonthlyDay1(e.value)}
                    placeholder="Select Day"
                    className="w-full"
                  />
                </div>

                <div>
                  <label className="block mb-2">Day 2</label>
                  <Dropdown
                    value={biMonthlyDay2}
                    options={dayOptions}
                    onChange={e => setBiMonthlyDay2(e.value)}
                    placeholder="Select Day"
                    className="w-full"
                  />
                </div>
              </div>
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
