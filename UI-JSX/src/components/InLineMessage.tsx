// src/shared/components/InlineError.tsx
import { Message } from 'primereact/message';
type InlineSeverity = 'error' | 'warn' | 'info' | 'success';

interface InlineErrorProps {
  severity: InlineSeverity;
  status?: number;
  message: string;
  className?: string;
}

export function InLineMessage({ severity, status, message, className }: InlineErrorProps) {
  return (
    <Message
      severity={severity}
      text={`${status ? `${status}: ` : ''}${message}`}
      className={className ?? 'mt-2'}
    />
  );
}
