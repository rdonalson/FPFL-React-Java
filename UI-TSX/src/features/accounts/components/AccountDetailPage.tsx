// src/features/accounts/components/AccountDetailPage.tsx
import { useCallback, useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { Button } from 'primereact/button';
import { Card } from 'primereact/card';
import { Checkbox } from 'primereact/checkbox';
import { Dialog } from 'primereact/dialog';
import { Dropdown } from 'primereact/dropdown';
import { InputNumber } from 'primereact/inputnumber';
import { InputText } from 'primereact/inputtext';
import { Calendar } from 'primereact/calendar';
import { Message } from 'primereact/message';

import { useToast } from '@/shared/hooks/useToast';
import { formatDate, formatMoney, formatPercent } from '@/features/debt-paydown/types/Debt';

import { Segmented } from './Segmented';
import { addPurchase, deletePurchase, getAccount, updatePurchase } from '../api/accountApi';
import {
  ONE_TIME,
  PURCHASE_PERIODS,
  WEEKDAYS,
  WEEKLY,
  describePayment,
  describePurchase,
  emptyPurchaseForm,
  kindIcon,
  kindLabel,
  toPurchaseForm,
  toPurchaseRequest,
  validatePurchaseForm,
  type Account,
  type Purchase,
  type PurchaseForm,
} from '../types/Account';

const WEEKDAY_OPTIONS = WEEKDAYS.map((label, value) => ({ label, value }));

/** One card or loan: its details and payments, and its purchases to add, change and delete. */
export default function AccountDetailPage() {
  const navigate = useNavigate();
  const toast = useToast();
  const { id } = useParams();
  const accountId = Number(id);
  const validId = Number.isInteger(accountId) && accountId > 0;

  const [account, setAccount] = useState<Account | null>(null);
  const [loading, setLoading] = useState(validId);
  const [fetchError, setFetchError] = useState<string | null>(null);
  const loadError = validId ? fetchError : 'That account does not exist.';

  const reload = useCallback(async () => setAccount(await getAccount(accountId)), [accountId]);

  useEffect(() => {
    if (!validId) return;
    let cancelled = false;
    getAccount(accountId)
      .then(a => {
        if (!cancelled) setAccount(a);
      })
      .catch((e: any) => {
        if (!cancelled) setFetchError(e?.message ?? 'Could not load the account.');
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, [accountId, validId]);

  // ---- purchase editor -------------------------------------------------------------------

  /** null = closed; 'new' = adding; a Purchase = editing it. */
  const [editing, setEditing] = useState<Purchase | 'new' | null>(null);
  const [form, setForm] = useState<PurchaseForm>(() => emptyPurchaseForm(new Date()));
  const [saving, setSaving] = useState(false);
  const [problem, setProblem] = useState<string | null>(null);

  const openNew = () => {
    setForm(emptyPurchaseForm(new Date()));
    setProblem(null);
    setEditing('new');
  };

  const openEdit = (purchase: Purchase) => {
    setForm(toPurchaseForm(purchase));
    setProblem(null);
    setEditing(purchase);
  };

  const closeEditor = () => {
    if (!saving) setEditing(null);
  };

  const patch = (changes: Partial<PurchaseForm>) => {
    setForm(f => ({ ...f, ...changes }));
    setProblem(null);
  };

  const save = async () => {
    if (!editing || !account) return;
    const issue = validatePurchaseForm(form);
    if (issue) {
      setProblem(issue);
      return;
    }

    setSaving(true);
    try {
      const request = toPurchaseRequest(form);
      if (editing === 'new') await addPurchase(account.id, request);
      else await updatePurchase(account.id, editing.id, request);
      toast?.current?.show({
        severity: 'success',
        summary: editing === 'new' ? 'Purchase added' : 'Purchase updated',
        life: 3000,
      });
      setEditing(null);
      await reload();
    } catch (e: any) {
      setProblem(e?.message ?? 'Could not save the purchase.');
    } finally {
      setSaving(false);
    }
  };

  // ---- delete ----------------------------------------------------------------------------

  const [pendingDelete, setPendingDelete] = useState<Purchase | null>(null);
  const [deleting, setDeleting] = useState(false);
  const [deleteError, setDeleteError] = useState<string | null>(null);

  const doDelete = async () => {
    if (!pendingDelete || !account) return;
    setDeleting(true);
    try {
      await deletePurchase(account.id, pendingDelete.id);
      toast?.current?.show({ severity: 'success', summary: 'Purchase deleted', life: 3000 });
      setPendingDelete(null);
      await reload();
    } catch (e: any) {
      setDeleteError(e?.message ?? 'Could not delete the purchase.');
    } finally {
      setDeleting(false);
    }
  };

  // ---- render ----------------------------------------------------------------------------

  const oneTime = form.fkPeriod === ONE_TIME;

  return (
    <div className="app-page app-page-gutter p-4 flex flex-col gap-4 debt-paydown accounts">
      <div>
        <Button
          label="Cards & Loans"
          icon="pi pi-arrow-left"
          className="p-button-text"
          onClick={() => navigate('/command/transactions/accounts')}
        />
      </div>

      {loadError && <Message severity="error" text={loadError} />}
      {loading && !account && (
        <Card>
          <p className="m-0">Loading…</p>
        </Card>
      )}

      {account && (
        <>
          <Card>
            <div className="flex flex-wrap justify-between items-start gap-3">
              <div>
                <h2 className="text-xl font-semibold m-0">
                  <i className={`${kindIcon(account.kind)} mr-2`} aria-hidden="true" />
                  {account.name}
                </h2>
                <p className="m-0 mt-1 text-color-secondary">
                  {kindLabel(account.kind)} · {formatMoney(account.openingBalance)} owed as of{' '}
                  {formatDate(account.balanceDate)} ·{' '}
                  {account.apr > 0 ? `${formatPercent(account.apr)} APR` : 'no interest'}
                  {account.creditLimit !== null
                    ? ` · ${formatMoney(account.creditLimit)} limit`
                    : ''}
                </p>
                <p className="m-0 mt-1 text-sm text-color-secondary">
                  {account.payments.length === 0
                    ? 'Nothing pays this yet; edit it on Cards & Loans.'
                    : `Paid by ${account.payments.map(describePayment).join(', ')}.`}
                </p>
              </div>
              <div className="flex gap-2">
                <Button
                  label="Add a purchase"
                  icon="pi pi-plus"
                  className="p-button-success"
                  onClick={openNew}
                />
                <Button
                  label="Forecast"
                  icon="pi pi-chart-line"
                  outlined
                  onClick={() => navigate('/query/display')}
                />
              </div>
            </div>
          </Card>

          <Card>
            <h3 className="text-lg font-semibold m-0 mb-3">Purchases</h3>
            {account.purchases.length === 0 ? (
              <p className="m-0 text-color-secondary">
                No purchases yet. Add what you put on this {kindLabel(account.kind).toLowerCase()}:
                a one-time buy, or something daily or weekly.
              </p>
            ) : (
              <ul className="accounts__purchases">
                {account.purchases.map(purchase => (
                  <li key={purchase.id} className="accounts__purchase">
                    <div className="accounts__purchase-main">
                      <strong>{purchase.name}</strong>
                      <span className="text-sm text-color-secondary">
                        {describePurchase(purchase)}
                      </span>
                    </div>
                    <span className="accounts__purchase-amount">
                      {formatMoney(purchase.amount)}
                    </span>
                    <div className="flex gap-1">
                      <Button
                        icon="pi pi-pencil"
                        rounded
                        text
                        aria-label={`Edit ${purchase.name}`}
                        onClick={() => openEdit(purchase)}
                      />
                      <Button
                        icon="pi pi-trash"
                        rounded
                        text
                        severity="danger"
                        aria-label={`Delete ${purchase.name}`}
                        onClick={() => {
                          setDeleteError(null);
                          setPendingDelete(purchase);
                        }}
                      />
                    </div>
                  </li>
                ))}
              </ul>
            )}
          </Card>
        </>
      )}

      {/* Add / edit purchase */}
      <Dialog
        header={editing === 'new' || editing === null ? 'Add a purchase' : `Edit ${editing.name}`}
        visible={editing !== null}
        onHide={closeEditor}
        modal
        draggable={false}
        style={{ width: '38rem' }}
        breakpoints={{ '640px': '95vw' }}
      >
        {editing !== null && (
          <form
            className="debt-editor"
            noValidate
            onSubmit={e => {
              e.preventDefault();
              void save();
            }}
          >
            <div className="debt-editor__grid accounts__grid">
              <div className="debt-editor__field debt-editor__field--wide">
                <label htmlFor="purchase-name">What for</label>
                <InputText
                  id="purchase-name"
                  value={form.name}
                  maxLength={150}
                  placeholder="e.g. Groceries, New laptop"
                  onChange={e => patch({ name: e.target.value })}
                  className="w-full"
                />
              </div>

              <div className="debt-editor__field">
                <label htmlFor="purchase-amount">Amount</label>
                <InputNumber
                  inputId="purchase-amount"
                  mode="currency"
                  currency="USD"
                  locale="en-US"
                  placeholder="$0.00"
                  value={form.amount}
                  onValueChange={e => patch({ amount: e.value ?? null })}
                  className="w-full"
                />
              </div>

              <div className="debt-editor__field">
                <span className="accounts__label">How often</span>
                <Segmented
                  ariaLabel="How often"
                  options={PURCHASE_PERIODS}
                  value={form.fkPeriod}
                  onChange={fkPeriod =>
                    patch({
                      fkPeriod,
                      dateRangeReq: fkPeriod === ONE_TIME ? false : form.dateRangeReq,
                    })
                  }
                />
              </div>

              {form.fkPeriod === WEEKLY && (
                <div className="debt-editor__field">
                  <label htmlFor="purchase-dow">Day of the week</label>
                  <Dropdown
                    inputId="purchase-dow"
                    options={WEEKDAY_OPTIONS}
                    optionLabel="label"
                    optionValue="value"
                    placeholder="Choose a day"
                    value={form.weeklyDow}
                    onChange={e => patch({ weeklyDow: e.value ?? null })}
                    className="w-full"
                  />
                </div>
              )}

              {oneTime ? (
                <div className="debt-editor__field">
                  <label htmlFor="purchase-date">Date</label>
                  <Calendar
                    inputId="purchase-date"
                    value={form.beginDate}
                    onChange={e => patch({ beginDate: (e.value as Date | null) ?? null })}
                    dateFormat="M d, yy"
                    showIcon
                    className="w-full"
                  />
                </div>
              ) : (
                <div className="debt-editor__field debt-editor__field--wide">
                  <div className="flex items-center gap-2">
                    <Checkbox
                      inputId="purchase-ranged"
                      checked={form.dateRangeReq}
                      onChange={e => patch({ dateRangeReq: !!e.checked })}
                    />
                    <label htmlFor="purchase-ranged">Only between two dates</label>
                  </div>
                  {form.dateRangeReq && (
                    <div className="accounts__range">
                      <div className="debt-editor__field">
                        <label htmlFor="purchase-begin">From</label>
                        <Calendar
                          inputId="purchase-begin"
                          value={form.beginDate}
                          onChange={e => patch({ beginDate: (e.value as Date | null) ?? null })}
                          dateFormat="M d, yy"
                          showIcon
                          className="w-full"
                        />
                      </div>
                      <div className="debt-editor__field">
                        <label htmlFor="purchase-end">To</label>
                        <Calendar
                          inputId="purchase-end"
                          value={form.endDate}
                          onChange={e => patch({ endDate: (e.value as Date | null) ?? null })}
                          dateFormat="M d, yy"
                          showIcon
                          className="w-full"
                        />
                      </div>
                    </div>
                  )}
                </div>
              )}
            </div>

            {problem && <Message severity="error" text={problem} className="w-full" />}

            <div className="flex justify-end gap-2">
              <Button type="button" label="Cancel" text disabled={saving} onClick={closeEditor} />
              <Button
                type="submit"
                label={editing === 'new' ? 'Add purchase' : 'Save'}
                icon="pi pi-check"
                loading={saving}
              />
            </div>
          </form>
        )}
      </Dialog>

      {/* Delete purchase */}
      <Dialog
        header="Delete purchase"
        visible={pendingDelete !== null}
        onHide={() => !deleting && setPendingDelete(null)}
        modal
        draggable={false}
        dismissableMask
        style={{ width: '26rem' }}
        breakpoints={{ '640px': '95vw' }}
      >
        {pendingDelete && (
          <div className="flex flex-col gap-3">
            {deleteError && <Message severity="error" text={deleteError} />}
            <p className="m-0">
              Delete <strong>{pendingDelete.name}</strong>? The forecast stops adding it to the
              balance.
            </p>
            <div className="flex justify-end gap-2">
              <Button
                label="Cancel"
                text
                disabled={deleting}
                onClick={() => setPendingDelete(null)}
              />
              <Button
                label="Delete"
                icon="pi pi-trash"
                severity="danger"
                loading={deleting}
                onClick={() => void doDelete()}
              />
            </div>
          </div>
        )}
      </Dialog>
    </div>
  );
}
