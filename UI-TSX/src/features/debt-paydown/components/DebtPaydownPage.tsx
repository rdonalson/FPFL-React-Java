// src/features/debt-paydown/components/DebtPaydownPage.tsx
import { useEffect, useMemo, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Button } from 'primereact/button';
import { Card } from 'primereact/card';
import { Dialog } from 'primereact/dialog';
import { Dropdown } from 'primereact/dropdown';
import { InputNumber } from 'primereact/inputnumber';
import { Calendar } from 'primereact/calendar';
import { Message } from 'primereact/message';

import { getSessionUserId } from '@/app/state/sessionHelpers';
import { useToast } from '@/shared/hooks/useToast';
import { getItemsForUserAndType } from '@/features/catalog-command/transactions/api/itemApi';
import type { Item } from '@/features/catalog-command/transactions/types/Item';

import { createDebt, deleteDebt, listDebts, updateDebt } from '../api/debtApi';
import {
  MAX_ALLOCATIONS,
  allocatedElsewhere,
  emptyDebtForm,
  formatDate,
  formatMoney,
  formatPercent,
  newAllocation,
  perOccurrence,
  toDebtForm,
  toDebtRequest,
  validateDebtForm,
  type AllocationForm,
  type Debt,
  type DebtForm,
} from '../types/Debt';

const CREDIT = 1;
const DEBIT = 2;

/**
 * Debt Paydown setup: choose debits to pay down, what's owed on each, and which credits send a
 * percentage of every occurrence to them. The forecast (Display) deducts those shares and charts
 * each debt's payoff.
 */
