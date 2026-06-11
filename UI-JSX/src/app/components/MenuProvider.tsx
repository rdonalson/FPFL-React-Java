// src/app/layout/components/MenuProvider.tsx
import React, { useState, useCallback, ReactNode, useMemo } from 'react';
import { MenuContext, MenuContextType } from '@/app/layout/context/MenuContext';

export function MenuProvider({ children, onClose }: { children: ReactNode; onClose?: () => void }) {
  const [activeIndex, setActiveIndex] = useState<string | null>(null);

  const toggleIndex = useCallback((idx: string) => {
    setActiveIndex(prev => (prev === idx ? null : idx));
  }, []);

  const closeMenu = useCallback(() => {
    if (typeof onClose === 'function') onClose();
  }, [onClose]);

  const value = useMemo<MenuContextType>(
    () => ({
      activeIndex,
      setActiveIndex,
      toggleIndex,
      closeMenu,
    }),
    [activeIndex, toggleIndex, closeMenu],
  );

  return <MenuContext.Provider value={value}>{children}</MenuContext.Provider>;
}
