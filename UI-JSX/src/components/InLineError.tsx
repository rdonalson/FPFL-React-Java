import { useEffect } from "react";
import { useToast } from "@/shared/hooks/useToast";

interface InlineErrorProps {
  status?: number;
  message: string;
}

export function InlineError({ status, message }: InlineErrorProps) {
  const toast = useToast();

  useEffect(() => {
    toast.current?.show({
      severity: "error",
      summary: status ? `Error ${status}` : "Error",
      detail: message,
      life: 4000,
    });
  }, [status, message]);

  return (
    <div className="inline-error">
      {status && <span className="inline-error-status">{status}:</span>} {message}
    </div>
  );
}
