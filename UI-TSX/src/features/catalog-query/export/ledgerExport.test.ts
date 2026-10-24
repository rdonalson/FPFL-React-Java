import { describe, expect, it, vi } from 'vitest';
import * as ExcelModule from 'exceljs';
import type { Row, Worksheet } from 'exceljs';

import { LEDGER, PAYDOWN } from '../test-ledger';
import type { LedgerDto } from '../types/DispayResponse';
import {
  buildLedgerWorkbook,
  excelDate,
  exportLedgerToExcel,
  formatDay,
  ledgerFileName,
  resolveExcel,
  summarizeLedger,
} from './ledgerExport';

const excel = resolveExcel(ExcelModule);
const options = {
  start: '2026-10-30',
  end: '2026-10-31',
  createdAt: new Date(2026, 9, 24, 13, 30),
};

const day = (
  wDate: string,
  runningTotal: number,
  net = 0,
  items: LedgerDto['items'] = [],
): LedgerDto => ({
  rollupKey: 0,
  year: 2026,
  wDate,
  creditSummary: Math.max(net, 0),
  debitSummary: Math.min(net, 0),
  net,
  runningTotal,
  items,
});

function sheet(days: readonly LedgerDto[] = LEDGER): Worksheet {
  return buildLedgerWorkbook(excel, days, options).getWorksheet('Ledger')!;
}

/** Rows whose first cell is a date, i.e. the day rows. */
function dayRows(ws: Worksheet): Row[] {
  const rows: Row[] = [];
  ws.eachRow(row => {
    if (row.getCell(1).value instanceof Date) rows.push(row);
  });
  return rows;
}

/** Values of columns A–E (ExcelJS's row.values skips empty cells). */
const cells = (row: Row, count = 5) =>
  Array.from({ length: count }, (_, i) => row.getCell(i + 1).value);

const columnA = (ws: Worksheet) => {
  const values: unknown[] = [];
  ws.eachRow(row => values.push(row.getCell(1).value));
  return values;
};

describe('summarizeLedger', () => {
  it('totals the window and finds its lowest point', () => {
    expect(summarizeLedger(LEDGER)).toEqual({
      startingBalance: 100,
      totalCredits: 2000,
      totalDebits: -500,
      endingBalance: 1600,
      lowest: { amount: 1600, date: '2026-10-30' },
      firstNegativeDate: null,
    });
  });

  it('flags the first day below zero, and is null with no days', () => {
    const days = [
      day('2026-10-17', 100),
      day('2026-10-18', -50, -150),
      day('2026-10-19', -80, -30),
    ];
    expect(summarizeLedger(days)).toMatchObject({
      lowest: { amount: -80, date: '2026-10-19' },
      firstNegativeDate: '2026-10-18',
    });
    expect(summarizeLedger([])).toBeNull();
  });
});

describe('Ledger sheet', () => {
  it('starts with a title, the range and a summary', () => {
    const ws = sheet();
    expect(ws.getCell('A1').value).toBe('Forecast Ledger');
    expect(ws.getCell('A2').value).toBe('Oct 30, 2026 – Oct 31, 2026 · 2 days');
    expect(String(ws.getCell('A3').value)).toContain('Created Oct 24, 2026');

    const labels = columnA(ws);
    for (const label of [
      'Starting balance',
      'Credits',
      'Debits',
      'Ending balance',
      'Lowest balance',
    ]) {
      expect(labels).toContain(label);
    }
  });

  it('writes one row per day under frozen column headers', () => {
    const ws = sheet();
    const days = dayRows(ws);
    expect(days).toHaveLength(2);

    const header = ws.getRow(days[0].number - 1);
    expect(cells(header)).toEqual(['Date', 'Credits', 'Debits', 'Net change', 'Balance']);
    expect(ws.views[0]).toMatchObject({ state: 'frozen', ySplit: header.number });

    expect(cells(days[0])).toEqual([excelDate('2026-10-30'), 2000, -500, 1500, 1600]);
    expect(cells(days[1])).toEqual([excelDate('2026-10-31'), 0, 0, 0, 1600]);
  });

  it("puts a day's credits and debits in a collapsed outline group below it", () => {
    const ws = sheet();
    const [busy, quiet] = dayRows(ws);
    expect(ws.properties.outlineProperties?.summaryBelow).toBe(false);

    const subHeader = ws.getRow(busy.number + 1);
    const paycheck = ws.getRow(busy.number + 2);
    const paydown = ws.getRow(busy.number + 3);
    expect(cells(subHeader)).toEqual([null, 'Credits', 'Debits', 'Name', 'Period']);
    expect(cells(paycheck)).toEqual([null, 2000, null, 'Paycheck', 'Every Two Weeks']);
    expect(cells(paydown)).toEqual([null, null, -500, 'Chase Visa paydown', '25% of Paycheck']);

    for (const row of [subHeader, paycheck, paydown]) {
      expect(row.outlineLevel).toBe(1);
      expect(row.hidden).toBe(true);
    }
    expect(busy.outlineLevel ?? 0).toBe(0);
    // A day without items has no group
    expect(quiet.number).toBe(busy.number + 4);
  });

  it('warns when the balance goes negative', () => {
    const ws = sheet([day('2026-10-17', 100), day('2026-10-18', -50, -150)]);
    expect(columnA(ws)).toContain('Your balance drops below $0.00 on Oct 18, 2026.');
    expect(columnA(sheet())).not.toContainEqual(expect.stringContaining('drops below'));
  });

  it('still builds with no days', () => {
    const ws = sheet([]);
    expect(ws.getCell('A2').value).toBe('Oct 30, 2026 – Oct 31, 2026 · 0 days');
    expect(dayRows(ws)).toHaveLength(0);
  });
});

