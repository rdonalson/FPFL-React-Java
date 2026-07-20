// src/features/catalog-query/display/types/DisplayModels.ts

export interface ItemDto {
  amount: number;
  fkItemType: number;
  itemKey: number;
  itemType: string;
  name: string;
  occurrenceDate: string;
  period: string;
  rollupKey: number | null;
  year: number | null;
}

export interface LedgerDto {
  creditSummary: number;
  debitSummary: number;
  net: number;
  runningTotal: number;
  rollupKey: number;
  wDate: string;
  year: number;
  items: ItemDto[];
}
