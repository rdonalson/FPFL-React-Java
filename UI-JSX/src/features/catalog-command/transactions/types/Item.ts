// src/features/catalog-command/transactions/types/Item.ts

// C:\Users\rickd\source\repos\FPFL-React-Java\UI-JSX\src\features\catalog-command\transactions\types\Item
export interface ItemTypeDto {
  Id: number;
  Name?: string;
  [key: string]: any;
}

export interface TimePeriodDto {
  Id?: number;
  Name?: string;
  [key: string]: any;
}

/**
 * Item model used for both request and response.
 * Matches backend ApiResponse.data shape.
 */
export interface Item {
  id?: number;
  userId: string;
  name?: string;
  amount: number;
  ItemType?: ItemTypeDto | null;
  TimePeriod?: TimePeriodDto | null;
  beginDate?: string | null;
  endDate?: string | null;
  weeklyDow?: number | null;
  everyOtherWeekDow?: number | null;
  biMonthlyDay1?: number | null;
  biMonthlyDay2?: number | null;
  monthlyDom?: number | null;
  quarterly1Month?: number | null;
  quarterly1Day?: number | null;
  quarterly2Month?: number | null;
  quarterly2Day?: number | null;
  quarterly3Month?: number | null;
  quarterly3Day?: number | null;
  quarterly4Month?: number | null;
  quarterly4Day?: number | null;
  semiAnnual1Month?: number | null;
  semiAnnual1Day?: number | null;
  semiAnnual2Month?: number | null;
  semiAnnual2Day?: number | null;
  annualMoy?: number | null;
  annualDom?: number | null;
  dateRangeReq?: boolean | null;

  // POST/PUT fields expected by backend
  fkItemType?: number;
  fkPeriod?: number | null;
}
