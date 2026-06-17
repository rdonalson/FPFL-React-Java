// src/features/catalog-command/transactions/components/items/occurrence/one-time/AddOneTimePage.tsx
import { useNavigate } from 'react-router-dom';
import { Card } from 'primereact/card';

import OneTimeForm from './OneTimeForm';
import { useItem } from '../../../../hooks/useItem';

interface AddOneTimePageProps {
  itemType: number;
}

export default function AddOneTimePage({ itemType }: AddOneTimePageProps) {
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
          {itemType === 1 ? 'Add One Time Credit' : 'Add One Time Debit'}
        </h2>
      </Card>

      <div className="mt-3">
        <OneTimeForm
          itemType={itemType}
          initial={null}
          create={create}
          update={async () => {
            throw new Error('update not supported here');
          }}
          onSaved={handleSaved}
        />
      </div>
    </div>
  );
}
