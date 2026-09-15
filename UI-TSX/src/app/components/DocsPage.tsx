// src/app/components/DocsPage.tsx
import { useState } from 'react';
import { version as reactVersion } from 'react';
import { useNavigate } from 'react-router-dom';
import * as RadixTabs from '@radix-ui/react-tabs';
import { Card } from 'primereact/card';
import { Panel } from 'primereact/panel';
import { Button } from 'primereact/button';
import { Tag } from 'primereact/tag';
import { DataTable } from 'primereact/datatable';
import { Column } from 'primereact/column';
import { Divider } from 'primereact/divider';
import { AppConfig } from '@/config/appConfig';

/* ------------------------------------------------------------------ */
/*  Static reference data                                              */
/* ------------------------------------------------------------------ */

const REPO_URL = 'https://github.com/rdonalson/FPFL-React-Java';

type TabKey = 'overview' | 'architecture' | 'ledger' | 'data' | 'cicd';

type TabDef = { key: TabKey; label: string; icon: string };

const TABS: TabDef[] = [
  { key: 'overview', label: 'Overview', icon: 'pi pi-home' },
  { key: 'architecture', label: 'Architecture', icon: 'pi pi-sitemap' },
  { key: 'ledger', label: 'Ledger Engine', icon: 'pi pi-calendar' },
  { key: 'data', label: 'Data Model', icon: 'pi pi-database' },
  { key: 'cicd', label: 'CI/CD', icon: 'pi pi-cloud-upload' },
];

type LinkRow = { label: string; description: string; href: string };

const REPO_LINKS: LinkRow[] = [
  {
    label: 'Repository root',
    description: 'Parent project — both applications, k8s manifests, CI/CD workflows',
    href: REPO_URL,
  },
  {
    label: 'UI-TSX',
    description: 'This application — React 19 + TypeScript on Vite',
    href: `${REPO_URL}/tree/main/UI-TSX`,
  },
  {
    label: 'API-JDK',
    description: 'Java 25 / Spring Boot 4 multi-module API',
    href: `${REPO_URL}/tree/main/API-JDK`,
  },
  {
    label: 'Kubernetes manifests',
    description: 'Deployments, services, ingress, cert-manager issuer',
    href: `${REPO_URL}/tree/main/k8s`,
  },
];

type StackRow = { area: string; choice: string };

const UI_STACK: StackRow[] = [
  { area: 'Framework', choice: 'React 19 + TypeScript (React Compiler enabled)' },
  { area: 'Build tool', choice: 'Vite 7' },
  { area: 'Components', choice: 'PrimeReact · PrimeFlex · PrimeIcons · Radix primitives' },
  { area: 'Styling', choice: 'Tailwind CSS + tailwindcss-primeui · SCSS overrides' },
  { area: 'Server state', choice: 'TanStack Query' },
  { area: 'Tables', choice: 'TanStack Table' },
  { area: 'Client state', choice: 'Zustand (session + theme)' },
  { area: 'Routing', choice: 'React Router 7' },
  { area: 'Forms', choice: 'React Hook Form + Zod' },
  { area: 'HTTP', choice: 'Axios with centralized interceptors' },
  { area: 'Charting', choice: 'Chart.js' },
];

const API_STACK: StackRow[] = [
  { area: 'Language', choice: 'Java 25' },
  { area: 'Framework', choice: 'Spring Boot 4.0.x · Spring Security · Spring Data JPA' },
  { area: 'Build', choice: 'Maven (multi-module reactor)' },
  { area: 'API docs', choice: 'springdoc-openapi (Swagger UI)' },
  { area: 'Auth', choice: 'JJWT — access tokens + persisted refresh tokens' },
  { area: 'Database', choice: 'PostgreSQL 16+' },
];

type ModuleRow = { module: string; role: string };

