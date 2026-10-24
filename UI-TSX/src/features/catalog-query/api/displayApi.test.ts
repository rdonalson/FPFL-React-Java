import { beforeEach, describe, expect, it, vi } from 'vitest';

const client = vi.hoisted(() => ({ fetchLedger: vi.fn(), fetchPaydown: vi.fn() }));
vi.mock('@/api/generated/DisplayClient', () => ({ DisplayClient: client }));

import { fetchDisplayLedger, fetchDisplayPaydown } from './displayApi';

const criteria = {
  userId: 'user-1',
  ledgerStartDate: new Date('2026-10-01T12:00:00Z'),
  ledgerEndDate: new Date('2026-12-31T12:00:00Z'),
  includeGrouping: false,
};

beforeEach(() => vi.clearAllMocks());

describe('displayApi', () => {
  it('sends the range as yyyy-MM-dd and maps the ledger rows', async () => {
    client.fetchLedger.mockResolvedValue({
      data: {
        data: [
          {
            rollupKey: 1,
            year: 2026,
            wDate: '2026-10-01',
            creditSummary: 10,
            debitSummary: -5,
            net: 5,
            runningTotal: 105,
            items: [
              {
                itemKey: 1,
                fkItemType: 1,
                itemType: 'Credit',
                name: 'Pay',
                amount: 10,
                period: 'Daily',
                occurrenceDate: '2026-10-01',
                extra: 'dropped',
              },
            ],
            extra: 'dropped',
          },
        ],
      },
    });

    const rows = await fetchDisplayLedger(criteria);

    expect(client.fetchLedger).toHaveBeenCalledWith({
      userId: 'user-1',
      ledgerStartDate: '2026-10-01',
      ledgerEndDate: '2026-12-31',
      includeGrouping: false,
    });
    expect(rows?.[0]).toMatchObject({ wDate: '2026-10-01', runningTotal: 105 });
    expect(rows?.[0]).not.toHaveProperty('extra');
    expect(rows?.[0].items[0]).toMatchObject({ name: 'Pay', period: 'Daily' });
    expect(rows?.[0].items[0]).not.toHaveProperty('extra');
  });

  it('returns null when there is no ledger', async () => {
    client.fetchLedger.mockResolvedValue(null);
    expect(await fetchDisplayLedger(criteria)).toBeNull();
  });

  it('returns the paydown list, or nothing', async () => {
    client.fetchPaydown.mockResolvedValue({ data: { data: [{ debtId: 1 }] } });
    expect(await fetchDisplayPaydown(criteria)).toEqual([{ debtId: 1 }]);
    client.fetchPaydown.mockResolvedValue({ data: { data: null } });
    expect(await fetchDisplayPaydown(criteria)).toEqual([]);
  });
});
