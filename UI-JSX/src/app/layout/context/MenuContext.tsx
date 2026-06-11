// src/app/layout/context/MenuContext.ts
import { createContext, useContext } from 'react';

export type MenuContextType = {
  activeIndex: string | null;
  setActiveIndex: (idx: string | null) => void;
  toggleIndex: (idx: string) => void;
  closeMenu: () => void;
};

export const MenuContext = createContext<MenuContextType | undefined>(undefined);

export function useMenuContext(): MenuContextType {
  const ctx = useContext(MenuContext);
  if (!ctx) throw new Error('useMenuContext must be used within a MenuProvider');
  return ctx;
}