const API_MODULES: ModuleRow[] = [
  {
    module: 'module-api',
    role: 'REST controllers, DTOs, mappers, security filters, OpenAPI, global exception handling',
  },
  {
    module: 'module-items-bc',
    role: 'Write-side domain logic and persistence for items, item types, time periods',
  },
  {
    module: 'module-display-bc',
    role: 'Read-side projections — recurrence expansion into the forecasted ledger',
  },
  { module: 'module-auth', role: 'Users, roles, authentication, refresh tokens' },
  {
    module: 'module-common-bc',
    role: 'Shared converters, exceptions, sanitization, error logging',
  },
];

type ExpanderRow = { periodId: number; expander: string; pattern: string };

const EXPANDERS: ExpanderRow[] = [
  { periodId: 1, expander: 'OneTimeOccurrenceExpander', pattern: 'Single-date item' },
  { periodId: 2, expander: 'DailyRecurrenceExpander', pattern: 'Every day in range' },
  { periodId: 3, expander: 'WeeklyRecurrenceExpander', pattern: 'Weekly on a chosen weekday' },
  { periodId: 4, expander: 'BiWeeklyRecurrenceExpander', pattern: 'Every other week' },
  { periodId: 5, expander: 'BiMonthlyRecurrenceExpander', pattern: 'Two days per month' },
  { periodId: 6, expander: 'MonthlyRecurrenceExpander', pattern: 'Monthly on a chosen day' },
  { periodId: 7, expander: 'QuarterlyRecurrenceExpander', pattern: 'Quarterly' },
  { periodId: 8, expander: 'SemiAnnualRecurrenceExpander', pattern: 'Twice yearly' },
  { periodId: 9, expander: 'AnnualRecurrenceExpander', pattern: 'Yearly' },
  {
    periodId: 10,
    expander: 'NthWeekdayRecurrenceExpander',
    pattern: '"Nth weekday of the month" patterns',
  },
];

type EntityRow = { entity: string; group: 'Domain' | 'Auth'; description: string };

const ENTITIES: EntityRow[] = [
  {
    entity: 'Item',
    group: 'Domain',
    description: 'User-defined financial item — amount, date bounds, recurrence metadata',
  },
  {
    entity: 'ItemType',
    group: 'Domain',
    description: 'Category of item — drives the credit / debit classification',
  },
  {
    entity: 'TimePeriod',
    group: 'Domain',
    description: 'Recurrence period; selects which recurrence columns an item uses',
  },
  { entity: 'User', group: 'Auth', description: 'Application account' },
  {
    entity: 'Role / UserRoles',
    group: 'Auth',
    description: 'Role assignments (composite key via UserRolesId)',
  },
  {
    entity: 'RefreshToken',
    group: 'Auth',
    description: 'Persisted refresh tokens for session renewal',
  },
];

type PipelineRow = { step: string; detail: string };

const PIPELINE_STEPS: PipelineRow[] = [
  {
    step: '1 · Trigger',
    detail:
      'Push to main touching that application’s paths (UI-TSX/** or API-JDK/**, plus its k8s manifests), or a manual workflow_dispatch',
  },
  {
    step: '2 · Build',
    detail:
      'Docker Buildx builds the multi-stage image and pushes two tags to Azure Container Registry: the commit SHA and :latest',
  },
  {
    step: '3 · Authenticate',
    detail: 'Azure login with a service principal, then set the AKS context for fpfl-cluster',
  },
  {
    step: '4 · Apply',
    detail: 'kubectl apply of the service, deployment, and ingress manifests for that application',
  },
  {
    step: '5 · Roll out',
    detail:
      'kubectl rollout restart forces the new :latest image to be pulled; rollout status fails the run if the deployment does not become healthy',
  },
];

/* ------------------------------------------------------------------ */
/*  Small presentational helpers                                       */
/* ------------------------------------------------------------------ */

const TRIGGER_CLASS =
  'fpfl-tab flex items-center gap-2 px-3 py-2 rounded-md font-medium ' +
  'data-[state=active]:text-sky-700 data-[state=active]:font-semibold ' +
  'data-[state=inactive]:text-gray-500';

const codeText = (value: string) => <code className="text-sm white-space-nowrap">{value}</code>;

