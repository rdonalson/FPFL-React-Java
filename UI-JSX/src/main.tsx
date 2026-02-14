import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './app/App';

// Providers
import { QueryProvider } from './app/providers/QueryProvider';
import { AppErrorBoundary } from './app/providers/AppErrorBoundary';
import { PrimeReactProvider } from 'primereact/api';

// PrimeReact global styles
//import 'primereact/resources/themes/lara-light-blue/theme.css';
import 'primereact/resources/themes/arya-orange/theme.css';
//import 'primereact/resources/themes/lara-dark-blue/theme.css';
import 'primereact/resources/primereact.min.css';
import 'primeicons/primeicons.css';

// PrimeFlex
import 'primeflex/primeflex.css';

// Tailwind + your global styles
import './index.css';

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <PrimeReactProvider value={{ ripple: true }}>
      <AppErrorBoundary>
        <QueryProvider>
          <App />
        </QueryProvider>
      </AppErrorBoundary>
    </PrimeReactProvider>
  </React.StrictMode>
);
