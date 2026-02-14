import { useState, useRef } from 'react';
import { Outlet, Link } from 'react-router-dom';
import { Menubar } from 'primereact/menubar';
import { Sidebar } from 'primereact/sidebar';
import { Button } from 'primereact/button';
import { Toast } from 'primereact/toast';

export function MainLayout() {
  const [sidebarVisible, setSidebarVisible] = useState(false);
  const toastRef = useRef<Toast>(null);

  const menuItems = [
    {
      label: 'Home',
      icon: 'pi pi-home',
      template: () => (
        <Link to="/" className="p-menuitem-link">
          Home
        </Link>
      ),
    },
    {
      label: 'Item Types',
      icon: 'pi pi-list',
      template: () => (
        <Link to="/item-types" className="p-menuitem-link">
          Item Types
        </Link>
      ),
    },
    {
      label: 'Status',
      icon: 'pi pi-server',
      template: () => (
        <Link to="/status" className="p-menuitem-link">
          Status
        </Link>
      ),
    },
    {
      label: 'Docs',
      icon: 'pi pi-book',
      template: () => (
        <Link to="/docs" className="p-menuitem-link">
          Docs
        </Link>
      ),
    },
  ];

  const start = (
    <Button icon="pi pi-bars" text onClick={() => setSidebarVisible(true)} className="mr-2" />
  );

  return (
    <div className="min-h-screen flex flex-column surface-ground">
      {/* Top Navigation */}
      <Menubar model={menuItems} start={start} className="border-none shadow-1" />

      {/* Sidebar Navigation */}
      <Sidebar
        visible={sidebarVisible}
        onHide={() => setSidebarVisible(false)}
        className="p-sidebar-sm"
      >
        <h3 className="mb-3">Navigation</h3>
        <ul className="list-none p-0 m-0 flex flex-column gap-3">
          <li>
            <Link to="/" onClick={() => setSidebarVisible(false)}>
              <i className="pi pi-home mr-2" /> Home
            </Link>
          </li>
          <li>
            <Link to="/item-types" onClick={() => setSidebarVisible(false)}>
              <i className="pi pi-list mr-2" /> Item Types
            </Link>
          </li>
          <li>
            <Link to="/status" onClick={() => setSidebarVisible(false)}>
              <i className="pi pi-server mr-2" /> Status
            </Link>
          </li>
          <li>
            <Link to="/docs" onClick={() => setSidebarVisible(false)}>
              <i className="pi pi-book mr-2" /> Docs
            </Link>
          </li>
        </ul>
      </Sidebar>

      {/* Main Content */}
      <main className="flex-1 p-4">
        <Outlet />
      </main>
    </div>
  );
}
