// src/app/components/StatusPage.tsx
import { useCallback, useEffect, useState } from 'react';
import { version as reactVersion } from 'react';
import { useLocation } from 'react-router-dom';
import { Card } from 'primereact/card';
import { Panel } from 'primereact/panel';
import { Button } from 'primereact/button';
import { Tag } from 'primereact/tag';
import { Message } from 'primereact/message';
import { DataTable } from 'primereact/datatable';
import { Column } from 'primereact/column';
import { ProgressSpinner } from 'primereact/progressspinner';
import { useSessionStore } from '@/app/state/sessionStore';
import { useAuth } from '@/app/auth/hooks/useAuth';
import {
  API_BASE_URL,
  HEALTH_URL,
  fetchHealth,
  fetchInfo,
  type ActuatorHealth,
  type ActuatorInfo,
  type HealthStatus,
  type ProbeResult,
} from '@/api/status/healthApi';

const POLL_INTERVAL_MS = 30_000;
const APP_STARTED_AT = Date.now();

type CheckRow = { name: string; status: HealthStatus; detail: string };

function statusSeverity(status: HealthStatus) {
  switch (status) {
    case 'UP':
      return 'success' as const;
    case 'DOWN':
    case 'OUT_OF_SERVICE':
      return 'danger' as const;
    default:
      return 'warning' as const;
  }
}

function formatDuration(ms: number) {
  const totalSeconds = Math.floor(ms / 1000);
  const hours = Math.floor(totalSeconds / 3600);
  const minutes = Math.floor((totalSeconds % 3600) / 60);
  const seconds = totalSeconds % 60;

  if (hours) return `${hours}h ${minutes}m`;
  if (minutes) return `${minutes}m ${seconds}s`;
  return `${seconds}s`;
}

function formatDetails(details?: Record<string, unknown>) {
  if (!details) return '—';

  return Object.entries(details)
    .map(
      ([key, value]) =>
        `${key}: ${typeof value === 'object' ? JSON.stringify(value) : String(value)}`,
    )
    .join(' · ');
}

