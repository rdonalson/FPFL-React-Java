// src/shared/hooks/useIsSmallScreen.ts
import { useEffect, useState } from 'react';

/** Same query as the SM rules in styles/_global.scss: phone portrait, or phone landscape. */
export const SMALL_SCREEN_QUERY = '(max-width: 767px), (max-height: 500px) and (orientation: landscape)';

/** True on phone-size screens; updates live on rotate/resize (no refresh needed). */
export function useIsSmallScreen(): boolean {
  const get = () => typeof window !== 'undefined' && window.matchMedia(SMALL_SCREEN_QUERY).matches;
  const [isSmall, setIsSmall] = useState(get);

  useEffect(() => {
    const mql = window.matchMedia(SMALL_SCREEN_QUERY);
    const update = () => setIsSmall(mql.matches);
    update();
    mql.addEventListener('change', update);
    // iOS Safari can report the new size a moment after rotating
    window.addEventListener('orientationchange', update);
    window.addEventListener('resize', update);
    return () => {
      mql.removeEventListener('change', update);
      window.removeEventListener('orientationchange', update);
      window.removeEventListener('resize', update);
    };
  }, []);

  return isSmall;
}
