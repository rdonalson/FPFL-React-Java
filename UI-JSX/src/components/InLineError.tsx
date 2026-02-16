interface InlineErrorProps {
  status?: number;
  message: string;
}

export function InlineError({ status, message }: InlineErrorProps) {
  return (
    <div className="inline-error">
      {status && <span className="inline-error-status">{status}:</span>} {message}
    </div>
  );
}
