// src/features/catalog-command/transactions/components/items/occurrence/semiannual/AddSemiAnnualPage.tsx
import React from 'react';
import { useNavigate } from 'react-router-dom';
import { Card } from 'primereact/card';

import SemiAnnualForm from './SemiAnnualForm';
import { useItem } from '../../../../hooks/useItem';

export default function AddSemiAnnualPage({ itemType }: { itemType: number }) {
  const navigate = useNavigate();
  const { create } = useItem();

  function handleSaved() {
    const base = itemType === 1 ? '/command/transactions/credits' : '/command/transactions/debits';
    navigate(base);
  }

  return (
    <div className="p-4">
      <Card>
        <h2 className="text-lg font-semibold">
          {itemType === 1 ? 'Add Semi-Annual Credit' : 'Add Semi-Annual Debit'}
        </h2>
      </Card>

      <div className="mt-3">
        <SemiAnnualForm
          itemType={itemType}
          initial={null}
          create={create}
          update={async () => {
            throw new Error('update not supported');
          }}
          onSaved={handleSaved}
        />
      </div>
    </div>
  );
}
