// src/app/layout/model/filterMenu.ts
import type { MenuItem } from './menuModel';

/**
 * Removes items the user lacks a role for (recursively),
 * then drops any section/submenu left with no visible children.
 */
export function filterMenuByRoles(items: MenuItem[], roles: readonly string[] = []): MenuItem[] {
  return items
    .filter(item => !item.roles?.length || item.roles.some(r => roles.includes(r)))
    .map(item => (item.items ? { ...item, items: filterMenuByRoles(item.items, roles) } : item))
    .filter(item => !item.items || item.items.length > 0);
}
