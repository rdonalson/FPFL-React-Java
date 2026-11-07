// src/features/accounts/api/accountApi.ts
import { AccountClient } from '@/api/generated/AccountClient';
import { unwrap } from '@/api/utils/responseHelpers';
import type { Account, AccountRequest, Purchase, PurchaseRequest } from '../types/Account';

export async function listAccounts(): Promise<Account[]> {
  return unwrap<Account[]>(await AccountClient.list(), []);
}

export async function getAccount(id: number): Promise<Account> {
  return unwrap<Account>(await AccountClient.get(id), null, true) as Account;
}

export async function createAccount(request: AccountRequest): Promise<Account> {
  return unwrap<Account>(await AccountClient.create(request), null, true) as Account;
}

export async function updateAccount(id: number, request: AccountRequest): Promise<Account> {
  return unwrap<Account>(await AccountClient.update(id, request), null, true) as Account;
}

export async function deleteAccount(id: number): Promise<void> {
  await AccountClient.remove(id);
}

export async function addPurchase(accountId: number, request: PurchaseRequest): Promise<Purchase> {
  return unwrap<Purchase>(
    await AccountClient.addPurchase(accountId, request),
    null,
    true,
  ) as Purchase;
}

export async function updatePurchase(
  accountId: number,
  purchaseId: number,
  request: PurchaseRequest,
): Promise<Purchase> {
  return unwrap<Purchase>(
    await AccountClient.updatePurchase(accountId, purchaseId, request),
    null,
    true,
  ) as Purchase;
}

export async function deletePurchase(accountId: number, purchaseId: number): Promise<void> {
  await AccountClient.removePurchase(accountId, purchaseId);
}