export default function DebtPaydownPage() {
  const navigate = useNavigate();
  const toast = useToast();

  const [debts, setDebts] = useState<Debt[]>([]);
  const [debits, setDebits] = useState<Item[]>([]);
  const [credits, setCredits] = useState<Item[]>([]);
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState<string | null>(null);

  // Load the debts plus the user's debits and credits once; state is only set after the awaits
  useEffect(() => {
    let cancelled = false;
    const userId = getSessionUserId();

    const loadAll = async () => {
      if (!userId) throw new Error('No user session found.');
      return Promise.all([
        listDebts(),
        getItemsForUserAndType(userId, DEBIT),
        getItemsForUserAndType(userId, CREDIT),
      ]);
    };

    loadAll()
      .then(([d, debitItems, creditItems]) => {
        if (cancelled) return;
        setDebts(d);
        setDebits(debitItems);
        setCredits(creditItems);
      })
      .catch((e: any) => {
        if (!cancelled) setLoadError(e?.message ?? 'Could not load your debts.');
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });

    return () => {
      cancelled = true;
    };
  }, []);

  const itemsById = useMemo(
    () => new Map<number, Item>([...debits, ...credits].map(i => [i.id!, i])),
    [debits, credits],
  );

  const untrackedDebits = useMemo(() => {
    const tracked = new Set(debts.map(d => d.itemId));
    return debits.filter(d => !tracked.has(d.id!));
  }, [debts, debits]);

  const schedule = (id: number | null) =>
    id === null ? '' : (itemsById.get(id)?.TimePeriod?.Name ?? '');

  // ---- editor ----------------------------------------------------------------------------

  /** null = closed; 'new' = tracking a debit; a Debt = editing it. */
  const [editing, setEditing] = useState<Debt | 'new' | null>(null);
  const [form, setForm] = useState<DebtForm>(() => emptyDebtForm(new Date()));
  const [saving, setSaving] = useState(false);
  const [problem, setProblem] = useState<string | null>(null);

  const editingId = editing && editing !== 'new' ? editing.id : null;
  const elsewhere = useMemo(() => allocatedElsewhere(debts, editingId), [debts, editingId]);

  const debitOptions =
    editing && editing !== 'new' ? debits.filter(d => d.id === editing.itemId) : untrackedDebits;
  const chosenDebit = form.itemId === null ? null : (itemsById.get(form.itemId) ?? null);

  const openNew = () => {
    setForm(emptyDebtForm(new Date()));
    setProblem(null);
    setEditing('new');
  };

  const openEdit = (debt: Debt) => {
    setForm(toDebtForm(debt));
    setProblem(null);
    setEditing(debt);
  };

  const closeEditor = () => {
    if (!saving) setEditing(null);
  };

  const patch = (changes: Partial<DebtForm>) => {
    setForm(f => ({ ...f, ...changes }));
    setProblem(null);
  };

  const patchAllocation = (key: number, changes: Partial<AllocationForm>) => {
    setForm(f => ({
      ...f,
      allocations: f.allocations.map(a => (a.key === key ? { ...a, ...changes } : a)),
    }));
    setProblem(null);
  };

  const addAllocation = () =>
    setForm(f => ({ ...f, allocations: [...f.allocations, newAllocation()] }));

  const removeAllocation = (key: number) => {
    setForm(f => ({ ...f, allocations: f.allocations.filter(a => a.key !== key) }));
    setProblem(null);
  };

  const save = async () => {
    if (!editing) return;
    const issue = validateDebtForm(form, elsewhere, id => itemsById.get(id)?.name ?? 'That credit');
    if (issue) {
      setProblem(issue);
      return;
    }

    setSaving(true);
    try {
      const request = toDebtRequest(form);
      if (editing === 'new') await createDebt(request);
      else await updateDebt(editing.id, request);
      toast?.current?.show({
        severity: 'success',
        summary: editing === 'new' ? 'Debt tracked' : 'Debt updated',
        life: 3000,
      });
      setEditing(null);
      setDebts(await listDebts());
    } catch (e: any) {
      setProblem(e?.message ?? 'Could not save the debt.');
    } finally {
      setSaving(false);
    }
  };

  // ---- delete ----------------------------------------------------------------------------

  const [pendingDelete, setPendingDelete] = useState<Debt | null>(null);
  const [deleting, setDeleting] = useState(false);
  const [deleteError, setDeleteError] = useState<string | null>(null);

  const confirmDelete = (debt: Debt) => {
    setDeleteError(null);
    setPendingDelete(debt);
  };

  const doDelete = async () => {
    if (!pendingDelete) return;
    setDeleting(true);
    try {
      await deleteDebt(pendingDelete.id);
      toast?.current?.show({ severity: 'success', summary: 'Stopped tracking', life: 3000 });
      setPendingDelete(null);
      setDebts(await listDebts());
    } catch (e: any) {
      setDeleteError(e?.message ?? 'Could not stop tracking the debt.');
    } finally {
      setDeleting(false);
    }
  };

  // ---- render ----------------------------------------------------------------------------

  return (
    <div className="app-page app-page-gutter p-4 flex flex-col gap-4 debt-paydown">
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
              <i className="pi pi-credit-card mr-2" aria-hidden="true" />
              Debt Paydown
            </h2>
            <p className="m-0 mt-1 text-color-secondary">
              Send part of your credits to paying down a debt. The forecast deducts it and charts
              the payoff.
            </p>
          </div>
          <div className="flex gap-2">
            <Button
              label="Track a debt"
              icon="pi pi-plus"
              className="p-button-success"
              disabled={untrackedDebits.length === 0}
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

      {debts.length > 0 ? (
        <div className="debt-paydown__list">
          {debts.map(debt => (
            <Card key={debt.id} className="debt-card" aria-label={debt.name ?? 'Debt'}>
              <div className="flex justify-between items-start gap-2">
                <div>
                  <h3 className="text-lg font-semibold m-0">{debt.name}</h3>
                  <p className="m-0 text-sm text-color-secondary">
                    Payment {formatMoney(debt.payment)}
                    {debt.period ? ` · ${debt.period}` : ''}
                  </p>
                </div>
                <div className="flex gap-1">
                  <Button
                    icon="pi pi-pencil"
                    rounded
                    text
                    aria-label={`Edit ${debt.name}`}
                    onClick={() => openEdit(debt)}
                  />
                  <Button
                    icon="pi pi-trash"
                    rounded
                    text
                    severity="danger"
                    aria-label={`Stop tracking ${debt.name}`}
                    onClick={() => confirmDelete(debt)}
                  />
                </div>
              </div>

              <div className="debt-card__owed">
                <span className="debt-card__label">Owed</span>
                <span className="debt-card__balance">{formatMoney(debt.balance)}</span>
                <span className="text-sm text-color-secondary">
                  as of {formatDate(debt.balanceDate)} ·{' '}
                  {debt.apr > 0 ? `${formatPercent(debt.apr)} APR` : 'no interest'}
                </span>
              </div>

              <span className="debt-card__label">Allocations</span>
              {debt.allocations.length === 0 ? (
                <p className="m-0 text-sm text-color-secondary">
                  Only the debit&apos;s own payment pays this down. Edit it to send part of a credit
                  here too.
                </p>
              ) : (
                <ul className="debt-card__allocations">
                  {debt.allocations.map(a => (
                    <li key={a.id}>
                      <span className="debt-card__percent">{formatPercent(a.percent)}</span>
                      <span>
                        of <strong>{a.creditName}</strong>
                        {a.creditPeriod && (
                          <span className="text-sm text-color-secondary"> · {a.creditPeriod}</span>
                        )}
                      </span>
                      <span className="debt-card__share">{formatMoney(a.perOccurrence)} each</span>
                    </li>
                  ))}
                </ul>
              )}
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
            <i className="pi pi-credit-card text-4xl text-color-secondary" aria-hidden="true" />
            {debits.length === 0 ? (
              <p className="m-0">
                Add the debt&apos;s payment as a{' '}
                <Link to="/command/transactions/debits">debit</Link> first (for example your credit
                card payment), then track it here.
              </p>
            ) : (
              <>
                <p className="m-0">
                  Pick a debit, such as a credit card payment, enter what you owe, and choose how
                  much of each credit goes toward it.
                </p>
                <Button label="Track a debt" icon="pi pi-plus" onClick={openNew} />
              </>
            )}
          </div>
        </Card>
      ) : null}

      {/* Track / edit */}
      <Dialog
        header={
          editing === 'new' || editing === null ? 'Track a debt' : `Edit ${editing.name ?? 'debt'}`
        }
        visible={editing !== null}
        onHide={closeEditor}
        modal
        draggable={false}
        style={{ width: '40rem' }}
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
            <div className="debt-editor__grid">
              <div className="debt-editor__field debt-editor__field--wide">
                <label htmlFor="debt-item">Debit being paid down</label>
                <Dropdown
                  inputId="debt-item"
                  options={debitOptions}
                  optionLabel="name"
                  optionValue="id"
                  placeholder="Choose a debit, e.g. a credit card payment"
                  value={form.itemId}
                  onChange={e => patch({ itemId: e.value })}
                  disabled={editing !== 'new'}
                  className="w-full"
                />
                {chosenDebit && (
                  <small className="text-color-secondary">
                    Its payment of {formatMoney(chosenDebit.amount)}
                    {schedule(chosenDebit.id!) ? ` (${schedule(chosenDebit.id!)})` : ''} also pays
                    it down.
                  </small>
                )}
              </div>

              <div className="debt-editor__field">
                <label htmlFor="debt-balance">Balance owed</label>
                <InputNumber
                  inputId="debt-balance"
                  mode="currency"
                  currency="USD"
                  locale="en-US"
                  placeholder="$0.00"
                  value={form.balance}
                  onValueChange={e => patch({ balance: e.value ?? null })}
                  className="w-full"
                />
              </div>

              <div className="debt-editor__field">
                <label htmlFor="debt-date">As of</label>
                <Calendar
                  inputId="debt-date"
                  value={form.balanceDate}
                  onChange={e => patch({ balanceDate: (e.value as Date | null) ?? null })}
                  dateFormat="M d, yy"
                  showIcon
                  className="w-full"
                />
              </div>

              <div className="debt-editor__field">
                <label htmlFor="debt-apr">APR</label>
                <InputNumber
                  inputId="debt-apr"
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
            </div>

            <fieldset className="debt-editor__allocations">
              <legend>Allocations</legend>
              <p className="m-0 text-sm text-color-secondary">
                Each time the credit comes in, this share goes to the debt.
              </p>

              {form.allocations.length === 0 && (
                <p className="m-0 text-sm">
                  None yet: only the debit&apos;s own payment will pay it down.
                </p>
              )}

              {form.allocations.map((a, i) => {
                const taken = a.creditItemId === null ? 0 : (elsewhere.get(a.creditItemId) ?? 0);
                const share = perOccurrence(
                  a.creditItemId === null ? null : itemsById.get(a.creditItemId)?.amount,
                  a.percent,
                );
                return (
                  <div key={a.key} className="debt-editor__allocation">
                    <Dropdown
                      inputId={`alloc-credit-${i}`}
                      options={credits}
                      optionLabel="name"
                      optionValue="id"
                      placeholder="Choose a credit"
                      value={a.creditItemId}
                      onChange={e => patchAllocation(a.key, { creditItemId: e.value })}
                      aria-label={`Credit for allocation ${i + 1}`}
                      className="w-full"
                    />
                    <InputNumber
                      inputId={`alloc-percent-${i}`}
                      suffix="%"
                      minFractionDigits={0}
                      maxFractionDigits={2}
                      min={0}
                      max={100}
                      placeholder="%"
                      value={a.percent}
                      onValueChange={e => patchAllocation(a.key, { percent: e.value ?? null })}
                      aria-label={`Percentage for allocation ${i + 1}`}
                      className="w-full"
                    />
                    <Button
                      type="button"
                      icon="pi pi-times"
                      rounded
                      text
                      severity="secondary"
                      aria-label={`Remove allocation ${i + 1}`}
                      onClick={() => removeAllocation(a.key)}
                    />
                    {a.creditItemId !== null && (
                      <small className="debt-editor__share">
                        {share > 0 ? `${formatMoney(share)} each time · ` : ''}
                        {schedule(a.creditItemId)}
                        {taken > 0 &&
                          ` · ${formatPercent(100 - taken)} left (other debts take ${formatPercent(taken)})`}
                      </small>
                    )}
                  </div>
                );
              })}

              <div>
                <Button
                  type="button"
                  icon="pi pi-plus"
                  label="Add allocation"
                  text
                  size="small"
                  disabled={credits.length === 0 || form.allocations.length >= MAX_ALLOCATIONS}
                  onClick={addAllocation}
                />
                {credits.length === 0 && (
                  <small className="text-color-secondary">
                    Add a <Link to="/command/transactions/credits">credit</Link> first to allocate
                    part of it.
                  </small>
                )}
              </div>
            </fieldset>

            {problem && <Message severity="error" text={problem} className="w-full" />}

            <div className="flex justify-end gap-2">
              <Button type="button" label="Cancel" text disabled={saving} onClick={closeEditor} />
              <Button
                type="submit"
                label={editing === 'new' ? 'Track debt' : 'Save'}
                icon="pi pi-check"
                loading={saving}
              />
            </div>
          </form>
        )}
      </Dialog>

      {/* Stop tracking */}
      <Dialog
        header="Stop tracking"
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
              Stop tracking <strong>{pendingDelete.name}</strong>? Its allocations are removed and
              the forecast stops deducting them. The debit itself stays.
            </p>
            <div className="flex justify-end gap-2">
              <Button
                label="Cancel"
                text
                disabled={deleting}
                onClick={() => setPendingDelete(null)}
              />
              <Button
                label="Stop tracking"
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
