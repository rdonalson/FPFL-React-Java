import { useState, useEffect } from 'react';
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

  useEffect(() => {
    if (itemType) {
      setId(itemType.id);
      setName(itemType.name);
    }
  }, [itemType]);

  const handleSubmit = () => {
    if (!name.trim()) return;

    onUpdate({ id: id!, name });
    onHide();
  };

  return (
    <Dialog
      header="Edit Item Type"
      visible={visible}
      onHide={onHide}
      style={{ width: '30rem' }}
      modal
    >
      <div className="flex flex-column gap-3">
        {/* ID (read-only) */}
        <div>
          <label className="block mb-1">ID</label>
          <div className="p-2 border-round surface-100">{id}</div>
        </div>

        {/* Name */}
        <div>
          <label className="block mb-1">Name</label>
          <InputText value={name} onChange={e => setName(e.target.value)} className="w-full" />
        </div>

        <Button label="Update" icon="pi pi-check" onClick={handleSubmit} />
      </div>
    </Dialog>
  );
}
