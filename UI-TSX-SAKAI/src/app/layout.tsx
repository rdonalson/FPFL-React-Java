import 'primereact/resources/primereact.min.css';
import 'primeicons/primeicons.css';
import { PrimeReactProvider } from 'primereact/api';
import './globals.scss';

import ClientLayout from './ClientLayout';
import QueryProvider from './providers/QueryProvider';

export const metadata = {
    title: 'UI-JSX-SAKAI',
    description: 'A clean React + PrimeReact application',
    icons: {
        icon: '/favicon.ico'
    }
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
    console.log('Running in:', process.env.NODE_ENV);
    console.log('Env file:', process.env.NEXT_PUBLIC_ENV);
    console.log('API URL:', process.env.NEXT_PUBLIC_API_URL);

    return (
        <html lang="en">
            <head>
                <link id="theme-css" href={`/themes/lara-light-teal/theme.css`} rel="stylesheet"></link>
            </head>
            <body>
                <QueryProvider>
                    <PrimeReactProvider>
                        <ClientLayout>{children}</ClientLayout>
                    </PrimeReactProvider>
                </QueryProvider>
            </body>
        </html>
    );
}