function SectionHeading({ title, blurb }: { title: string; blurb: string }) {
  return (
    <div className="mb-3">
      <h2 className="text-xl font-bold m-0 mb-1">{title}</h2>
      <p className="text-color-secondary m-0">{blurb}</p>
    </div>
  );
}

/* ------------------------------------------------------------------ */
/*  Page                                                               */
/* ------------------------------------------------------------------ */

export default function DocsPage() {
  const navigate = useNavigate();
  const cfg = AppConfig.get();
  const [activeTab, setActiveTab] = useState<TabKey>('overview');

  const linkBody = (row: LinkRow) => (
    <a href={row.href} target="_blank" rel="noreferrer noopener">
      {row.label}
      <i className="pi pi-external-link ml-2 text-xs" />
    </a>
  );

  const groupBody = (row: EntityRow) => (
    <Tag value={row.group} severity={row.group === 'Auth' ? 'warning' : 'info'} />
  );

  return (
    <div className="docs-page p-4 flex flex-column gap-4">
      <div>
        <Button
          label="Back to Home"
          icon="pi pi-arrow-left"
          className="p-button-text"
          onClick={() => navigate('/')}
        />
      </div>

      {/* ── Hero ── */}
      <Card className="shadow-2">
        <div className="flex flex-wrap align-items-start justify-content-between gap-3">
          <div>
            <h1 className="text-3xl font-bold m-0 mb-2">FPFL Platform Documentation</h1>
            <p className="text-lg text-color-secondary m-0">
              A full-stack financial planning system: a React 19 / TypeScript UI, a Java 25 / Spring
              Boot 4 API, and a PostgreSQL database — containerized with Docker and deployed to
              Azure Kubernetes Service through GitHub Actions.
            </p>
          </div>

          <Button
            label="View on GitHub"
            icon="pi pi-github"
            severity="secondary"
            outlined
            onClick={() => window.open(REPO_URL, '_blank', 'noopener')}
          />
        </div>

        <Divider />

        <div className="flex flex-wrap gap-4 text-sm text-color-secondary">
          <span>
            <i className="pi pi-desktop mr-1" />
            UI build: {import.meta.env.MODE} · React {reactVersion}
          </span>
          <span>
            <i className="pi pi-link mr-1" />
            API: {cfg.api.baseUrl}
          </span>
          <span>
            <i className="pi pi-clock mr-1" />
            Request timeout: {cfg.api.timeoutMs} ms
          </span>
        </div>
      </Card>

      {/* ── Sections ── */}
      <Card className="shadow-1">
        <div className="pr-tabs">
          <RadixTabs.Root
            value={activeTab}
            onValueChange={(val: string) => setActiveTab((val as TabKey) ?? 'overview')}
            className="max-w-full"
          >
            <RadixTabs.List
              className="fpfl-tabs-list flex flex-wrap gap-4 border-b pb-2"
              aria-label="Documentation sections"
            >
              {TABS.map(tab => (
                <RadixTabs.Trigger key={tab.key} value={tab.key} className={TRIGGER_CLASS}>
                  <i className={tab.icon} />
                  {tab.label}
                </RadixTabs.Trigger>
              ))}
            </RadixTabs.List>

            <div className="pt-4">
              {/* ---------------------------------------------------- */}
              <RadixTabs.Content value="overview" className="outline-none">
                <SectionHeading
                  title="What this system does"
                  blurb="Store financial items, project them forward, and read the result as a running-balance ledger."
                />

                <p>
                  You define <strong>credits</strong> and <strong>debits</strong> — a salary, a rent
                  payment, a subscription — each with an amount, a date range, and a recurrence
                  pattern. The API expands those definitions into concrete dated occurrences and
                  composes them with your starting balance, producing a forecasted ledger you can
                  read as a table or a chart over any date range you choose.
                </p>

                <Divider />

                <SectionHeading
                  title="Where things live"
                  blurb="The repository is a parent project holding both applications and their shared infrastructure."
                />

                <DataTable value={REPO_LINKS} size="small" stripedRows dataKey="label">
                  <Column header="Location" body={linkBody} style={{ width: '28%' }} />
                  <Column field="description" header="Contains" />
                </DataTable>

                <Divider />

                <SectionHeading
                  title="Getting around this app"
                  blurb="Everything below the Home page needs a signed-in session; admin maintenance needs ROLE_ADMIN."
                />

                <div className="flex flex-wrap gap-2">
                  <Button
                    label="Forecasted Ledger"
                    icon="pi pi-chart-bar"
                    size="small"
                    onClick={() => navigate('/query/display')}
                  />
                  <Button
                    label="Initial Amount"
                    icon="pi pi-dollar"
                    size="small"
                    onClick={() => navigate('/command/transactions/initial-amount')}
                  />
                  <Button
                    label="Credits"
                    icon="pi pi-plus-circle"
                    size="small"
                    onClick={() => navigate('/command/transactions/credits')}
                  />
                  <Button
                    label="Debits"
                    icon="pi pi-minus-circle"
                    size="small"
                    onClick={() => navigate('/command/transactions/debits')}
                  />
                </div>
              </RadixTabs.Content>

              {/* ---------------------------------------------------- */}
              <RadixTabs.Content value="architecture" className="outline-none">
                <SectionHeading
                  title="Frontend — UI-TSX"
                  blurb="Organized by feature, mirroring the backend's write/read split."
                />

                <ul className="mt-0">
                  <li>
                    {codeText('features/catalog-command')} — the write side: items, initial amount,
                    and admin maintenance of item types and time periods
                  </li>
                  <li>
                    {codeText('features/catalog-query')} — the read side: the forecasted ledger,
                    date range selection, and charting
                  </li>
                  <li>
                    {codeText('app/')} — layout, routing and guards, authentication, providers, and
                    state stores
                  </li>
                  <li>
                    {codeText('api/')} — a single Axios instance with correlation IDs, bearer-token
                    injection, and normalized errors; feature modules never call Axios directly
                  </li>
                </ul>

                <DataTable value={UI_STACK} size="small" stripedRows dataKey="area">
                  <Column field="area" header="Area" style={{ width: '30%' }} />
                  <Column field="choice" header="Choice" />
                </DataTable>

                <Divider />

                <SectionHeading
                  title="Backend — API-JDK"
                  blurb="Five Maven modules, each a bounded context. Only module-api is runnable."
                />

                <DataTable value={API_MODULES} size="small" stripedRows dataKey="module">
                  <Column
                    header="Module"
                    body={(row: ModuleRow) => codeText(row.module)}
                    style={{ width: '30%' }}
                  />
                  <Column field="role" header="Responsibility" />
                </DataTable>

                <div className="mt-3">
                  <DataTable value={API_STACK} size="small" stripedRows dataKey="area">
                    <Column field="area" header="Area" style={{ width: '30%' }} />
                    <Column field="choice" header="Choice" />
                  </DataTable>
                </div>

                <Divider />

                <SectionHeading
                  title="Security model"
                  blurb="Stateless JWT on the API, role-aware routing in the UI."
                />

                <ul className="mt-0 mb-0">
                  <li>
                    Spring Security with a stateless JWT filter chain; access tokens are paired with
                    persisted refresh tokens for rotation
                  </li>
                  <li>Passwords hashed with BCrypt</li>
                  <li>
                    Roles drive both API authorization and UI route guards — admin-only areas are
                    also filtered out of the sidebar, so the guard is a backstop rather than the only
                    defense
                  </li>
                </ul>
              </RadixTabs.Content>

              {/* ---------------------------------------------------- */}
              <RadixTabs.Content value="ledger" className="outline-none">
                <SectionHeading
                  title="How a forecast is built"
                  blurb="module-display-bc turns stored items into a date-ranged, running-balance ledger."
                />

                <p>
                  Every item carries recurrence metadata, but only the fields matching its time
                  period are populated. Each pattern has a dedicated expander that reads exactly
                  those fields and emits concrete dated occurrences within the requested range.{' '}
                  {codeText('LedgerReadoutService')} then composes those occurrences with your
                  initial amount into the running balance the Display page renders.
                </p>

                <p className="text-color-secondary text-sm">
                  The period id below is the same id used in this app&apos;s URLs — for example{' '}
                  {codeText('/command/transactions/credits/new/6')} opens the monthly add form.
                </p>

                <DataTable value={EXPANDERS} size="small" stripedRows dataKey="expander">
                  <Column field="periodId" header="Period" style={{ width: '10%' }} />
                  <Column
                    header="Expander"
                    body={(row: ExpanderRow) => codeText(row.expander)}
                    style={{ width: '40%' }}
                  />
                  <Column field="pattern" header="Pattern" />
                </DataTable>
              </RadixTabs.Content>

              {/* ---------------------------------------------------- */}
              <RadixTabs.Content value="data" className="outline-none">
                <SectionHeading
                  title="PostgreSQL entities"
                  blurb="Schema management runs through Hibernate; a full breakdown lives in the API README."
                />

                <DataTable value={ENTITIES} size="small" stripedRows dataKey="entity">
                  <Column
                    header="Entity"
                    body={(row: EntityRow) => codeText(row.entity)}
                    style={{ width: '25%' }}
                  />
                  <Column header="Group" body={groupBody} style={{ width: '15%' }} />
                  <Column field="description" header="Description" />
                </DataTable>

                <Divider />

                <SectionHeading title="Relationships" blurb="How the tables hang together." />

                <ul className="mt-0 mb-0">
                  <li>
                    {codeText('items.fk_item_type')} → {codeText('item_types.id')}
                  </li>
                  <li>
                    {codeText('items.fk_time_period')} → {codeText('time_periods.id')}
                  </li>
                  <li>
                    {codeText('items.user_id')} → {codeText('users.userID')} (ownership; UUID)
                  </li>
                  <li>
                    {codeText('user_roles')} joins {codeText('users')} and {codeText('roles')}
                  </li>
                  <li>
                    {codeText('refresh_tokens.user_id')} → {codeText('users.id')}
                  </li>
                </ul>
              </RadixTabs.Content>

              {/* ---------------------------------------------------- */}
              <RadixTabs.Content value="cicd" className="outline-none">
                <SectionHeading
                  title="From push to pod"
                  blurb="GitHub Actions → Azure Container Registry → Azure Kubernetes Service."
                />

                <p>
                  Each application has its own workflow with path filters, so a change to one side
                  never rebuilds or redeploys the other.
                </p>

                <DataTable value={PIPELINE_STEPS} size="small" stripedRows dataKey="step">
                  <Column field="step" header="Step" style={{ width: '20%' }} />
                  <Column field="detail" header="What happens" />
                </DataTable>

                <Divider />

                <SectionHeading
                  title="Cluster layout"
                  blurb="Both applications share one namespace behind an nginx ingress."
                />

                <ul className="mt-0">
                  <li>
                    Namespace {codeText('fpfl')} — deployments {codeText('api')} (port 8080) and{' '}
                    {codeText('ui')} (port 80)
                  </li>
                  <li>nginx ingress routes each public hostname to its service</li>
                  <li>
                    TLS issued by cert-manager through the {codeText('letsencrypt-prod')}{' '}
                    ClusterIssuer
                  </li>
                  <li>
                    Database credentials come from a Kubernetes secret, never from the repository
                  </li>
                </ul>

                <Panel
                  header="A note on build-time configuration"
                  toggleable
                  collapsed
                  className="mt-3"
                >
                  <p className="m-0">
                    Vite inlines {codeText('VITE_*')} values when the bundle is built, not when the
                    container starts — so the UI workflow passes them to Docker as build args.
                    Changing an API URL means rebuilding the image. For values that must change
                    without a rebuild, the app also reads an optional runtime{' '}
                    {codeText('/config.json')} at startup and merges it over the build-time
                    defaults.
                  </p>
                </Panel>
              </RadixTabs.Content>
            </div>
          </RadixTabs.Root>
        </div>
      </Card>
    </div>
  );
}
