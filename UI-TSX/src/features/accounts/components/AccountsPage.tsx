// src/features/accounts/components/AccountsPage.tsx
import { useEffect, useMemo, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Button } from 'primereact/button';
import { Card } from 'primereact/card';
import { Dialog } from 'primereact/dialog';
import { Dropdown } from 'primereact/dropdown';
import { InputNumber } from 'primereact/inputnumber';
import { InputText } from 'primereact/inputtext';
import { Calendar } from 'primereact/calendar';
import { Message } from 'primereact/message';

import { getSessionUserId } from '@/app/state/sessionHelpers';
import { useToast } from '@/shared/hooks/useToast';
import { getItemsForUserAndType } from '@/features/catalog-command/transactions/api/itemApi';
import type { Item } from '@/features/catalog-command/transactions/types/Item';
import { formatDate, formatMoney, formatPercent } from '@/features/debt-paydown/types/Debt';

import { createAccount, deleteAccount, listAccounts, updateAccount } from '../api/accountApi';
import { Segmented } from './Segmented';
import {
  CREDIT,
  DEBIT,
  KIND_OPTIONS,
  MAX_PAYMENTS,
  emptyAccountForm,
  kindIcon,
  kindLabel,
  newPayment,
  paymentShare,
  takenElsewhere,
  toAccountForm,
  toAccountRequest,
  validateAccountForm,
  type Account,
  type AccountForm,
  type PaymentForm,
  type PaymentMode,
} from '../types/Account';

const MODE_OPTIONS: { label: string; value: PaymentMode }[] = [
  { label: '%', value: 'percent' },
  { label: '$', value: 'amount' },
];

/**
 * Cards & Loans: each credit card or loan is its own account with its own purchases, paid down by
 * the user's credits (a fixed amount or a percentage of each occurrence) and debits (their whole
 * amount). The forecast (Display) deducts what credits send and charts every balance.
 */
