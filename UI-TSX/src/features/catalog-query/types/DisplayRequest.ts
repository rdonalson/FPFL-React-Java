export interface DisplayRequest {
  userId: string | null;
  ledgerStartDate: Date | null;
  ledgerEndDate: Date | null;
  includeGrouping: boolean;
}
