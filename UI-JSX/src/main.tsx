import React from 'react';
import ReactDOM from 'react-dom/client';
import { HelmetProvider } from 'react-helmet-async';
import App from './app/App';
import { AppProviders } from './app/providers/AppProviders';

// PrimeReact global styles
import 'primereact/resources/primereact.min.css';
import 'primeicons/primeicons.css';

// PrimeFlex
import 'primeflex/primeflex.css';

// Tailwind + your global styles
import './index.css';

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <HelmetProvider>
      <AppProviders>
        <App />
      </AppProviders>
    </HelmetProvider>
  </React.StrictMode>,
);
