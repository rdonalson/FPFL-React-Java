// src/app/layout/AppSidebar.tsx
import { Sidebar } from 'primereact/sidebar';
import { useState } from 'react';
import { MenuContext } from './context/MenuContext';
import AppMenu from './AppMenu';

interface AppSidebarProps {
  visible: boolean;
  onHide: () => void;
}

export default function AppSidebar({ visible, onHide }: AppSidebarProps) {
  const [activeIndex, setActiveIndex] = useState<number | null>(null);

  const onMenuToggle = () => {
    onHide();
  };

  return (
    <Sidebar visible={visible} onHide={onHide} position="left" className="p-sidebar-sm">
      <MenuContext.Provider value={{ activeIndex, setActiveIndex, onMenuToggle }}>
        <AppMenu />
      </MenuContext.Provider>
    </Sidebar>
  );
}
