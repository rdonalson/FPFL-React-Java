import React, { use } from 'react';
import ReactDOM from 'react-dom/client';
import App from './app/App';
import { AppProviders } from './app/providers/AppProviders.tsx';

// Global styles
import '@/assets/styles/globals.scss';

// PrimeReact
import 'primereact/resources/primereact.min.css';
import 'primeicons/primeicons.css';

// PrimeFlex
import 'primeflex/primeflex.css';

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <AppProviders>
      <App />
    </AppProviders>
  </React.StrictMode>,
);
