// src/features/timePeriod/components/TimePeriodDeleteDialog.tsx

import { ConfirmDialog } from 'primereact/confirmdialog';

interface Props {
  visible: boolean;
  onHide: () => void;
  onConfirm: () => void;
}

export function TimePeriodDeleteDialog({ visible, onHide, onConfirm }: Props) {
  return (
    <ConfirmDialog
      visible={visible}
      onHide={onHide}
      message="Are you sure you want to delete this Time Period?"
      header="Delete Confirmation"
      icon="pi pi-exclamation-triangle"
      accept={onConfirm}
      reject={onHide}
    />
  );
}