export default function AccountsPage() {
  const navigate = useNavigate();
  const toast = useToast();

  const [accounts, setAccounts] = useState<Account[]>([]);
  const [credits, setCredits] = useState<Item[]>([]);
  const [debits, setDebits] = useState<Item[]>([]);
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState<string | null>(null);

  // Load the accounts plus the user's credits and debits once; state is only set after the awaits
  useEffect(() => {
    let cancelled = false;
    const userId = getSessionUserId();

    const loadAll = async () => {
      if (!userId) throw new Error('No user session found.');
      return Promise.all([
        listAccounts(),
        getItemsForUserAndType(userId, CREDIT),
        getItemsForUserAndType(userId, DEBIT),
      ]);
    };

    loadAll()
      .then(([a, creditItems, debitItems]) => {
        if (cancelled) return;
        setAccounts(a);
        setCredits(creditItems);
        setDebits(debitItems);
      })
      .catch((e: any) => {
        if (!cancelled) setLoadError(e?.message ?? 'Could not load your cards and loans.');
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });

    return () => {
      cancelled = true;
    };
  }, []);

  const itemsById = useMemo(
    () => new Map<number, Item>([...credits, ...debits].map(i => [i.id!, i])),
    [credits, debits],
  );

  /** Credits first, then debits, each labelled so the dropdown shows which is which. */
  const sourceOptions = useMemo(
    () => [
      ...credits.map(i => ({ label: `${i.name} (credit)`, value: i.id! })),
      ...debits.map(i => ({ label: `${i.name} (debit)`, value: i.id! })),
    ],
    [credits, debits],
  );

  const schedule = (id: number | null) =>
    id === null ? '' : (itemsById.get(id)?.TimePeriod?.Name ?? '');

  // ---- editor ----------------------------------------------------------------------------

  /** null = closed; 'new' = adding; an Account = editing it. */
  const [editing, setEditing] = useState<Account | 'new' | null>(null);
  const [form, setForm] = useState<AccountForm>(() => emptyAccountForm(new Date()));
  const [saving, setSaving] = useState(false);
  const [problem, setProblem] = useState<string | null>(null);

  const editingId = editing && editing !== 'new' ? editing.id : null;
  const elsewhere = useMemo(() => takenElsewhere(accounts, editingId), [accounts, editingId]);

  const openNew = () => {
    setForm(emptyAccountForm(new Date()));
    setProblem(null);
    setEditing('new');
  };

  const openEdit = (account: Account) => {
    setForm(toAccountForm(account));
    setProblem(null);
    setEditing(account);
  };

  const closeEditor = () => {
    if (!saving) setEditing(null);
  };

  const patch = (changes: Partial<AccountForm>) => {
    setForm(f => ({ ...f, ...changes }));
    setProblem(null);
  };

  const patchPayment = (key: number, changes: Partial<PaymentForm>) => {
    setForm(f => ({
      ...f,
      payments: f.payments.map(p => (p.key === key ? { ...p, ...changes } : p)),
    }));
    setProblem(null);
  };

  /** Picking a debit makes the row pay its whole amount; picking a credit goes back to a share. */
  const chooseSource = (payment: PaymentForm, itemId: number | null) => {
    const isDebit = itemId !== null && itemsById.get(itemId)?.ItemType?.Id === DEBIT;
    patchPayment(payment.key, {
      itemId,
      mode: isDebit ? 'whole' : payment.mode === 'whole' ? 'percent' : payment.mode,
    });
  };

  const addPayment = () => setForm(f => ({ ...f, payments: [...f.payments, newPayment()] }));

  const removePayment = (key: number) => {
    setForm(f => ({ ...f, payments: f.payments.filter(p => p.key !== key) }));
    setProblem(null);
  };

  const save = async () => {
    if (!editing) return;
    const issue = validateAccountForm(form, accounts, editingId, id => itemsById.get(id));
    if (issue) {
      setProblem(issue);
      return;
    }

    setSaving(true);
    try {
      const request = toAccountRequest(form);
      if (editing === 'new') await createAccount(request);
      else await updateAccount(editing.id, request);
      toast?.current?.show({
        severity: 'success',
        summary: editing === 'new' ? 'Account added' : 'Account updated',
        life: 3000,
      });
      setEditing(null);
      setAccounts(await listAccounts());
    } catch (e: any) {
      setProblem(e?.message ?? 'Could not save the account.');
    } finally {
      setSaving(false);
    }
  };

  // ---- delete ----------------------------------------------------------------------------

  const [pendingDelete, setPendingDelete] = useState<Account | null>(null);
  const [deleting, setDeleting] = useState(false);
  const [deleteError, setDeleteError] = useState<string | null>(null);

  const confirmDelete = (account: Account) => {
    setDeleteError(null);
    setPendingDelete(account);
  };

  const doDelete = async () => {
    if (!pendingDelete) return;
    setDeleting(true);
    try {
      await deleteAccount(pendingDelete.id);
      toast?.current?.show({ severity: 'success', summary: 'Account deleted', life: 3000 });
      setPendingDelete(null);
      setAccounts(await listAccounts());
    } catch (e: any) {
      setDeleteError(e?.message ?? 'Could not delete the account.');
    } finally {
      setDeleting(false);
    }
  };

  // ---- render ----------------------------------------------------------------------------

  return (
    <div className="app-page app-page-gutter p-4 flex flex-col gap-4 debt-paydown accounts">
      <div>
        <Button
          label="Back to Home"
          icon="pi pi-arrow-left"
          className="p-button-text"
          onClick={() => navigate('/')}
        />
      </div>

      <Card>
        <div className="flex flex-wrap justify-between items-center gap-3">
          <div>
            <h2 className="text-xl font-semibold m-0">
              <i className="pi pi-wallet mr-2" aria-hidden="true" />
              Cards &amp; Loans
            </h2>
            <p className="m-0 mt-1 text-color-secondary">
              Keep each credit card or loan as its own account. Purchases go on the card; your
              credits and debits pay it down, and the forecast shows the payoff.
            </p>
          </div>
          <div className="flex gap-2">
            <Button
              label="Add a card or loan"
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

      {loadError && <Message severity="error" text={loadError} />}

      {accounts.length > 0 ? (
        <div className="debt-paydown__list">
          {accounts.map(account => (
            <Card key={account.id} className="debt-card" aria-label={account.name}>
              <div className="flex justify-between items-start gap-2">
                <div>
                  <h3 className="text-lg font-semibold m-0">
                    <i className={`${kindIcon(account.kind)} mr-2`} aria-hidden="true" />
                    {account.name}
                  </h3>
                  <p className="m-0 text-sm text-color-secondary">
                    {kindLabel(account.kind)}
                    {account.creditLimit !== null
                      ? ` · ${formatMoney(account.creditLimit)} limit`
                      : ''}
                  </p>
                </div>
                <div className="flex gap-1">
                  <Button
                    icon="pi pi-shopping-cart"
                    rounded
                    text
                    aria-label={`Purchases on ${account.name}`}
                    onClick={() => navigate(`/command/transactions/accounts/${account.id}`)}
                  />
                  <Button
                    icon="pi pi-pencil"
                    rounded
                    text
                    aria-label={`Edit ${account.name}`}
                    onClick={() => openEdit(account)}
                  />
                  <Button
                    icon="pi pi-trash"
                    rounded
                    text
                    severity="danger"
                    aria-label={`Delete ${account.name}`}
                    onClick={() => confirmDelete(account)}
                  />
                </div>
              </div>

              <div className="debt-card__owed">
                <span className="debt-card__label">Owed</span>
                <span className="debt-card__balance">{formatMoney(account.openingBalance)}</span>
                <span className="text-sm text-color-secondary">
                  as of {formatDate(account.balanceDate)} ·{' '}
                  {account.apr > 0 ? `${formatPercent(account.apr)} APR` : 'no interest'}
                </span>
              </div>

              <span className="debt-card__label">Paid by</span>
              {account.payments.length === 0 ? (
                <p className="m-0 text-sm text-color-secondary">
                  Nothing pays this yet. Edit it to choose credits or debits that do.
                </p>
              ) : (
                <ul className="debt-card__allocations">
                  {account.payments.map(p => (
                    <li key={p.id}>
                      <span className="debt-card__percent">
                        {p.itemTypeId === DEBIT
                          ? 'All'
                          : p.amount !== null
                            ? formatMoney(p.amount)
                            : formatPercent(p.percent ?? 0)}
                      </span>
                      <span>
                        {p.itemTypeId === DEBIT ? 'of ' : p.amount !== null ? 'from ' : 'of '}
                        <strong>{p.itemName}</strong>
                        {p.itemPeriod && (
                          <span className="text-sm text-color-secondary"> · {p.itemPeriod}</span>
                        )}
                      </span>
                      <span className="debt-card__share">{formatMoney(p.perOccurrence)} each</span>
                    </li>
                  ))}
                </ul>
              )}

              <Link
                to={`/command/transactions/accounts/${account.id}`}
                className="text-sm accounts__purchases-link"
              >
                {account.purchases.length === 0
                  ? 'No purchases yet. Add some'
                  : `${account.purchases.length} purchase${account.purchases.length === 1 ? '' : 's'}`}
                <i className="pi pi-angle-right ml-1" aria-hidden="true" />
              </Link>
            </Card>
          ))}
        </div>
      ) : loading ? (
        <Card>
          <p className="m-0">Loading…</p>
        </Card>
      ) : !loadError ? (
        <Card>
          <div className="flex flex-col items-center gap-3 text-center py-4">
            <i className="pi pi-wallet text-4xl text-color-secondary" aria-hidden="true" />
            <p className="m-0">
              Add a credit card or loan with what you owe on it. Then add its purchases and choose
              which of your credits and debits pay it.
            </p>
            <Button label="Add a card or loan" icon="pi pi-plus" onClick={openNew} />
          </div>
        </Card>
      ) : null}

      {/* Add / edit */}
      <Dialog
        header={
          editing === 'new' || editing === null ? 'Add a card or loan' : `Edit ${editing.name}`
        }
        visible={editing !== null}
        onHide={closeEditor}
        modal
        draggable={false}
        style={{ width: '44rem' }}
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
                <label htmlFor="account-name">Name</label>
                <InputText
                  id="account-name"
                  value={form.name}
                  maxLength={150}
                  placeholder="e.g. Chase Visa, Car loan"
                  onChange={e => patch({ name: e.target.value })}
                  className="w-full"
                />
              </div>

              <div className="debt-editor__field debt-editor__field--wide">
                <span className="accounts__label">Kind</span>
                <Segmented
                  ariaLabel="Kind"
                  options={KIND_OPTIONS}
                  value={form.kind}
                  onChange={kind =>
                    patch({ kind, creditLimit: kind === 'loan' ? null : form.creditLimit })
                  }
                />
              </div>

              <div className="debt-editor__field">
                <label htmlFor="account-balance">Balance owed</label>
                <InputNumber
                  inputId="account-balance"
                  mode="currency"
                  currency="USD"
                  locale="en-US"
                  placeholder="$0.00"
                  value={form.openingBalance}
                  onValueChange={e => patch({ openingBalance: e.value ?? null })}
                  className="w-full"
                />
              </div>

              <div className="debt-editor__field">
                <label htmlFor="account-date">As of</label>
                <Calendar
                  inputId="account-date"
                  value={form.balanceDate}
                  onChange={e => patch({ balanceDate: (e.value as Date | null) ?? null })}
                  dateFormat="M d, yy"
                  showIcon
                  className="w-full"
                />
              </div>

              <div className="debt-editor__field">
                <label htmlFor="account-apr">APR</label>
                <InputNumber
                  inputId="account-apr"
                  suffix="%"
                  minFractionDigits={0}
                  maxFractionDigits={3}
                  min={0}
                  max={100}
                  placeholder="0%"
                  value={form.apr}
                  onValueChange={e => patch({ apr: e.value ?? null })}
                  className="w-full"
                />
                <small className="text-color-secondary">Charged monthly. Leave 0 for none.</small>
              </div>

              {form.kind === 'creditCard' && (
                <div className="debt-editor__field">
                  <label htmlFor="account-limit">Credit limit (optional)</label>
                  <InputNumber
                    inputId="account-limit"
                    mode="currency"
                    currency="USD"
                    locale="en-US"
                    placeholder="None"
                    value={form.creditLimit}
                    onValueChange={e => patch({ creditLimit: e.value ?? null })}
                    className="w-full"
                  />
                </div>
              )}
            </div>

            <fieldset className="debt-editor__allocations">
              <legend>Paid by</legend>
              <p className="m-0 text-sm text-color-secondary">
                Each time a credit comes in, a fixed amount or a share of it goes here. A debit
                (such as the card&apos;s monthly payment) pays its whole amount.
              </p>

              {form.payments.length === 0 && (
                <p className="m-0 text-sm">None yet: only purchases will change the balance.</p>
              )}

              {form.payments.map((p, i) => {
                const source = p.itemId === null ? undefined : itemsById.get(p.itemId);
                const taken = p.itemId === null ? 0 : (elsewhere.get(p.itemId) ?? 0);
                const share = paymentShare(p, source);
                return (
                  <div key={p.key} className="debt-editor__allocation accounts__payment">
                    <Dropdown
                      inputId={`payment-item-${i}`}
                      options={sourceOptions}
                      optionLabel="label"
                      optionValue="value"
                      placeholder="Choose a credit or debit"
                      value={p.itemId}
                      onChange={e => chooseSource(p, e.value ?? null)}
                      aria-label={`Item for payment ${i + 1}`}
                      className="w-full"
                    />
                    {p.mode === 'whole' ? (
                      <span className="text-sm accounts__whole">Whole amount</span>
                    ) : (
                      <div className="accounts__share">
                        <Segmented
                          size="small"
                          ariaLabel={`Percentage or amount for payment ${i + 1}`}
                          options={MODE_OPTIONS}
                          value={p.mode}
                          onChange={mode => patchPayment(p.key, { mode })}
                        />
                        {p.mode === 'percent' ? (
                          <InputNumber
                            inputId={`payment-percent-${i}`}
                            suffix="%"
                            minFractionDigits={0}
                            maxFractionDigits={2}
                            min={0}
                            max={100}
                            placeholder="%"
                            value={p.percent}
                            onValueChange={e => patchPayment(p.key, { percent: e.value ?? null })}
                            aria-label={`Percentage for payment ${i + 1}`}
                            className="w-full"
                          />
                        ) : (
                          <InputNumber
                            inputId={`payment-amount-${i}`}
                            mode="currency"
                            currency="USD"
                            locale="en-US"
                            placeholder="$0.00"
                            value={p.amount}
                            onValueChange={e => patchPayment(p.key, { amount: e.value ?? null })}
                            aria-label={`Amount for payment ${i + 1}`}
                            className="w-full"
                          />
                        )}
                      </div>
                    )}
                    <Button
                      type="button"
                      icon="pi pi-times"
                      rounded
                      text
                      severity="secondary"
                      aria-label={`Remove payment ${i + 1}`}
                      onClick={() => removePayment(p.key)}
                    />
                    {p.itemId !== null && (
                      <small className="debt-editor__share">
                        {share > 0 ? `${formatMoney(share)} each time · ` : ''}
                        {schedule(p.itemId)}
                        {p.mode === 'percent' &&
                          taken > 0 &&
                          ` · ${formatPercent(100 - taken)} left (other accounts take ${formatPercent(taken)})`}
                      </small>
                    )}
                  </div>
                );
              })}

              <div>
                <Button
                  type="button"
                  icon="pi pi-plus"
                  label="Add a payment"
                  text
                  size="small"
                  disabled={sourceOptions.length === 0 || form.payments.length >= MAX_PAYMENTS}
                  onClick={addPayment}
                />
                {sourceOptions.length === 0 && (
                  <small className="text-color-secondary">
                    Add a <Link to="/command/transactions/credits">credit</Link> or a{' '}
                    <Link to="/command/transactions/debits">debit</Link> first to pay this from it.
                  </small>
                )}
              </div>
            </fieldset>

            {problem && <Message severity="error" text={problem} className="w-full" />}

            <div className="flex justify-end gap-2">
              <Button type="button" label="Cancel" text disabled={saving} onClick={closeEditor} />
              <Button
                type="submit"
                label={editing === 'new' ? 'Add account' : 'Save'}
                icon="pi pi-check"
                loading={saving}
              />
            </div>
          </form>
        )}
      </Dialog>

      {/* Delete */}
      <Dialog
        header="Delete account"
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
              Delete <strong>{pendingDelete.name}</strong> with its purchases and payments? Your
              credits and debits themselves stay.
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
