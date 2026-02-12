// src/components/InlineError.tsx
interface InlineErrorProps {
  status?: number; // Optional HTTP status code for more context
  message: string;
}

export function InlineError({ status, message }: InlineErrorProps) {
  return (
    <div className="inline-error">
      {status && <span className="inline-error-status">{status}:</span>} {message}
    </div>
  );
}
