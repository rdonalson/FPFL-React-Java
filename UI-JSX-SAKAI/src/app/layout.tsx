import 'primereact/resources/primereact.min.css';
import 'primeicons/primeicons.css';
import { PrimeReactProvider } from 'primereact/api';
import './globals.scss';

import ClientLayout from './ClientLayout';

export const metadata = {
    title: 'UI-JSX-SAKAI',
    description: 'A clean React + PrimeReact application',
    icons: {
        icon: '/favicon.ico'
    }
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
    return (
        <html lang="en">
            <head>
                <link id="theme-css" href={`/themes/lara-light-teal/theme.css`} rel="stylesheet"></link>
            </head>
            <body>
                <PrimeReactProvider>
                    <ClientLayout>{children}</ClientLayout>
                </PrimeReactProvider>
            </body>
        </html>
    );
}
