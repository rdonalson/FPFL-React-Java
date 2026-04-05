// src/app/layout/MenuContext.tsx
import { createContext, useContext } from 'react';

export interface MenuContextType {
  activeIndex: number | null;
  setActiveIndex: (index: number | null) => void;
  onMenuToggle: () => void;
}

export const MenuContext = createContext<MenuContextType | null>(null);

export function useMenuContext() {
  const ctx = useContext(MenuContext);
  if (!ctx) throw new Error('MenuContext missing');
  return ctx;
}
