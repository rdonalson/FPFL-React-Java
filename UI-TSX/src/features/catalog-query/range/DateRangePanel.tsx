// src/features/catalog-query/display/DateRangePanel.tsx
import { Calendar } from 'primereact/calendar';
import { Checkbox } from 'primereact/checkbox';
import { Button } from 'primereact/button';
import type { DisplayRequest } from '../types/DisplayRequest';

interface Props {
  criteria: DisplayRequest;
  updateStart: (value: Date | null) => void;
  updateEnd: (value: Date | null) => void;
  updateGrouping: (value: boolean) => void;
  onCalculate: () => void;
}

export function DateRangePanel({
  criteria,
  updateStart,
  updateEnd,
  updateGrouping,
  onCalculate,
}: Props) {
  return (
    <div className="mt-4">
      <div className="p-4 surface-card border-round shadow-2 max-w-sm">
        <div className="p-fluid">
          <div className="field mb-4">
            <label htmlFor="ledgerStartDate" className="font-bold mb-2 block">
              Start Date
            </label>
            <Calendar
              id="ledgerStartDate"
              value={criteria.ledgerStartDate}
              onChange={e => updateStart(e.value as Date)}
              dateFormat="mm/dd/yy"
              showIcon
              className="w-full"
            />
          </div>

          <div className="field mb-4">
            <label htmlFor="ledgerEndDate" className="font-bold mb-2 block">
              End Date
            </label>
            <Calendar
              id="ledgerEndDate"
              value={criteria.ledgerEndDate}
              onChange={e => updateEnd(e.value as Date)}
              dateFormat="mm/dd/yy"
              showIcon
              className="w-full"
            />
          </div>

          <div className="field mb-4 flex items-center">
            <Checkbox
              inputId="includeGrouping"
              checked={criteria.includeGrouping}
              onChange={e => updateGrouping(e.checked ?? false)}
            />
            <label htmlFor="includeGrouping" className="ml-2 font-bold">
              Grouping?
            </label>
          </div>

          <Button
            label="Calculate"
            icon="pi pi-clock"
            className="p-button-success w-full"
            onClick={onCalculate}
          />
        </div>
      </div>
    </div>
  );
}
