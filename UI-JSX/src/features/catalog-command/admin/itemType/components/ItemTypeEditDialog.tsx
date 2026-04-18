import { useState } from 'react';
import { Dialog } from 'primereact/dialog';
import { InputText } from 'primereact/inputtext';
import { Button } from 'primereact/button';
import type { ItemType } from '../types/ItemType';

interface Props {
  visible: boolean;
  onHide: () => void;
  itemType: ItemType | null;
  onUpdate: (value: ItemType) => void;
}

export function ItemTypeEditDialog({ visible, onHide, itemType, onUpdate }: Props) {
  const [id, setId] = useState<number | null>(null);
  const [name, setName] = useState('');

  // Initialize form state when dialog becomes visible
  if (visible && itemType && id === null) {
    setId(itemType.id);
    setName(itemType.name);
  }

  const reset = () => {
    setId(null);
    setName('');
  };

  const handleSubmit = () => {
    if (!name.trim() || !itemType) return;

    onUpdate({ id: itemType.id, name });
    onHide();
    reset();
  };

  return (
    <Dialog
      header="Edit Item Type"
      visible={visible}
      onHide={() => {
        onHide();
        reset();
      }}
      style={{ width: '30rem' }}
      modal
    >
      <div className="flex flex-column gap-3">
        <div>
          <label className="block mb-1">ID</label>
          <div className="p-2 border-round surface-100">{id}</div>
        </div>

        <div>
          <label className="block mb-1">Name</label>
          <InputText value={name} onChange={e => setName(e.target.value)} className="w-full" />
        </div>

        <Button label="Update" icon="pi pi-check" onClick={handleSubmit} />
      </div>
    </Dialog>
  );
}
