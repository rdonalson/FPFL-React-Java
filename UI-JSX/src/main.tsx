// src/main.tsx
import React from 'react';
import ReactDOM from 'react-dom/client';
import { HelmetProvider } from 'react-helmet-async';
import App from './app/App';
import { AppProviders } from './app/providers/AppProviders';
import { BrowserRouter } from 'react-router-dom';

// PrimeReact global styles
import 'primereact/resources/primereact.min.css';
import 'primeicons/primeicons.css';

// PrimeFlex
import 'primeflex/primeflex.css';

// Tailwind + your global styles
import './index.css';

// App config bootstrap (initializes runtime overrides)
import { initAppConfig } from '@/config/appConfig';

async function bootstrap() {
  try {
    // Load runtime config (server-injected / public/config.json) and merge with build-time defaults
    await initAppConfig();
  } catch (err) {
    // Non-fatal: log and continue with build-time defaults
    // eslint-disable-next-line no-console
    console.error('Failed to initialize runtime config, continuing with defaults', err);
  }

  const root = ReactDOM.createRoot(document.getElementById('root')!);
  root.render(
    <React.StrictMode>
      <HelmetProvider>
        <BrowserRouter>
          <AppProviders>
            <App />
          </AppProviders>
        </BrowserRouter>
      </HelmetProvider>
    </React.StrictMode>,
  );
}

// Start the app
bootstrap();
