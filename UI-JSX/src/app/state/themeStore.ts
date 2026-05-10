import { create } from 'zustand';

interface ThemeState {
  dark: boolean;
  toggleTheme: () => void;
  setTheme: (dark: boolean) => void;
}

export const useThemeStore = create<ThemeState>(set => ({
  dark: localStorage.getItem('darkTheme') === 'true',

  setTheme: dark => {
    localStorage.setItem('darkTheme', String(dark));

    const themeLink = document.getElementById('theme-css') as HTMLLinkElement;
    themeLink.href = dark
      ? '/public/themes/lara-dark-indigo/theme.css'
      : '/public/themes/lara-light-indigo/theme.css';

    set({ dark });
  },

  toggleTheme: () =>
    set(state => {
      const next = !state.dark;
      localStorage.setItem('darkTheme', String(next));

      const themeLink = document.getElementById('theme-css') as HTMLLinkElement;
      themeLink.href = next
        ? '/public/themes/lara-dark-indigo/theme.css'
        : '/public/themes/lara-light-indigo/theme.css';

      return { dark: next };
    }),
}));
