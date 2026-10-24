import { afterEach, describe, expect, it } from 'vitest';
import { useThemeStore } from './themeStore';

afterEach(() => {
  document.getElementById('theme-css')?.remove();
  localStorage.clear();
});

describe('themeStore', () => {
  it('switches the PrimeReact stylesheet, data-theme and remembers the choice', () => {
    const link = document.createElement('link');
    link.id = 'theme-css';
    document.head.appendChild(link);

    useThemeStore.getState().setTheme(true);
    expect(useThemeStore.getState().dark).toBe(true);
    expect(link.getAttribute('href')).toBe('/themes/lara-dark-green/theme.css');
    expect(document.documentElement.getAttribute('data-theme')).toBe('dark');
    expect(localStorage.getItem('darkTheme')).toBe('true');

    useThemeStore.getState().toggleTheme();
    expect(useThemeStore.getState().dark).toBe(false);
    expect(link.getAttribute('href')).toBe('/themes/lara-light-green/theme.css');
    expect(document.documentElement.getAttribute('data-theme')).toBe('light');
    expect(localStorage.getItem('darkTheme')).toBe('false');
  });

  it('works without a theme link on the page', () => {
    expect(() => useThemeStore.getState().toggleTheme()).not.toThrow();
  });
});
