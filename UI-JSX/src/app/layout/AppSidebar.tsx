// src/app/layout/AppSidebar.tsx
import React from 'react';
import { Sidebar } from 'primereact/sidebar';
import { useSessionStore } from '@/app/state/sessionStore';
import AppMenu from './AppMenu';
import { MenuProvider } from './context/MenuContext';

export default function AppSidebar({ visible, onHide }: { visible: boolean; onHide: () => void }) {
  const { userID } = useSessionStore();

  if (!userID) return null;

  return (
    <Sidebar visible={visible} onHide={onHide} modal>
      {/* Provide the sidebar onHide to the menu context so menu items can close it */}
      <MenuProvider onClose={onHide}>
        <AppMenu />
      </MenuProvider>
    </Sidebar>
  );
}
