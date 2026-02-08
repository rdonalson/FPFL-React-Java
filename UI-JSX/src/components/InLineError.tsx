// src/components/InlineError.tsx
interface InlineErrorProps {
    status?: number; // Optional HTTP status code for more context
    message: string;
}

export function InlineError({ status, message }: InlineErrorProps) {
  return (
    <div
      style={{
        padding: "0.75rem 1rem",
        background: "#ffe6e6",
        border: "1px solid #ffb3b3",
        color: "#b30000",
        borderRadius: "4px",
        marginBottom: "1rem",
      }}
    >
      {status && <span style={{ fontWeight: "bold" }}>{status}:</span>} {message}
    </div>
  );
}
