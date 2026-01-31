import js from '@eslint/js';
import globals from 'globals';
import reactHooks from 'eslint-plugin-react-hooks';
import reactRefresh from 'eslint-plugin-react-refresh';
import tseslint from 'typescript-eslint';
import prettier from 'eslint-plugin-prettier';
import { defineConfig, globalIgnores } from 'eslint/config';

export default defineConfig([
  // Ignore build output
  globalIgnores(['dist']),

  // TypeScript + React rules
  {
    files: ['**/*.{ts,tsx}'],
    extends: [
      js.configs.recommended,
      tseslint.configs.recommended,
      reactHooks.configs.flat.recommended,
      reactRefresh.configs.vite,
    ],
    languageOptions: {
      ecmaVersion: 2020,
      globals: globals.browser,
    },
    rules: {
      // Enforce TS-only imports
      'no-restricted-syntax': [
        'error',
        {
          selector: 'Program:has(ImportDeclaration[source.value=/\\.js$/])',
          message: 'Use TypeScript files instead of JavaScript.',
        },
      ],

      // Prettier formatting as ESLint rule
      'prettier/prettier': 'error',
    },
    plugins: {
      prettier,
    },
  },

  // Block .js files inside src/
  {
    files: ['src/**/*.js'],
    rules: {
      'no-restricted-syntax': [
        'error',
        {
          selector: 'Program',
          message: 'JavaScript files are not allowed in src/. Use TypeScript.',
        },
      ],
    },
  },

  // Allow JS in config files (eslint, vite, tailwind, etc.)
  {
    files: ['*.js'],
    rules: {
      // No TS-only restrictions here
    },
  },
]);
