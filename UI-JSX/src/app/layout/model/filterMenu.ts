// src/app/layout/model/filterMenu.ts
import type { MenuItem } from './menuModel';

export function filterMenuByRoles(menu: MenuItem[], userRoles: string[]): MenuItem[] {
  return menu
    .filter(item => {
      // If item has role restrictions, user must match at least one
      if (item.roles && !item.roles.some(r => userRoles.includes(r))) {
        return false;
      }
      return true;
    })
    .map(item => {
      if (item.items) {
        const filteredChildren = filterMenuByRoles(item.items, userRoles);
        return { ...item, items: filteredChildren };
      }
      return item;
    })
    .filter(item => {
      // Remove parent groups that ended up empty
      if (item.items && item.items.length === 0) return false;
      return true;
    });
}
