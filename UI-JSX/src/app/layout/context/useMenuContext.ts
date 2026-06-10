// src/app/layout/context/useMenuContext.ts
import { useContext } from 'react';
import { MenuContext } from './MenuContext';

export function useMenuContext() {
  const ctx = useContext(MenuContext);
  if (!ctx) {
    throw new Error('useMenuContext must be used within a MenuProvider');
  }
  return ctx;
}
