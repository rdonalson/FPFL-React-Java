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

// Tailwind (must load BEFORE SCSS)
import './index.css';

// Global SCSS (loads AFTER Tailwind)
import '@/styles/main.scss';

import { initAppConfig } from '@/config/appConfig';

async function bootstrap() {
  await initAppConfig();

  ReactDOM.createRoot(document.getElementById('root')!).render(
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

bootstrap();
