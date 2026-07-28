import React from 'react';
import { useNavigate } from 'react-router-dom';
import { Card } from 'primereact/card';

import NthWeekdayForm from './NthWeekdayForm';
import { useItem } from '../../../../hooks/useItem';

export default function AddNthWeekdayPage({ itemType }: { itemType: number }) {
  const navigate = useNavigate();
  const { create } = useItem();

  function handleSaved() {
    const base = itemType === 1 ? '/command/transactions/credits' : '/command/transactions/debits';
    navigate(base);
  }

  return (
    <div className="p-4">
      <Card className="w-full mb-3 px-4 sm:px-6">
        <h2 className="text-lg font-semibold">
          {itemType === 1 ? 'Add Nth-Weekday Credit' : 'Add Nth-Weekday Debit'}
        </h2>
      </Card>

      <Card className="w-full">
        <NthWeekdayForm
          itemType={itemType}
          initial={null}
          create={create}
          update={async () => {
            throw new Error('update not supported');
          }}
          onSaved={handleSaved}
        />
      </Card>
    </div>
  );
}
