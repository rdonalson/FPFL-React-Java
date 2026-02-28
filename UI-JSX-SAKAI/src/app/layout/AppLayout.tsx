'use client';

import { useState, useEffect } from 'react';
import AppTopbar from './AppTopbar';
import AppSidebar from './AppSidebar';
import AppFooter from './AppFooter';

export default function AppLayout({ children }: { children: React.ReactNode }) {
    const [sidebarActive, setSidebarActive] = useState(false);

    const toggleSidebar = () => setSidebarActive(prev => !prev);
    const hideSidebar = () => setSidebarActive(false);

    return (
        <div className={`layout-wrapper layout-static ${sidebarActive ? '' : 'layout-static-inactive'}`}>
            <AppTopbar onToggleSidebar={toggleSidebar} />
            <AppSidebar onHide={hideSidebar} />

            <div className="layout-main-container" onClick={hideSidebar}>
                <div className="layout-main">{children}</div>
                <AppFooter />
            </div>
        </div>
    );
}
