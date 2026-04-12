// src/app/layout/context/MenuContext.tsx
import { createContext, useContext } from 'react';

export interface MenuContextType {
  activeIndex: string | null;
  setActiveIndex: (index: string | null) => void;
  onMenuToggle: () => void;
}

export const MenuContext = createContext<MenuContextType>({
  activeIndex: null,
  setActiveIndex: () => {},
  onMenuToggle: () => {},
});

export function useMenuContext() {
  return useContext(MenuContext);
}
