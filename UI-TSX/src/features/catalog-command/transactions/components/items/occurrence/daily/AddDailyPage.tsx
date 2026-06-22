// src/features/catalog-command/transactions/components/items/occurrence/daily/AddDailyPage.tsx
import React from 'react';
import { useNavigate } from 'react-router-dom';
import { Card } from 'primereact/card';

import DailyForm from './DailyForm';
import { useItem } from '../../../../hooks/useItem';

interface AddDailyPageProps {
  itemType: number;
}

export default function AddDailyPage({ itemType }: AddDailyPageProps) {
  const navigate = useNavigate();
  const { create } = useItem();

  function handleSaved() {
    const base = itemType === 1 ? '/command/transactions/credits' : '/command/transactions/debits';
    navigate(base);
  }

  return (
    <div className="p-4">
      {/* Title Card */}
      <Card className="w-full mb-3 px-4 sm:px-6">
        <h2 className="text-lg font-semibold">
          {itemType === 1 ? 'Add Daily Credit' : 'Add Daily Debit'}
        </h2>
      </Card>

      {/* Form Card */}
      <Card className="w-full">
        <DailyForm
          itemType={itemType}
          initial={null}
          create={create}
          update={async () => {
            throw new Error('update not supported here');
          }}
          onSaved={handleSaved}
        />
      </Card>
    </div>
  );
}
