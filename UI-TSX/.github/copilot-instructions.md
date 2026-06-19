# FPFL React UI - Copilot Instructions

## Project Overview

Modern React + TypeScript + Vite frontend for Fantasy Premier League Fantasy League platform. Built with React Compiler for optimization, Radix UI + PrimeReact for components, TanStack utilities for state/data management, and Tailwind CSS for styling. Runs on port 4000 locally.

## Architecture

### Tech Stack

- **Runtime**: React 19.2 (SSR-ready with React Compiler)
- **Language**: TypeScript 5.9 (strict mode enabled)
- **Build**: Vite 7.2 with HMR, Babel plugin for React Compiler
- **Styling**: Tailwind CSS 3.4 + PostCSS
- **UI Components**: PrimeReact 10.9 + Radix UI (dialog, dropdown, popover, tabs, tooltip)
- **State Management**: Zustand 5.0 (lightweight store)
- **Data Fetching**: TanStack React Query 5.90 (caching/sync)
- **Tables**: TanStack React Table 8.21 (headless)
- **Forms**: React Hook Form 7.71 + Zod 4.3 (validation)
- **Icons**: Lucide React + PrimeIcons

### Project Structure

```
src/
  main.tsx          # Entry point with React 19 root API
  App.tsx           # Root component (import from src via @ alias)
  App.css
  index.css         # Global styles (Tailwind imports here)
  assets/           # Images, SVGs
```

Alias resolution: `@/` → `./src/` (configured in tsconfig.json + vite.config.ts)

### Key Patterns

1. **Component organization**: Use TypeScript export syntax; components automatically support Fast Refresh
2. **Styling**: Tailwind + CSS modules; global styles in index.css
3. **Forms**: React Hook Form + Zod for validation (see dependencies for pattern)
4. **Data fetching**: TanStack React Query for server state; Zustand for client state
5. **Dialogs/Dropdowns**: Prefer Radix UI primitives over PrimeReact for accessibility

## Developer Workflow

### Commands

- `npm run dev` - Start Vite dev server (http://localhost:4000, HMR enabled)
- `npm run build` - Type-check (`tsc -b`) then Vite build to dist/
- `npm run lint` - ESLint check (TypeScript + React rules)
- `npm run lint:fix` - Auto-fix linting issues + Prettier formatting
- `npm run format` - Prettier format all files
- `npm run preview` - Serve built dist/ locally

### Build Pipeline

TypeScript compilation runs first (`tsc -b`), then Vite builds with React Compiler optimizations. React Compiler may impact dev/build performance—monitor if issues arise.

## Code Style & Conventions

### ESLint + Prettier

- **Single quotes**, **no semicolons** (Prettier: semi: false, singleQuote: true)
- **Print width**: 100 characters
- **Tab width**: 2 spaces, **trailing commas**: always
- **Arrow functions**: No parens for single params (`x => x.id`)
- **TypeScript**: Strict mode enforced; no `.js` imports in src/ (linting error)
- ESLint rules: React Hooks recommended, React Refresh for HMR, Prettier integration

**Auto-fix**: Run `npm run lint:fix` to resolve all formatting/linting issues before commit.

### TypeScript Patterns

- Use strict mode (`strict: true`); avoid `any` types
- Path aliases: import from `'@/components/MyComponent'` (not relative paths)
- Define types in `.d.ts` files for modules (e.g., `App.d.ts`)

## Critical Files & Integration Points

- **[eslint.config.js](eslint.config.js)** - ESLint/Prettier rules; blocks JS in src/
- **[vite.config.ts](vite.config.ts)** - Port 4000, @ alias, React Compiler config
- **[tsconfig.json](tsconfig.json)** - Strict mode, @ alias resolution, module settings
- **[tailwind.config.js](tailwind.config.js)** - Tailwind content paths (src/\*_/_.{ts,tsx})
- **[package.json](package.json)** - All dependencies; React 19, TanStack, Radix UI, PrimeReact

## Common Tasks

### Adding a Component

1. Create in `src/components/MyComponent.tsx` using functional syntax
2. Import styles (CSS modules or Tailwind)
3. Export as named export; use TypeScript interfaces for props
4. Test HMR with `npm run dev`

### Adding a New Dependency

- Run `npm install <pkg>`; dependencies auto-discovered by build
- Update imports in files using it
- Run `npm run lint:fix` to ensure no issues

### Debugging

- Browser DevTools: React tab for component inspection (Fiber tree)
- Vite dev server logs: Check terminal for HMR/import errors
- Console: React Compiler may hide some debug info—use debugger statement if needed

## Integration Points & External Services

- **Backend API**: Likely Java-based (inferred from monorepo name); use React Query for requests
- **State hydration**: Use Zustand for global client state; React Query for server sync
- **Icons**: Mix Lucide React (prefer for SVG) and PrimeIcons (Material Design)

## Known Constraints & Notes

- React Compiler enabled → may slow Vite dev/build; report performance regressions
- TypeScript 5.9 strict mode means no implicit `any`; declare types explicitly
- Radix UI primitives unstyled—pair with Tailwind or CSS for visual output
- PrimeReact components come pre-styled (CSS must be loaded from primeicons/primereact)
