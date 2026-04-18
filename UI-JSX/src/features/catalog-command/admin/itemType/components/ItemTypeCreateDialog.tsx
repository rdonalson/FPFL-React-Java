import { useState } from 'react';
import { Dialog } from 'primereact/dialog';
import { InputText } from 'primereact/inputtext';
import { InputNumber } from 'primereact/inputnumber';
import { Button } from 'primereact/button';
import type { ItemType } from '../types/ItemType';

interface Props {
  visible: boolean;
  onHide: () => void;
  onCreate: (value: ItemType) => void;
}

export function ItemTypeCreateDialog({ visible, onHide, onCreate }: Props) {
  const [id, setId] = useState<number | null>(null);
  const [name, setName] = useState('');

  const handleSubmit = () => {
    if (!id || id <= 0) return; // validation
    if (!name.trim()) return;

    onCreate({ id, name });
    setId(null);
    setName('');
    onHide();
  };

  return (
    <Dialog
      header="Create Item Type"
      visible={visible}
      onHide={onHide}
      style={{ width: '30rem' }}
      modal
    >
      <div className="flex flex-column gap-3">
        <div>
          <label className="block mb-1">ID</label>
          <InputNumber
            value={id}
            onValueChange={e => setId(e.value ?? null)}
            min={1}
            placeholder="Enter positive integer"
            className="w-full"
          />
        </div>

        <div>
          <label className="block mb-1">Name</label>
          <InputText value={name} onChange={e => setName(e.target.value)} className="w-full" />
        </div>

        <Button label="Create" icon="pi pi-check" onClick={handleSubmit} />
      </div>
    </Dialog>
  );
}
