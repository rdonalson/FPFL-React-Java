// src/api/generated/DisplayClient.ts
import { apiClient } from '@/api/client'; // same base client used by ItemClient

export class DisplayClient {
  static async fetchLedger(payload: {
    userId: string;
    ledgerStartDate: string;
    ledgerEndDate: string;
    includeGrouping: boolean;
  }) {
    return apiClient.post('/display/ledger', payload);
  }
}
