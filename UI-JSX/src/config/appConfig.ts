// src/config/appConfig.ts
import { loadRuntimeConfig } from '@/bootstrap';

const parseNumber = (v: unknown, fallback: number) =>
  typeof v === 'string' || typeof v === 'number' ? Number(v) : fallback;

const parseBool = (v: unknown, fallback = false) =>
  typeof v === 'string' ? v === 'true' : typeof v === 'boolean' ? v : fallback;

export type AppConfigShape = {
  app: {
    title: string;
    description: string;
    ogImage: string;
    nodeEnv: string;
  };
  api: {
    baseUrl: string;
    timeoutMs: number;
  };
  session: {
    warningBeforeMs: number;
    gracePeriodMs: number;
    debug: boolean;
  };
  seo: {
    titleTemplate: string;
    defaultMeta: { title: string; description: string; image: string };
  };
  features: Record<string, boolean>;
  // allow arbitrary runtime keys
  [k: string]: unknown;
};

const buildDefaults: AppConfigShape = {
  app: {
    title: import.meta.env.VITE_APP_TITLE ?? 'FPFL React/Java Platform',
    description:
      import.meta.env.VITE_APP_DESCRIPTION ??
      'FPFL Platform – A React + Java powered financial planning UI with modular features and clean architecture.',
    ogImage: import.meta.env.VITE_APP_OG_IMAGE ?? '/og-image.png',
    nodeEnv: import.meta.env.VITE_NODE_ENV ?? 'development',
  },
  api: {
    baseUrl: import.meta.env.VITE_API_URL ?? 'http://localhost:9000',
    timeoutMs: Number(import.meta.env.VITE_API_TIMEOUT_MS ?? 10000),
  },
  session: {
    warningBeforeMs: Number(import.meta.env.VITE_SESSION_WARNING_MS ?? 300_000),
    gracePeriodMs: Number(import.meta.env.VITE_SESSION_GRACE_PERIOD_MS ?? 0),
    debug: import.meta.env.VITE_SESSION_DEBUG === 'true' || false,
  },
  seo: {
    titleTemplate: import.meta.env.VITE_SEO_TITLE_TEMPLATE ?? '%s · FPFL',
    defaultMeta: {
      title: import.meta.env.VITE_SEO_DEFAULT_TITLE ?? 'FPFL Platform',
      description:
        import.meta.env.VITE_SEO_DEFAULT_DESCRIPTION ??
        'Financial planning platform built with React and Java.',
      image: import.meta.env.VITE_SEO_DEFAULT_IMAGE ?? '/og-image.png',
    },
  },
  features: {
    enableBetaX: import.meta.env.VITE_FEATURE_BETA_X === 'true' || false,
  },
};

let mergedConfig: AppConfigShape = buildDefaults;

/**
 * Initialize AppConfig by merging runtime overrides into build-time defaults.
 * Called once at app bootstrap.
 */
export async function initAppConfig() {
  const runtime = await loadRuntimeConfig();
  if (!runtime) return;

  // shallow merge strategy for top-level sections
  mergedConfig = {
    ...buildDefaults,
    ...runtime,
    app: { ...buildDefaults.app, ...(runtime.app as object) },
    api: {
      ...buildDefaults.api,
      ...(runtime.api as object),
      timeoutMs: parseNumber((runtime.api as any)?.timeoutMs, buildDefaults.api.timeoutMs),
    },
    session: {
      ...buildDefaults.session,
      ...(runtime.session as object),
      warningBeforeMs: parseNumber(
        (runtime.session as any)?.warningBeforeMs,
        buildDefaults.session.warningBeforeMs,
      ),
      gracePeriodMs: parseNumber(
        (runtime.session as any)?.gracePeriodMs,
        buildDefaults.session.gracePeriodMs,
      ),
      debug: parseBool((runtime.session as any)?.debug, buildDefaults.session.debug),
    },
    seo: {
      ...buildDefaults.seo,
      ...(runtime.seo as object),
    },
    features: { ...buildDefaults.features, ...(runtime.features as object) },
  } as AppConfigShape;
}

/**
 * Accessor for the merged config after initAppConfig has run.
 */
export const AppConfig = {
  get: (): AppConfigShape => mergedConfig,
};
