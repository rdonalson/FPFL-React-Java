import { afterEach, describe, expect, it, vi } from 'vitest';

async function fresh() {
  vi.resetModules();
  return import('./appConfig');
}

afterEach(() => {
  delete (window as any).__RUNTIME_CONFIG__;
  vi.unstubAllGlobals();
});

describe('AppConfig', () => {
  it('uses build-time defaults until initAppConfig runs', async () => {
    const { AppConfig } = await fresh();
    const cfg = AppConfig.get();
    expect(cfg.api.baseUrl).toBeTruthy();
    expect(cfg.api.timeoutMs).toBeGreaterThan(0);
    expect(cfg.seo.titleTemplate).toContain('%s');
  });

  it('merges a server-injected runtime config over the defaults', async () => {
    (window as any).__RUNTIME_CONFIG__ = {
      api: { baseUrl: 'https://api-jdk.ledger-finance.com', timeoutMs: '30000' },
      session: { warningBeforeMs: '60000', debug: 'true' },
      features: { newThing: true },
    };
    const { AppConfig, initAppConfig } = await fresh();
    await initAppConfig();

    const cfg = AppConfig.get();
    expect(cfg.api.baseUrl).toBe('https://api-jdk.ledger-finance.com');
    expect(cfg.api.timeoutMs).toBe(30000);
    expect(cfg.session.warningBeforeMs).toBe(60000);
    expect(cfg.session.debug).toBe(true);
    expect(cfg.features.newThing).toBe(true);
    expect(cfg.app.title).toBeTruthy(); // untouched sections keep their defaults
  });

  it('falls back to /config.json, and to the defaults when there is none', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn(async () => ({ ok: true, json: async () => ({ app: { title: 'From JSON' } }) })),
    );
    let mod = await fresh();
    await mod.initAppConfig();
    expect(mod.AppConfig.get().app.title).toBe('From JSON');

    vi.stubGlobal(
      'fetch',
      vi.fn(async () => ({ ok: false, status: 404, json: async () => ({}) })),
    );
    vi.spyOn(console, 'debug').mockImplementation(() => {});
    mod = await fresh();
    const before = mod.AppConfig.get();
    await mod.initAppConfig();
    expect(mod.AppConfig.get()).toBe(before);
  });
});
