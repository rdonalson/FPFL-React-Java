// src/app/providers/AppErrorBoundary.tsx
import React from 'react';
import { apiConfig } from '@/api/config';

export class AppErrorBoundary extends React.Component<
  { children: React.ReactNode },
  { hasError: boolean }
> {
  constructor(props: any) {
    super(props);
    this.state = { hasError: false };
  }

  componentDidCatch(error: any, info: any) {
    console.log('🔥 UI ERROR BOUNDARY FIRED', { error, info });

    fetch(`${apiConfig.baseUrl}/client-logs`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        level: 'error',
        url: 'UI_RENDER',
        status: 0,
        message: error?.message ?? 'UI render error',
        correlationId: crypto.randomUUID(),
        details: { componentStack: info?.componentStack },
      }),
    }).catch(e => console.log('🔥 UI LOGGING FAILED', e));

    this.setState({ hasError: true });
  }

  render() {
    if (this.state.hasError) {
      return <div className="error-boundary">Something went wrong in the UI.</div>;
    }

    return this.props.children;
  }
}
