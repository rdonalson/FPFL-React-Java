import type { CSSProperties } from 'react';
import { Card } from 'primereact/card';
import { Button, type ButtonProps } from 'primereact/button';
import { Panel } from 'primereact/panel';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '@/app/auth/hooks/useAuth';
//import './homepage.css';

type QuickAction = {
  label: string;
  icon: string;
  route: string;
  severity?: ButtonProps['severity'];
};

// Row 1 — everyone can see
const USER_ACTIONS: QuickAction[] = [
  { label: 'Display', icon: 'pi pi-chart-bar', route: '/query/display' },
  { label: 'Initial Amount', icon: 'pi pi-dollar', route: '/command/transactions/initial-amount' },
  { label: 'Credits', icon: 'pi pi-plus-circle', route: '/command/transactions/credits' },
  { label: 'Debits', icon: 'pi pi-minus-circle', route: '/command/transactions/debits' },
  { label: 'View Documentation', icon: 'pi pi-book', route: '/docs', severity: 'info' },
];

// Row 2 — ROLE_ADMIN only
const ADMIN_ACTIONS: QuickAction[] = [
  { label: 'Manage Item Types', icon: 'pi pi-list', route: '/command/admin/item-types' },
  { label: 'Manage Time Periods', icon: 'pi pi-calendar', route: '/command/admin/time-periods' },
  { label: 'Check API Status', icon: 'pi pi-server', route: '/status', severity: 'secondary' },
];

export function HomePage() {
  const navigate = useNavigate();
  const { hasRole } = useAuth();

  const isAdmin = hasRole('ROLE_ADMIN');

  // Both rows share the same column count, so every button is the same width.
  const columns = Math.max(USER_ACTIONS.length, isAdmin ? ADMIN_ACTIONS.length : 0);
  const gridStyle = { '--qa-cols': columns } as CSSProperties;

  const renderRow = (actions: QuickAction[]) => (
    <div className="qa-grid" style={gridStyle}>
      {actions.map((a) => (
        <Button
          key={a.route + a.label}
          label={a.label}
          icon={a.icon}
          severity={a.severity}
          onClick={() => navigate(a.route)}
        />
      ))}
    </div>
  );

  return (
    <div className="p-4 flex flex-column gap-4">
      {/* Hero Section */}
      <Card className="shadow-2">
        <h1 className="text-3xl font-bold mb-2">Welcome to UI-TSX</h1>
        <p className="text-lg text-color-secondary mb-3">
          A modular, enterprise‑grade React + PrimeReact workspace designed for clarity,
          scalability, and developer happiness.
        </p>

        <Button label="Get Started" icon="pi pi-arrow-right" onClick={() => navigate('/docs')} />
      </Card>

      {/* Quick Actions */}
      <Panel header="Quick Actions" className="shadow-1">
        {renderRow(USER_ACTIONS)}

        {isAdmin && (
          <>
            <h3 className="qa-section-title">Admin</h3>
            {renderRow(ADMIN_ACTIONS)}
          </>
        )}
      </Panel>
    </div>
  );
}
