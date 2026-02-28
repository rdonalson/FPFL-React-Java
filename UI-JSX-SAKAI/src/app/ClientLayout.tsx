'use client';

import AppLayout from './layout/AppLayout';
import { LayoutProvider } from './layout/context/layoutcontext'; // adjust import to your actual provider

export default function ClientLayout({ children }: { children: React.ReactNode }) {
    return (
        <LayoutProvider>
            <AppLayout>{children}</AppLayout>
        </LayoutProvider>
    );
}
