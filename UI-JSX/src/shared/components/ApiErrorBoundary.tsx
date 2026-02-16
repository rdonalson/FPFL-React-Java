// src/shared/components/ApiErrorBoundary.tsx
import { InlineError } from '@/components/InLineError';

interface ApiErrorBoundaryProps {
  error: any;
  children: React.ReactNode;
}

export function ApiErrorBoundary({ error, children }: ApiErrorBoundaryProps) {
  if (!error) {
    return <>{children}</>;
  }

  const status = error.status ?? 0;

  // ❗ Inline errors (400–499)
  if (status > 0 && status < 500) {
    return (
      <div className="api-error-inline">
        <InlineError status={status} message={error.message} />
      </div>
    );
  }

  // 🔥 Server errors (500+ or network) are handled globally by QueryProvider
  return null;
}
