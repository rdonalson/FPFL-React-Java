import { useContext } from 'react';
//import { MenuContext } from './MenuContext'; // unchanged path if you created the file above
import { MenuContext } from '@/app/layout/context/MenuContext'; // updated path based on your project structure

export function useMenuContext() {
  const ctx = useContext(MenuContext);
  if (!ctx) throw new Error('useMenuContext must be used within a MenuProvider');
  return ctx;
}
