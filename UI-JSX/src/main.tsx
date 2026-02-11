import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './app/App';
import { QueryProvider } from './app/providers/QueryProvider';
import { AppErrorBoundary } from './app/providers/AppErrorBoundary';

import './index.css';

// PrimeReact global styles
import 'primereact/resources/themes/lara-light-blue/theme.css';
import 'primereact/resources/primereact.min.css';
import 'primeicons/primeicons.css';

ReactDOM.createRoot(document.getElementById('root')!).render(
  //<React.StrictMode>
    <AppErrorBoundary>
      <QueryProvider>
        <App />
      </QueryProvider>
    </AppErrorBoundary>
  //</React.StrictMode>,
);
