import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './app/App';

// Providers
import { QueryProvider } from './app/providers/QueryProvider';
import { AppErrorBoundary } from './app/providers/AppErrorBoundary';
import { PrimeReactProvider } from 'primereact/api';

// PrimeReact global styles
//import 'primereact/resources/themes/lara-light-blue/theme.css';
import 'primereact/resources/themes/bootstrap4-light-purple/theme.css';
import 'primereact/resources/primereact.min.css';
import 'primeicons/primeicons.css';

// PrimeFlex (optional but recommended)
import 'primeflex/primeflex.min.css';

// Your global styles
import "./index.css";

const root = document.getElementById('root')!;
if (root) {
  ReactDOM.createRoot(root).render(
    <React.StrictMode>
      <PrimeReactProvider>
        <AppErrorBoundary>
          <QueryProvider>
            <App />
          </QueryProvider>
        </AppErrorBoundary>
      </PrimeReactProvider>
    </React.StrictMode>,
  );
}