describe('Debt Paydown sheet', () => {
  it('lists each debt with its activity grouped underneath', () => {
    const ws = buildLedgerWorkbook(excel, LEDGER, { ...options, paydown: PAYDOWN }).getWorksheet(
      'Debt Paydown',
    )!;
    expect(ws.getCell('A1').value).toBe('Debt Paydown');

    let summary: Row | undefined;
    ws.eachRow(row => {
      if (row.getCell(1).value === 'Chase Visa') summary = row;
    });
    expect(cells(summary!, 6)).toEqual([
      'Chase Visa',
      1000,
      500,
      0,
      500,
      'Nov 13, 2026 (after range)',
    ]);

    const sub = ws.getRow(summary!.number + 1);
    const allocation = ws.getRow(summary!.number + 2);
    expect(cells(sub)).toEqual([null, 'Date', 'Activity', 'Amount', 'Balance left']);
    expect(cells(allocation)).toEqual([null, excelDate('2026-10-30'), '25% of Paycheck', 500, 500]);
    for (const row of [sub, allocation]) {
      expect(row.outlineLevel).toBe(1);
      expect(row.hidden).toBe(true);
    }
  });

  it('says when a debt is paid off in range, or not within 30 years', () => {
    const paid = { ...PAYDOWN[0], name: 'Paid', payoffDate: '2026-10-31' };
    const never = { ...PAYDOWN[0], name: 'Never', payoffDate: null, events: [] };
    const ws = buildLedgerWorkbook(excel, LEDGER, {
      ...options,
      paydown: [paid, never],
    }).getWorksheet('Debt Paydown')!;
    const payoff = (name: string) => {
      let value: unknown;
      ws.eachRow(row => {
        if (row.getCell(1).value === name) value = row.getCell(6).value;
      });
      return value;
    };
    expect(payoff('Paid')).toBe('Oct 31, 2026');
    expect(payoff('Never')).toBe('Not within 30 years');
  });

  it('is left out when nothing is tracked', () => {
    expect(
      buildLedgerWorkbook(excel, LEDGER, { ...options, paydown: [] }).getWorksheet('Debt Paydown'),
    ).toBeUndefined();
    expect(
      buildLedgerWorkbook(excel, LEDGER, options).getWorksheet('Debt Paydown'),
    ).toBeUndefined();
  });
});

describe('helpers', () => {
  it('keeps calendar days in any time zone and names the file by range', () => {
    expect(excelDate('2026-10-18').toISOString()).toBe('2026-10-18T00:00:00.000Z');
    expect(formatDay('2026-10-18')).toBe('Oct 18, 2026');
    expect(ledgerFileName(options)).toBe('Forecast Ledger 2026-10-30 to 2026-10-31.xlsx');
  });

  it('accepts ExcelJS with or without a default export', () => {
    expect(resolveExcel({ default: excel })).toBe(excel);
    expect(resolveExcel(excel)).toBe(excel);
  });
});

describe('exportLedgerToExcel', () => {
  it('produces a real .xlsx and downloads it under the range name', async () => {
    const workbookBuffer = await buildLedgerWorkbook(excel, LEDGER, options).xlsx.writeBuffer();
    const bytes = new Uint8Array(workbookBuffer as ArrayBuffer);
    expect(bytes.length).toBeGreaterThan(1000);
    expect(String.fromCharCode(bytes[0], bytes[1])).toBe('PK'); // .xlsx is a zip

    const createObjectURL = vi.fn(() => 'blob:ledger');
    const revokeObjectURL = vi.fn();
    Object.assign(URL, { createObjectURL, revokeObjectURL });
    const click = vi.spyOn(HTMLAnchorElement.prototype, 'click').mockImplementation(function (
      this: HTMLAnchorElement,
    ) {
      expect(this.download).toBe('Forecast Ledger 2026-10-30 to 2026-10-31.xlsx');
      expect(this.href).toBe('blob:ledger');
    });

    await exportLedgerToExcel(LEDGER, { ...options, paydown: PAYDOWN });

    expect(click).toHaveBeenCalledTimes(1);
    const blob = (createObjectURL.mock.calls[0] as unknown[])[0] as Blob;
    expect(blob.type).toBe('application/vnd.openxmlformats-officedocument.spreadsheetml.sheet');
    await vi.waitFor(() => expect(revokeObjectURL).toHaveBeenCalledWith('blob:ledger'));
    click.mockRestore();
  });
});
