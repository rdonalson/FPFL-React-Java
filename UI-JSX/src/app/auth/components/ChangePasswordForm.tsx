// src/app/auth/components/ChangePasswordForm.tsx
import { useRef, useState } from 'react';
import { Toast } from 'primereact/toast';
import { InputText } from 'primereact/inputtext';
import { Button } from 'primereact/button';
import { AuthClient } from '@/api/generated/AuthClient';
import { unwrap } from '@/api/utils/responseHelpers';

interface ChangePasswordFormProps {
  id: number;
  onSuccess: () => void;
}

export function ChangePasswordForm({ id, onSuccess }: ChangePasswordFormProps) {
  const toast = useRef<Toast | null>(null);

  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirm, setConfirm] = useState('');

  const [showCurrent, setShowCurrent] = useState(false);
  const [showNew, setShowNew] = useState(false);
  const [showConfirm, setShowConfirm] = useState(false);

  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const reset = () => {
    setCurrentPassword('');
    setNewPassword('');
    setConfirm('');
    setShowCurrent(false);
    setShowNew(false);
    setShowConfirm(false);
    setError('');
  };

  const handleSubmit = async (e?: React.FormEvent) => {
    e?.preventDefault();
    setError('');

    if (newPassword !== confirm) {
      setError('New passwords do not match');
      return;
    }

    setSubmitting(true);

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
        onSuccess();
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
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div style={{ width: '100%' }}>
      <Toast ref={toast} />

      <form className="auth-form-grid" onSubmit={handleSubmit} style={{ width: '100%' }}>
        {/* Current Password */}
        <label htmlFor="current-password" className="form-label">
          Current Password
        </label>
        <div className="form-field" style={{ display: 'flex', gap: 8 }}>
          <InputText
            id="current-password"
            type={showCurrent ? 'text' : 'password'}
            value={currentPassword}
            onChange={e => setCurrentPassword((e.target as HTMLInputElement).value)}
            placeholder="Enter current password"
            autoComplete="current-password"
            style={{ flex: 1 }}
          />
          <Button
            type="button"
            icon={showCurrent ? 'pi pi-eye-slash' : 'pi pi-eye'}
            className="p-button-text"
            onClick={() => setShowCurrent(s => !s)}
          />
        </div>

        {/* New Password */}
        <label htmlFor="new-password" className="form-label">
          New Password
        </label>
        <div className="form-field" style={{ display: 'flex', gap: 8 }}>
          <InputText
            id="new-password"
            type={showNew ? 'text' : 'password'}
            value={newPassword}
            onChange={e => setNewPassword((e.target as HTMLInputElement).value)}
            placeholder="Enter new password"
            autoComplete="new-password"
            style={{ flex: 1 }}
          />
          <Button
            type="button"
            icon={showNew ? 'pi pi-eye-slash' : 'pi pi-eye'}
            className="p-button-text"
            onClick={() => setShowNew(s => !s)}
          />
        </div>

        {/* Confirm Password */}
        <label htmlFor="confirm-password" className="form-label">
          Confirm Password
        </label>
        <div className="form-field" style={{ display: 'flex', gap: 8 }}>
          <InputText
            id="confirm-password"
            type={showConfirm ? 'text' : 'password'}
            value={confirm}
            onChange={e => setConfirm((e.target as HTMLInputElement).value)}
            placeholder="Confirm new password"
            autoComplete="new-password"
            style={{ flex: 1 }}
          />
          <Button
            type="button"
            icon={showConfirm ? 'pi pi-eye-slash' : 'pi pi-eye'}
            className="p-button-text"
            onClick={() => setShowConfirm(s => !s)}
          />
        </div>

        {/* Error */}
        {error && (
          <div className="p-error" style={{ gridColumn: '1 / -1', marginTop: 8 }}>
            {error}
          </div>
        )}

        {/* Actions */}
        <div
          className="form-actions"
          style={{
            gridColumn: '1 / -1',
            display: 'flex',
            justifyContent: 'flex-end',
            gap: 8,
            marginTop: 16,
          }}
        >
          <Button
            type="submit"
            label={submitting ? 'Updating...' : 'Update Password'}
            icon="pi pi-check"
            disabled={submitting}
          />
          <Button
            type="button"
            label="Cancel"
            icon="pi pi-times"
            className="p-button-secondary"
            onClick={() => {
              reset();
              onSuccess();
            }}
            disabled={submitting}
          />
        </div>
      </form>
    </div>
  );
}
