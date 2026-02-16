import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './app/App';
import { AppProviders } from './app/providers/AppProviders.tsx';

// PrimeReact global styles
//import 'primereact/resources/themes/lara-light-purple/theme.css';
//import 'primereact/resources/themes/md-light-indigo/theme.css';
//import 'primereact/resources/themes/arya-orange/theme.css';
import 'primereact/resources/themes/bootstrap4-dark-blue/theme.css';
//import 'primereact/resources/themes/lara-dark-blue/theme.css';
import 'primereact/resources/primereact.min.css';
import 'primeicons/primeicons.css';

// PrimeFlex
import 'primeflex/primeflex.css';

// Tailwind + your global styles
import './index.css';

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <AppProviders>
      <App />
    </AppProviders>
  </React.StrictMode>,
);
