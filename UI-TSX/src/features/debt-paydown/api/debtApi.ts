// src/features/debt-paydown/api/debtApi.ts
import { DebtClient } from '@/api/generated/DebtClient';
import { unwrap } from '@/api/utils/responseHelpers';
import type { Debt, DebtRequest } from '../types/Debt';

export async function listDebts(): Promise<Debt[]> {
  return unwrap<Debt[]>(await DebtClient.list(), []);
}

export async function createDebt(request: DebtRequest): Promise<Debt> {
  return unwrap<Debt>(await DebtClient.create(request), null, true) as Debt;
}

export async function updateDebt(id: number, request: DebtRequest): Promise<Debt> {
  return unwrap<Debt>(await DebtClient.update(id, request), null, true) as Debt;
}

export async function deleteDebt(id: number): Promise<void> {
  await DebtClient.remove(id);
}
