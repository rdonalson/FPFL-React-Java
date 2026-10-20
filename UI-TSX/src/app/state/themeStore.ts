import { create } from 'zustand';

interface ThemeState {
  dark: boolean;
  toggleTheme: () => void;
  setTheme: (dark: boolean) => void;
}

const LIGHT_THEME = '/themes/lara-light-green/theme.css';
const DARK_THEME = '/themes/lara-dark-green/theme.css';

/** Swap the PrimeReact theme stylesheet and expose the mode as <html data-theme="…"> for our own SCSS. */
function applyTheme(dark: boolean) {
  localStorage.setItem('darkTheme', String(dark));

  const themeLink = document.getElementById('theme-css') as HTMLLinkElement | null;
  if (themeLink) themeLink.href = dark ? DARK_THEME : LIGHT_THEME;

  document.documentElement.setAttribute('data-theme', dark ? 'dark' : 'light');
}

export const useThemeStore = create<ThemeState>(set => ({
  dark: localStorage.getItem('darkTheme') === 'true',

  setTheme: dark => {
    applyTheme(dark);
    set({ dark });
  },

  toggleTheme: () =>
    set(state => {
      const next = !state.dark;
      applyTheme(next);
      return { dark: next };
    }),
}));
