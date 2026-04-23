// src/app/layout/context/MenuContext.tsx
import React, { createContext, useContext, useState } from 'react';

type MenuContextType = {
  activeIndex: string | null;
  setActiveIndex: (idx: string | null) => void;
  onMenuToggle: () => void;
};

const MenuContext = createContext<MenuContextType | undefined>(undefined);

export function MenuProvider({
  children,
  onClose,
}: {
  children: React.ReactNode;
  onClose?: () => void;
}) {
  const [activeIndex, setActiveIndex] = useState<string | null>(null);

  // onMenuToggle is called by menu items when a leaf is selected.
  // It will close the sidebar if an onClose handler was provided.
  const onMenuToggle = () => {
    if (typeof onClose === 'function') {
      onClose();
    }
  };

  return (
    <MenuContext.Provider value={{ activeIndex, setActiveIndex, onMenuToggle }}>
      {children}
    </MenuContext.Provider>
  );
}

export function useMenuContext() {
  const ctx = useContext(MenuContext);
  if (!ctx) {
    throw new Error('useMenuContext must be used within a MenuProvider');
  }
  return ctx;
}
