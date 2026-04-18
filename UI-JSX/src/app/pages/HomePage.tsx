import { Card } from 'primereact/card';
import { Button } from 'primereact/button';
import { Panel } from 'primereact/panel';
import { useNavigate } from 'react-router-dom';

export function HomePage() {
  const navigate = useNavigate();

  return (
    <div className="p-4 flex flex-column gap-4">
      {/* Hero Section */}
      <Card className="shadow-2">
        <h1 className="text-3xl font-bold mb-2">Welcome to UI‑JSX</h1>
        <p className="text-lg text-color-secondary mb-3">
          A modular, enterprise‑grade React + PrimeReact workspace designed for clarity,
          scalability, and developer happiness.
        </p>

        <Button label="Get Started" icon="pi pi-arrow-right" onClick={() => navigate('/docs')} />
      </Card>

      {/* Quick Actions */}
      <Panel header="Quick Actions" className="shadow-1">
        <div className="flex flex-column gap-3">
          <Button
            label="Manage Item Types"
            icon="pi pi-list"
            onClick={() => navigate('/command/admin/item-types')}
          />
          <Button
            label="Manage Time Periods"
            icon="pi pi-list"
            onClick={() => navigate('/command/admin/time-periods')}
          />
          <Button
            label="Check API Status"
            icon="pi pi-server"
            severity="secondary"
            onClick={() => navigate('/status')}
          />

          <Button
            label="View Documentation"
            icon="pi pi-book"
            severity="info"
            onClick={() => navigate('/docs')}
          />
        </div>
      </Panel>
    </div>
  );
}
