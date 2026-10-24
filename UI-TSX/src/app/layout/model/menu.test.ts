import { describe, expect, it } from 'vitest';
import { filterMenuByRoles } from './filterMenu';
import { menuModel, type MenuItem } from './menuModel';

const labels = (items: MenuItem[]) => items.map(i => i.label);

describe('filterMenuByRoles', () => {
  it('hides the Admin section from regular users', () => {
    const menu = filterMenuByRoles(menuModel, ['ROLE_USER']);
    expect(labels(menu)).toEqual(['Actions']);
    expect(labels(menu[0].items!)).toContain('Debt Paydown');
  });

  it('shows it to admins', () => {
    const menu = filterMenuByRoles(menuModel, ['ROLE_ADMIN']);
    expect(labels(menu)).toEqual(['Actions', 'Admin']);
    expect(labels(menu[1].items!)).toEqual(['Item Types', 'Time Periods', 'Status']);
  });

  it('filters nested items and drops sections left empty', () => {
    const menu: MenuItem[] = [
      { label: 'Mixed', items: [{ label: 'Open' }, { label: 'Secret', roles: ['ROLE_X'] }] },
      { label: 'All secret', items: [{ label: 'Only X', roles: ['ROLE_X'] }] },
    ];
    const filtered = filterMenuByRoles(menu);
    expect(labels(filtered)).toEqual(['Mixed']);
    expect(labels(filtered[0].items!)).toEqual(['Open']);
  });

  it('every menu link has a route path', () => {
    const links = menuModel.flatMap(section => section.items ?? []);
    expect(links.every(l => typeof l.to === 'string' && l.to.startsWith('/'))).toBe(true);
  });
});
