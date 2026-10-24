import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '@/app/auth/hooks/useAuth';
import { useGuestLogin } from '@/app/auth/hooks/useGuestLogin';

type Tone = 'brand' | 'credit' | 'debit';

type QuickAction = {
  label: string;
  description: string;
  icon: string;
  route: string;
  tone: Tone;
};

// Everyone (once signed in)
const USER_ACTIONS: QuickAction[] = [
  {
    label: 'Display',
    description: 'Chart and ledger for any date range',
    icon: 'pi pi-chart-bar',
    route: '/query/display',
    tone: 'brand',
  },
  {
    label: 'Initial Amount',
    description: 'Set the balance the forecast starts from',
    icon: 'pi pi-dollar',
    route: '/command/transactions/initial-amount',
    tone: 'brand',
  },
  {
    label: 'Credits',
    description: 'Income on a recurring schedule',
    icon: 'pi pi-plus-circle',
    route: '/command/transactions/credits',
    tone: 'credit',
  },
  {
    label: 'Debits',
    description: 'Bills and expenses on a recurring schedule',
    icon: 'pi pi-minus-circle',
    route: '/command/transactions/debits',
    tone: 'debit',
  },
  {
    label: 'Debt Paydown',
    description: 'Put part of your credits toward a debt and see the payoff date',
    icon: 'pi pi-credit-card',
    route: '/command/transactions/debt-paydown',
    tone: 'brand',
  },
];

// ROLE_ADMIN only
const ADMIN_ACTIONS = [
  { label: 'Item Types', icon: 'pi pi-list', route: '/command/admin/item-types' },
  { label: 'Time Periods', icon: 'pi pi-calendar', route: '/command/admin/time-periods' },
  { label: 'System Status', icon: 'pi pi-server', route: '/status' },
];

// Illustrative rows for the hero "receipt" — not the user's data
const EXAMPLE_LEDGER = [
  { date: 'Nov 01', name: 'Paycheck', amount: '+2,450.00', kind: 'credit' },
  { date: 'Nov 03', name: 'Rent', amount: '−1,600.00', kind: 'debit' },
  { date: 'Nov 05', name: 'Car payment', amount: '−385.20', kind: 'debit' },
] as const;

export function HomePage() {
  const navigate = useNavigate();
  const { hasRole, isAuthenticated } = useAuth();
  const { loginAsGuest, loading } = useGuestLogin();

  const isAdmin = hasRole('ROLE_ADMIN');

  return (
    <div className="fl-home">
      {/* Hero — continues the header band */}
      <section className="fl-band fl-hero">
        <div className="fl-container fl-hero-inner">
          <div className="fl-hero-copy">
            <span className="fl-eyebrow">Financial Planner · Forecast · Ledger</span>
            <h1 className="fl-hero-title">
              Every credit, every debit, every day — forecast ahead.
            </h1>
            <p className="fl-hero-lead">
              Model recurring income and expenses on weekly, monthly, quarterly or custom schedules
              and watch the running balance roll forward.
            </p>
            <div className="fl-hero-cta">
              {isAuthenticated ? (
                <button type="button" className="fl-cta" onClick={() => navigate('/query/display')}>
                  Open forecast <i className="pi pi-arrow-right" aria-hidden="true" />
                </button>
              ) : (
                <button type="button" className="fl-cta" onClick={loginAsGuest} disabled={loading}>
                  {loading ? 'Signing in…' : 'Try it as a guest'}
                </button>
              )}
              <button
                type="button"
                className="fl-cta fl-cta--ghost"
                onClick={() => navigate('/docs')}
              >
                How it works
              </button>
            </div>
          </div>

          <div className="fl-receipt" aria-label="Example ledger">
            <div className="fl-receipt-head">
              <span>Ledger preview</span>
              <span>Example</span>
            </div>
            {EXAMPLE_LEDGER.map(row => (
              <div key={row.date} className="fl-receipt-row">
                <span className="fl-mono fl-muted">{row.date}</span>
                <span>{row.name}</span>
                <span className={`fl-mono fl-${row.kind}`}>{row.amount}</span>
              </div>
            ))}
            <div className="fl-receipt-total">
              <span>Balance</span>
              <span className="fl-mono">7,204.75</span>
            </div>
          </div>
        </div>
      </section>

      {/* Actions */}
      <div className="fl-container fl-home-body">
        <section className="fl-home-main">
          <h2 className="fl-section-label">Get to work</h2>
          {!isAuthenticated && (
            <p className="fl-signin-hint">
              <i className="pi pi-lock" aria-hidden="true" /> Log in, or try it as a guest, to open
              these.
            </p>
          )}
          <div className="fl-list">
            {USER_ACTIONS.map(a => (
              <Link key={a.route} to={a.route} className="fl-list-row">
                <span className={`fl-list-icon fl-tone-${a.tone}`}>
                  <i className={a.icon} aria-hidden="true" />
                </span>
                <span className="fl-list-text">
                  <span className="fl-list-title">{a.label}</span>
                  <span className="fl-list-desc">{a.description}</span>
                </span>
                <i className="pi pi-angle-right fl-list-chevron" aria-hidden="true" />
              </Link>
            ))}
          </div>
        </section>

        <aside className="fl-home-aside">
          {isAdmin && (
            <>
              <h2 className="fl-section-label">Admin</h2>
              <div className="fl-list fl-list--compact">
                {ADMIN_ACTIONS.map(a => (
                  <Link key={a.route} to={a.route} className="fl-list-row">
                    <i className={`${a.icon} fl-muted`} aria-hidden="true" />
                    <span className="fl-list-title">{a.label}</span>
                  </Link>
                ))}
              </div>
            </>
          )}

          <div className="fl-note">
            <span className="fl-note-title">New here?</span>
            <span className="fl-note-body">
              The documentation walks through schedules, the ledger and the chart.
            </span>
            <Link to="/docs" className="fl-note-link">
              Open the docs →
            </Link>
          </div>
        </aside>
      </div>
    </div>
  );
}
