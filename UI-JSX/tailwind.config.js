/** @type {import('tailwindcss').Config} */
import { tailwindcss } from 'tailwindcss';
import primeui from 'tailwindcss-primeui';

export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {},
  },
  plugins: [
    primeui, // enables PrimeReact Tailwind mode
  ],
};
