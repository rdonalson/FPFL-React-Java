// src/app/layout/AppLayout.tsx
import { useState, useRef } from 'react';
import { Outlet } from 'react-router-dom';
import { Toast } from 'primereact/toast';
import { Helmet } from 'react-helmet-async';

import { AppTopMenu } from './AppTopMenu';
import AppSidebar from './AppSidebar';
import AppFooter from './AppFooter';

import { AppConfig } from '@/config/appConfig';

export default function AppLayout() {
  const [sidebarVisible, setSidebarVisible] = useState(false);
  const toastRef = useRef<Toast | null>(null);

  const cfg = AppConfig.get();
  const { title, description, ogImage } = cfg.app;

  return (
    <>
      {/* GLOBAL SEO */}
      <Helmet>
        <title>{title}</title>
        <meta name="description" content={description} />

        {/* OpenGraph */}
        <meta property="og:title" content={title} />
        <meta property="og:description" content={description} />
        <meta property="og:image" content={ogImage} />
        <meta property="og:type" content="website" />

        {/* Twitter */}
        <meta name="twitter:card" content="summary_large_image" />
        <meta name="twitter:title" content={title} />
        <meta name="twitter:description" content={description} />
        <meta name="twitter:image" content={ogImage} />
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
