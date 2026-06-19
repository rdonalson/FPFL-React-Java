// src/app/providers/AppErrorBoundary.tsx
import React from 'react';
import { AppConfig } from '@/config/appConfig';

export class AppErrorBoundary extends React.Component<
  { children: React.ReactNode },
  { hasError: boolean }
> {
  constructor(props: any) {
    super(props);
    this.state = { hasError: false };
  }

  componentDidCatch(error: any, info: any) {
    // Log locally
    // eslint-disable-next-line no-console
    console.error('🔥 UI ERROR BOUNDARY FIRED', { error, info });

    // Fire-and-forget remote logging using runtime-configured API base URL
    try {
      const baseUrl = AppConfig.get().api.baseUrl.replace(/\/$/, '');
      void fetch(`${baseUrl}/client-logs`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          level: 'error',
          url: 'UI_RENDER',
          status: 0,
          message: error?.message ?? 'UI render error',
          correlationId: crypto.randomUUID(),
          details: { componentStack: info?.componentStack },
          env: AppConfig.get().app.nodeEnv,
        }),
      }).catch(e => {
        // eslint-disable-next-line no-console
        console.warn('🔥 UI LOGGING FAILED', e);
      });
    } catch (e) {
      // eslint-disable-next-line no-console
      console.warn('🔥 UI LOGGING SKIPPED', e);
    }

    this.setState({ hasError: true });
  }

  render() {
    if (this.state.hasError) {
      const title = AppConfig.get().app.title;
      return (
        <div className="min-h-screen flex items-center justify-center p-6">
          <div className="max-w-lg w-full bg-white dark:bg-gray-900 border rounded-lg p-6 shadow">
            <h1 className="text-xl font-semibold mb-2">Something went wrong</h1>
            <p className="text-sm mb-4">
              An unexpected error occurred while rendering the application. The team has been
              notified.
            </p>
            <p className="text-xs text-muted">
              If the problem persists, try refreshing the page or contact support.
            </p>
            <div className="mt-4 text-xs text-right text-muted">{title}</div>
          </div>
        </div>
      );
    }

    return this.props.children;
  }
}