export default function StatusPage() {
  const location = useLocation();
  const { userId } = useSessionStore();
  const { roles } = useAuth();

  const [health, setHealth] = useState<ProbeResult<ActuatorHealth> | null>(null);
  const [info, setInfo] = useState<ProbeResult<ActuatorInfo> | null>(null);
  const [loading, setLoading] = useState(true);
  const [autoRefresh, setAutoRefresh] = useState(true);
  const [online, setOnline] = useState(navigator.onLine);

  const refresh = useCallback(async () => {
    setLoading(true);
    const [healthResult, infoResult] = await Promise.all([fetchHealth(), fetchInfo()]);
    setHealth(healthResult);
    setInfo(infoResult);
    setLoading(false);
  }, []);

  // Initial load + polling
  useEffect(() => {
    void refresh();

    if (!autoRefresh) return;

    const id = window.setInterval(() => void refresh(), POLL_INTERVAL_MS);
    return () => window.clearInterval(id);
  }, [refresh, autoRefresh]);

  // Browser connectivity
  useEffect(() => {
    const update = () => setOnline(navigator.onLine);
    window.addEventListener('online', update);
    window.addEventListener('offline', update);
    return () => {
      window.removeEventListener('online', update);
      window.removeEventListener('offline', update);
    };
  }, []);

  const apiStatus: HealthStatus =
    health?.body?.status ??
    (health?.ok ? 'UP' : health?.blocked ? 'BLOCKED' : health ? 'DOWN' : 'UNKNOWN');
  const buildInfo = info?.body?.build ?? info?.body?.app;

  const uiChecks: CheckRow[] = [
    { name: 'UI rendering', status: 'UP', detail: `React ${reactVersion}` },
    {
      name: 'Browser connection',
      status: online ? 'UP' : 'DOWN',
      detail: online ? 'Online' : 'Offline — no network',
    },
    {
      name: 'Session',
      status: userId ? 'UP' : 'UNKNOWN',
      detail: userId ? `User ${userId} · ${roles?.join(', ') || 'no roles'}` : 'Not signed in',
    },
    { name: 'Router', status: 'UP', detail: location.pathname },
    {
      name: 'API base URL',
      status: API_BASE_URL ? 'UP' : 'UNKNOWN',
      detail: API_BASE_URL || 'same origin (dev proxy)',
    },
    {
      name: 'Health endpoint',
      status: health ? (health.ok ? 'UP' : 'DOWN') : 'UNKNOWN',
      detail: HEALTH_URL,
    },
    {
      name: 'UI build',
      status: 'UP',
      detail: `${import.meta.env.MODE}${import.meta.env.VITE_APP_VERSION ? ` · v${import.meta.env.VITE_APP_VERSION}` : ''}`,
    },
    { name: 'Session uptime', status: 'UP', detail: formatDuration(Date.now() - APP_STARTED_AT) },
  ];

  const apiComponents: CheckRow[] = Object.entries(health?.body?.components ?? {}).map(
    ([name, component]) => ({
      name,
      status: component.status,
      detail: formatDetails(component.details),
    }),
  );

  const statusBody = (row: CheckRow) => (
    <Tag value={row.status} severity={statusSeverity(row.status)} />
  );

  const headerRight = (
    <div className="flex align-items-center gap-2">
      {loading && <ProgressSpinner style={{ width: '1.5rem', height: '1.5rem' }} strokeWidth="6" />}
      <span className="text-sm text-color-secondary">
        {health ? `Checked ${health.checkedAt.toLocaleTimeString()}` : 'Checking…'}
      </span>
      <Button
        icon={autoRefresh ? 'pi pi-pause' : 'pi pi-play'}
        label={autoRefresh ? 'Auto 30s' : 'Paused'}
        severity="secondary"
        outlined
        size="small"
        onClick={() => setAutoRefresh(v => !v)}
      />
      <Button
        icon="pi pi-refresh"
        label="Refresh"
        size="small"
        loading={loading}
        onClick={() => void refresh()}
      />
    </div>
  );

  return (
    // Width is controlled in StatusPage.css (--status-page-max-width).
    <div className="status-page p-4 flex flex-column gap-4">
      <div className="flex flex-wrap align-items-center justify-content-between gap-3">
        <h1 className="text-3xl font-bold m-0">System Status</h1>
        {headerRight}
      </div>

      {health && !health.ok && (
        <Message
          severity={health.blocked ? 'warn' : 'error'}
          className="w-full"
          content={
            <div className="flex flex-column gap-1">
              <span className="font-bold">
                {health.blocked
                  ? 'API is reachable, but this page cannot read its health'
                  : `API check failed: ${health.error ?? 'no response'}`}
              </span>
              <span className="text-sm">Probed {health.url}</span>
              {health.hint && <span className="text-sm">{health.hint}</span>}
            </div>
          }
        />
      )}

      <div className="status-cards">
        {/* ── UI health ── */}
        <div>
          <Card className="h-full shadow-1">
            <div className="flex align-items-center justify-content-between mb-3">
              <h2 className="text-xl font-bold m-0">UI</h2>
              <Tag value={online ? 'UP' : 'DOWN'} severity={online ? 'success' : 'danger'} />
            </div>

            <DataTable value={uiChecks} size="small" stripedRows dataKey="name">
              <Column field="name" header="Check" style={{ width: '35%' }} />
              <Column header="Status" body={statusBody} style={{ width: '20%' }} />
              <Column field="detail" header="Detail" />
            </DataTable>
          </Card>
        </div>

        {/* ── API health ── */}
        <div>
          <Card className="h-full shadow-1">
            <div className="flex align-items-center justify-content-between mb-3">
              <h2 className="text-xl font-bold m-0">API</h2>
              <Tag value={apiStatus} severity={statusSeverity(apiStatus)} />
            </div>

            <div className="flex flex-wrap gap-4 mb-3 text-sm text-color-secondary">
              <span>
                <i className="pi pi-clock mr-1" />
                {health ? `${health.latencyMs} ms` : '—'}
              </span>
              <span>
                <i className="pi pi-server mr-1" />
                {health?.httpStatus ? `HTTP ${health.httpStatus}` : 'no response'}
              </span>
              <span className="text-xs" title={HEALTH_URL}>
                <i className="pi pi-link mr-1" />
                {HEALTH_URL}
              </span>
              {buildInfo?.version && (
                <span>
                  <i className="pi pi-tag mr-1" />v{buildInfo.version}
                </span>
              )}
              {info?.body?.build?.time && (
                <span>
                  <i className="pi pi-calendar mr-1" />
                  {new Date(info.body.build.time).toLocaleString()}
                </span>
              )}
            </div>

            {apiComponents.length > 0 ? (
              <DataTable value={apiComponents} size="small" stripedRows dataKey="name">
                <Column field="name" header="Component" style={{ width: '35%' }} />
                <Column header="Status" body={statusBody} style={{ width: '20%' }} />
                <Column field="detail" header="Detail" />
              </DataTable>
            ) : (
              <Message
                severity="info"
                text={
                  health?.ok
                    ? 'No component detail returned. Set management.endpoint.health.show-details=always to see the database and disk checks.'
                    : 'No component detail available while the API is unreachable.'
                }
              />
            )}
          </Card>
        </div>
      </div>

      {/* ── Raw payloads, collapsed ── */}
      <Panel header="Raw actuator response" toggleable collapsed className="shadow-1">
        <pre className="text-sm overflow-auto m-0">
          {JSON.stringify(
            { health: health?.body ?? health?.error, info: info?.body ?? info?.error },
            null,
            2,
          )}
        </pre>
      </Panel>
    </div>
  );
}
