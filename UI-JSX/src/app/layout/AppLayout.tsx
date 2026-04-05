// src/app/layout/AppLayout.tsx
import { useState, useRef } from 'react';
import { Outlet } from 'react-router-dom';
import { Toast } from 'primereact/toast';
import { AppTopMenu } from './AppTopMenu';

import AppSidebar from './AppSidebar';
import AppFooter from './AppFooter';

export default function AppLayout() {
  const [sidebarVisible, setSidebarVisible] = useState(false);
  const toastRef = useRef<Toast>(null);

  return (
    <>
      {/* GLOBAL TOAST */}
      <Toast ref={toastRef} position="top-right" />

      <div className="min-h-screen flex flex-column surface-ground">
        {/* TOP MENU (hamburger + theme toggle) */}
        <AppTopMenu onToggleSidebar={() => setSidebarVisible(true)} />

        {/* SLIDE-OUT SIDEBAR (AppSidebar + AppMenu + AppMenuitem) */}
        <AppSidebar visible={sidebarVisible} onHide={() => setSidebarVisible(false)} />

        {/* PAGE CONTENT */}
        <main className="flex-1 p-4">
          <Outlet context={{ toastRef }} />
        </main>

        {/* FOOTER */}
        <AppFooter />
      </div>
    </>
  );
}
