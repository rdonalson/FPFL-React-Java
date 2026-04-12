// src/app/layout/AppLayout.tsx
import { useState, useRef } from 'react';
import { Outlet } from 'react-router-dom';
import { Toast } from 'primereact/toast';
import { Helmet } from 'react-helmet-async';

import AppTopMenu from './AppTopMenu';
import AppSidebar from './AppSidebar';
import AppFooter from './AppFooter';

import { APP_TITLE, APP_DESCRIPTION, APP_OG_IMAGE } from '../config/seoConfig';

export default function AppLayout() {
  const [sidebarVisible, setSidebarVisible] = useState(false);
  const toastRef = useRef<Toast | null>(null);

  return (
    <>
      {/* GLOBAL SEO */}
      <Helmet>
        <title>{APP_TITLE}</title>
        <meta name="description" content={APP_DESCRIPTION} />

        {/* OpenGraph */}
        <meta property="og:title" content={APP_TITLE} />
        <meta property="og:description" content={APP_DESCRIPTION} />
        <meta property="og:image" content={APP_OG_IMAGE} />
        <meta property="og:type" content="website" />

        {/* Twitter */}
        <meta name="twitter:card" content="summary_large_image" />
        <meta name="twitter:title" content={APP_TITLE} />
        <meta name="twitter:description" content={APP_DESCRIPTION} />
        <meta name="twitter:image" content={APP_OG_IMAGE} />
      </Helmet>

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
