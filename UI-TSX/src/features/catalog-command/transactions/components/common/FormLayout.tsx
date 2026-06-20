import { ReactNode } from 'react';
import { Button } from 'primereact/button';

interface FormLayoutProps {
  children: ReactNode;
  saving?: boolean;
  onCancel: () => void;
  onSave?: () => void;
  className?: string;
}

export function FormLayout({
  children,
  saving,
  onCancel,
  onSave,
  className = '',
}: FormLayoutProps) {
  return (
    <form
      onSubmit={e => {
        e.preventDefault();
        onSave?.();
      }}
      className={`grid grid-cols-1 sm:grid-cols-2 gap-4 ${className}`}
    >
      {children}

      <div className="col-span-1 sm:col-span-2 flex justify-end gap-3 mt-4">
        <Button label="Save" icon="pi pi-check" type="submit" loading={saving} />

        <Button
          label="Cancel"
          icon="pi pi-times"
          className="p-button-secondary"
          type="button"
          onClick={onCancel}
        />
      </div>
    </form>
  );
}
