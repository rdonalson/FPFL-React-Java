// src/app/layout/AppLayout.tsx
import { useState, useRef, useEffect } from 'react';
import { Outlet } from 'react-router-dom';
import { Toast } from 'primereact/toast';

import AppTopMenu from './AppTopMenu';
import AppSidebar from './AppSidebar';
import AppFooter from './AppFooter';
import { APP_TITLE } from '../config/appConfig';

export default function AppLayout() {
  const [sidebarVisible, setSidebarVisible] = useState(false);
  const toastRef = useRef<Toast | null>(null);

  // Set document title once on mount
  useEffect(() => {
    document.title = APP_TITLE;
  }, []);

  return (
    <>
      <Toast ref={toastRef} position="top-right" />

      <div className="min-h-screen flex flex-col surface-ground">
        <header>
          <AppTopMenu onToggleSidebar={() => setSidebarVisible(true)} />
        </header>

        <aside>
          <AppSidebar visible={sidebarVisible} onHide={() => setSidebarVisible(false)} />
        </aside>

        <main className="flex-1 p-4">
          <Outlet context={{ toastRef }} />
        </main>

        <footer>
          <AppFooter />
        </footer>
      </div>
    </>
  );
}
