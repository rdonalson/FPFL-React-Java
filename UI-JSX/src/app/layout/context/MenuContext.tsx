// src/app/layout/context/MenuContext.tsx
import { createContext, useState, useCallback, ReactNode } from 'react';

type MenuContextType = {
  activeIndex: string | null;
  setActiveIndex: (idx: string | null) => void;
  toggleIndex: (idx: string) => void;
  closeMenu: () => void;
};

// ❗ FIX: Export the context
export const MenuContext = createContext<MenuContextType | undefined>(undefined);

export function MenuProvider({ children, onClose }: { children: ReactNode; onClose?: () => void }) {
  const [activeIndex, setActiveIndex] = useState<string | null>(null);

  const toggleIndex = useCallback((idx: string) => {
    setActiveIndex(prev => (prev === idx ? null : idx));
  }, []);

  const closeMenu = useCallback(() => {
    if (typeof onClose === 'function') {
      onClose();
    }
  }, [onClose]);

  return (
    <MenuContext.Provider
      value={{
        activeIndex,
        setActiveIndex,
        toggleIndex,
        closeMenu,
      }}
    >
      {children}
    </MenuContext.Provider>
  );
}
