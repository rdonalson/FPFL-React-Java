import { DisplayClient } from '@/api/generated/DisplayClient';
import { unwrap } from '@/api/utils/responseHelpers';
import type { DisplayRequest } from '@/features/catalog-query/types/DisplayRequest';
import type { LedgerDto, ItemDto } from '../types/DispayResponse';

export async function fetchDisplayLedger(criteria: DisplayRequest): Promise<LedgerDto[] | null> {
  const payload = {
    userId: criteria.userId!,
    ledgerStartDate: criteria.ledgerStartDate?.toISOString().slice(0, 10) ?? '',
    ledgerEndDate: criteria.ledgerEndDate?.toISOString().slice(0, 10) ?? '',
    includeGrouping: criteria.includeGrouping,
  };

  const res = await DisplayClient.fetchLedger(payload);

  // unwrap returns the raw backend response
  const raw = unwrap(res, null);
  if (!raw) return null;

  // Convert raw response into LedgerDto[]
  const ledgerRows: LedgerDto[] = raw.data.map((row: any) => ({
    creditSummary: row.creditSummary,
    debitSummary: row.debitSummary,
    net: row.net,
    runningTotal: row.runningTotal,
    rollupKey: row.rollupKey,
    wDate: row.wDate,
    year: row.year,
    items: row.items.map((item: any): ItemDto => ({
      amount: item.amount,
      fkItemType: item.fkItemType,
      itemKey: item.itemKey,
      itemType: item.itemType,
      name: item.name,
      occurrenceDate: item.occurrenceDate,
      period: item.period,
      rollupKey: item.rollupKey,
      year: item.year,
    })),
  }));

  return ledgerRows;
}
