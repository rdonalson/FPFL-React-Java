// src/features/itemType/components/ItemTypeForm.tsx
import { useState } from 'react';
import { InputText } from 'primereact/inputtext';
import { Button } from 'primereact/button';
import { Card } from 'primereact/card';
import { ItemType } from '../types/ItemType';

interface Props {
  initialValue?: ItemType;
  onSubmit: (value: ItemType) => void;
  submitLabel: string;
}

export function ItemTypeForm({ initialValue, onSubmit, submitLabel }: Props) {
  const [form, setForm] = useState<ItemType>(initialValue ?? { id: 0, name: '' });

  const update = (field: keyof ItemType, value: string | number) =>
    setForm(prev => ({ ...prev, [field]: value }));

  return (
    <Card className="p-3">
      <div className="flex flex-column gap-3">
        <div>
          <label className="block mb-1">ID</label>
          <InputText
            value={form.id.toString()}
            keyfilter="int"
            onChange={e => {
              const num = Number(e.target.value);
              update('id', isNaN(num) ? 0 : num); // <-- safer numeric handling
            }}
            className="w-full"
          />
        </div>

        <div>
          <label className="block mb-1">Name</label>
          <InputText
            value={form.name}
            onChange={e => update('name', e.target.value)}
            className="w-full"
          />
        </div>

        <Button label={submitLabel} icon="pi pi-check" onClick={() => onSubmit(form)} />
      </div>
    </Card>
  );
}
