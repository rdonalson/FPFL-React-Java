// src/features/catalog-command/transactions/components/items/occurrence/annual/AnnualForm.tsx
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

  // ⭐ NEW: Date range support
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

    setAnnualMoy(initial?.annualMoy ?? null);
    setAnnualDom(initial?.annualDom ?? null);

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

      if (annualMoy === null || annualDom === null) {
        toastRef.current?.show({
          severity: 'warn',
          summary: 'Validation',
          detail: 'Annual Month and Day are required.',
        });
        setSaving(false);
        return;
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
        fkPeriod: 9, // ANNUAL

        annualMoy,
        annualDom,

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

            {/* Annual Row */}
            <MonthDayRow
              label="Annual"
              month={annualMoy}
              day={annualDom}
              setMonth={setAnnualMoy}
              setDay={setAnnualDom}
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
