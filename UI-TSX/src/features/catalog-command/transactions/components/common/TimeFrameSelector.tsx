import { Checkbox } from 'primereact/checkbox';
import { Calendar } from 'primereact/calendar';

export interface TimeFrameSelectorProps {
  dateRangeReq: boolean;
  beginDate: Date | null;
  endDate: Date | null;
  onChange: (value: {
    dateRangeReq: boolean;
    beginDate: Date | null;
    endDate: Date | null;
  }) => void;
}

export function TimeFrameSelector({
  dateRangeReq,
  beginDate,
  endDate,
  onChange,
}: TimeFrameSelectorProps) {
  const toggle = (checked: boolean) => {
    onChange({
      dateRangeReq: checked,
      beginDate: checked ? beginDate : null,
      endDate: checked ? endDate : null,
    });
  };

  const updateBegin = (value: Date | null) => {
    onChange({
      dateRangeReq,
      beginDate: value,
      endDate,
    });
  };

  const updateEnd = (value: Date | null) => {
    onChange({
      dateRangeReq,
      beginDate,
      endDate: value,
    });
  };

  return (
    <div className="time-frame-selector w-full">
      {/* Checkbox */}
      <div className="flex items-center gap-2 mb-3">
        <Checkbox checked={dateRangeReq} onChange={e => toggle(e.checked!)} />
        <label>Use Time Frame</label>
      </div>

      {/* Date Inputs (inline, full width, labels above) */}
      {dateRangeReq && (
        <div className="flex flex-row gap-3 w-full">
          {/* Start Date */}
          <div className="flex flex-col w-full">
            <label className="mb-2">Start</label>
            <Calendar
              value={beginDate}
              onChange={e => updateBegin(e.value as Date)}
              showIcon
              className="w-full"
            />
          </div>

          {/* End Date */}
          <div className="flex flex-col w-full">
            <label className="mb-2">End</label>
            <Calendar
              value={endDate}
              onChange={e => updateEnd(e.value as Date)}
              showIcon
              className="w-full"
            />
          </div>
        </div>
      )}
    </div>
  );
}
