import { useState, useRef } from 'react';
import { Dialog } from 'primereact/dialog';
import { Toast } from 'primereact/toast';
import { AuthClient } from '@/api/generated/AuthClient';
import { unwrap } from '@/api/utils/responseHelpers';

interface ChangePasswordDialogProps {
  visible: boolean;
  onHide: () => void;
  id: number;
}

export function ChangePasswordDialog({ visible, onHide, id }: ChangePasswordDialogProps) {
  const toast = useRef<Toast | null>(null);

  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirm, setConfirm] = useState('');

  const [error, setError] = useState('');

  const reset = () => {
    setCurrentPassword('');
    setNewPassword('');
    setConfirm('');
    setError('');
  };

  const handleSubmit = async () => {
    setError('');

    if (newPassword !== confirm) {
      setError('New passwords do not match');
      return;
    }

    try {
      const raw = await AuthClient.changePassword({
        id,
        currentPassword,
        newPassword,
      });

      unwrap(raw, null, true);

      toast.current?.show({
        severity: 'success',
        summary: 'Password Updated',
        detail: 'Your password has been changed successfully',
        life: 2500,
      });

      setTimeout(() => {
        reset();
        onHide();
      }, 800);
    } catch (err: any) {
      const message = err?.message ?? 'Unable to change password';

      toast.current?.show({
        severity: 'error',
        summary: 'Error',
        detail: message,
        life: 3000,
      });

      setError(message);
    }
  };

  return (
    <Dialog
      header="Change Password"
      visible={visible}
      onHide={() => {
        reset();
        onHide();
      }}
      modal
      style={{ width: '400px' }}
    >
      {/* Local Toast instance */}
      <Toast ref={toast} />

      <div className="flex flex-column gap-3">
        <input
          type="password"
          placeholder="Enter current password"
          value={currentPassword}
          onChange={e => setCurrentPassword(e.target.value)}
          className="p-inputtext"
        />

        <input
          type="password"
          placeholder="Enter new password"
          value={newPassword}
          onChange={e => setNewPassword(e.target.value)}
          className="p-inputtext"
        />

        <input
          type="password"
          placeholder="Confirm new password"
          value={confirm}
          onChange={e => setConfirm(e.target.value)}
          className="p-inputtext"
        />

        {error && <div className="text-red-500">{error}</div>}

        <div className="flex justify-content-end gap-2 mt-3">
          <button className="p-button p-button-text" onClick={handleSubmit}>
            Update Password
          </button>

          <button
            className="p-button p-button-text p-button-danger"
            onClick={() => {
              reset();
              onHide();
            }}
          >
            Cancel
          </button>
        </div>
      </div>
    </Dialog>
  );
}
