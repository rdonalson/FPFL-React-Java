// src/features/catalog-command/transactions/components/items/occurrence/semiannual/AddSemiAnnualPage.tsx
import React from 'react';
import { useNavigate } from 'react-router-dom';
import { Card } from 'primereact/card';

import SemiAnnualForm from './SemiAnnualForm';
import { useItem } from '../../../../hooks/useItem';

interface AddSemiAnnualPageProps {
  itemType: number;
}

export default function AddSemiAnnualPage({ itemType }: AddSemiAnnualPageProps) {
  const navigate = useNavigate();
  const { create } = useItem();

  function handleSaved() {
    const base = itemType === 1 ? '/command/transactions/credits' : '/command/transactions/debits';
    navigate(base);
  }

  return (
    <div className="p-4">
      {/* Title Card */}
      <Card className="w-full mb-3 px-4 sm:px-6 py-3">
        <h2 className="text-lg font-semibold">
          {itemType === 1 ? 'Add Semi-Annual Credit' : 'Add Semi-Annual Debit'}
        </h2>
      </Card>

      {/* Form Card */}
      <Card className="w-full">
        <SemiAnnualForm
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
