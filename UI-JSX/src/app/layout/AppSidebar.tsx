// src/app/layout/AppSidebar.tsx
import { Sidebar } from 'primereact/sidebar';
import { useState } from 'react';
import { MenuContext } from './context/MenuContext';
import AppMenu from './AppMenu';

interface Props {
  visible: boolean;
  onHide: () => void;
}

export default function AppSidebar({ visible, onHide }: Props) {
  const [activeIndex, setActiveIndex] = useState<string | null>(visible ? null : null);

  return (
    <Sidebar
      visible={visible}
      onHide={onHide}
      position="left"
      modal
      dismissable
      className="p-sidebar-sm"
    >
      <MenuContext.Provider value={{ activeIndex, setActiveIndex, onMenuToggle: onHide }}>
        <AppMenu />
      </MenuContext.Provider>
    </Sidebar>
  );
}
