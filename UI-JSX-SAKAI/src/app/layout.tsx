import './globals.scss';
import 'primereact/resources/primereact.min.css';
import 'primeicons/primeicons.css';
import { PrimeReactProvider } from 'primereact/api';

import '../assets/layout/styles/lara-dark-teal/theme.css';
import '../assets/layout/styles/layout.scss';

import AppLayout from './layout/AppLayout';

export const metadata = {
    title: 'UI-JSX-SAKAI',
    description: 'A clean React + PrimeReact application',
    icons: {
        icon: '/favicon.ico',
        shortcut: '/favicon.ico',
        apple: '/favicon.png'
    }
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
    return (
        <html lang="en">
            <head>
                {/* Optional: SVG version */}
                <link rel="icon" type="image/svg+xml" href="/favicon.svg" />

                {/* Charset + viewport */}
                <meta charSet="UTF-8" />
                <meta name="viewport" content="width=device-width, initial-scale=1" />

                {/* Title fallback (metadata handles the real title) */}
                <title>My App 1</title>
            </head>

            <body>
                <PrimeReactProvider>
                    <AppLayout>{children}</AppLayout>
                </PrimeReactProvider>
            </body>
        </html>
    );
}
