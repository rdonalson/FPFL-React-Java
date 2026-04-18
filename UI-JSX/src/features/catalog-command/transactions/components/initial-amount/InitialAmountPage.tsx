// transactions/InitialAmountPage.tsx
import React, { JSX, useEffect, useRef, useState } from 'react';
import { Toast } from 'primereact/toast';
import { InputNumber } from 'primereact/inputnumber';
import { Button } from 'primereact/button';
import {
  fetchInitialAmountForCurrentUser,
  createInitialAmountForCurrentUser,
  updateInitialAmountForCurrentUser,
} from '../../api/itemApi';
import { Item } from '../../types/Item';

/**
 * InitialAmountPage (PrimeReact)
 * - Shows only the Amount field (currency) to the logged-in user.
 * - Uses PrimeReact InputNumber (mode="currency") for formatting and input.
 * - Uses transactions/itemApi helpers for GET/POST/PUT.
 */

export default function InitialAmountPage(): JSX.Element {
  const [initialAmount, setInitialAmount] = useState<Item | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [saving, setSaving] = useState<boolean>(false);
  const [editing, setEditing] = useState<boolean>(false);
  const [amount, setAmount] = useState<number | null>(0);
  const toast = useRef<Toast | null>(null);

  useEffect(() => {
    load();
  }, []);

  async function load() {
    setLoading(true);
    try {
      const existing = await fetchInitialAmountForCurrentUser();
      setInitialAmount(existing);
      setAmount(existing?.amount ?? 0);
      setEditing(false);
    } catch (err: any) {
      toast.current?.show({
        severity: 'error',
        summary: 'Load failed',
        detail: err?.message ?? String(err),
        life: 5000,
      });
    } finally {
      setLoading(false);
    }
  }

  async function handleCreate() {
    setSaving(true);
    try {
      const n = amount ?? 0;
      if (!Number.isFinite(n) || n < 0) {
        throw new Error('Amount must be a number greater than or equal to 0.');
      }
      const created = await createInitialAmountForCurrentUser(n);
      setInitialAmount(created);
      setAmount(created.amount ?? n);
      toast.current?.show({
        severity: 'success',
        summary: 'Created',
        detail: 'Initial Amount created',
        life: 3000,
      });
      setEditing(false);
    } catch (err: any) {
      toast.current?.show({
        severity: 'error',
        summary: 'Create failed',
        detail: err?.message ?? String(err),
        life: 5000,
      });
    } finally {
      setSaving(false);
    }
  }

  function startEdit() {
    if (!initialAmount) return;
    setAmount(initialAmount.amount ?? 0);
    setEditing(true);
  }

  async function handleSaveEdit() {
    if (!initialAmount?.id) return;
    setSaving(true);
    try {
      const n = amount ?? 0;
      if (!Number.isFinite(n) || n < 0) {
        throw new Error('Amount must be a number greater than or equal to 0.');
      }
      const updated = await updateInitialAmountForCurrentUser(initialAmount.id, n);
      setInitialAmount(updated);
      setAmount(updated.amount ?? n);
      setEditing(false);
      toast.current?.show({
        severity: 'success',
        summary: 'Saved',
        detail: 'Initial Amount updated',
        life: 3000,
      });
    } catch (err: any) {
      toast.current?.show({
        severity: 'error',
        summary: 'Update failed',
        detail: err?.message ?? String(err),
        life: 5000,
      });
    } finally {
      setSaving(false);
    }
  }

  function cancelEdit() {
    setAmount(initialAmount?.amount ?? 0);
    setEditing(false);
  }

  return (
    <div style={{ padding: 16, maxWidth: 560 }}>
      <Toast ref={toast} />
      <h3>Initial Amount</h3>

      {loading ? (
        <div>Loading...</div>
      ) : (
        <>
          {initialAmount ? (
            <section>
              <div style={{ display: 'grid', gap: 12, maxWidth: 420 }}>
                <label style={{ display: 'flex', flexDirection: 'column', gap: 6 }}>
                  <strong>Amount</strong>
                  <InputNumber
                    value={amount}
                    onValueChange={e => setAmount(e.value as number)}
                    mode="currency"
                    currency="USD"
                    locale={navigator.language}
                    min={0}
                    disabled={!editing || saving}
                    inputId="initial-amount-input"
                    style={{ width: '100%' }}
                  />
                </label>

                <div>
                  {editing ? (
                    <>
                      <Button
                        label={saving ? 'Saving...' : 'Save'}
                        icon="pi pi-check"
                        onClick={handleSaveEdit}
                        disabled={saving}
                      />
                      <Button
                        label="Cancel"
                        icon="pi pi-times"
                        className="p-button-secondary"
                        onClick={cancelEdit}
                        style={{ marginLeft: 8 }}
                        disabled={saving}
                      />
                    </>
                  ) : (
                    <>
                      <Button label="Edit" icon="pi pi-pencil" onClick={startEdit} />
                      <Button
                        label="Reload"
                        icon="pi pi-refresh"
                        className="p-button-secondary"
                        onClick={load}
                        style={{ marginLeft: 8 }}
                      />
                    </>
                  )}
                </div>

                <div style={{ marginTop: 8 }}>
                  <em>Only one Initial Amount is allowed. Editing updates that single item.</em>
                </div>
              </div>
            </section>
          ) : (
            <section>
              <h4>Create Initial Amount</h4>

              <div style={{ display: 'grid', gap: 12, maxWidth: 420 }}>
                <label style={{ display: 'flex', flexDirection: 'column', gap: 6 }}>
                  <strong>Amount</strong>
                  <InputNumber
                    value={amount}
                    onValueChange={e => setAmount(e.value as number)}
                    mode="currency"
                    currency="USD"
                    locale={navigator.language}
                    min={0}
                    inputId="initial-amount-create"
                    style={{ width: '100%' }}
                  />
                </label>

                {/* <div>
                  <small>
                    <strong>User</strong>{' '}
                    {sessionStorage.getItem('userId') ?? '00000000-0000-0000-0000-000000000000'}
                  </small>
                </div> */}

                <div>
                  <Button
                    label={saving ? 'Creating...' : 'Create Initial Amount'}
                    icon="pi pi-plus"
                    onClick={handleCreate}
                    disabled={saving}
                  />
                  <Button
                    label="Reload"
                    icon="pi pi-refresh"
                    className="p-button-secondary"
                    onClick={load}
                    style={{ marginLeft: 8 }}
                    disabled={saving}
                  />
                </div>
              </div>
            </section>
          )}
        </>
      )}
    </div>
  );
}
