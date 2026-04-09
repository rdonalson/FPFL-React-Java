// src/features/itemType/components/ItemTypeDeleteDialog.tsx
import { ConfirmDialog } from 'primereact/confirmdialog';

interface Props {
  visible: boolean;
  onHide: () => void;
  onConfirm: () => void;
}

export function ItemTypeDeleteDialog({ visible, onHide, onConfirm }: Props) {
  return (
    <ConfirmDialog
      visible={visible}
      onHide={onHide}
      message="Are you sure you want to delete this Item Type?"
      header="Delete Confirmation"
      icon="pi pi-exclamation-triangle"
      accept={onConfirm}
      reject={onHide}
    />
  );
}
