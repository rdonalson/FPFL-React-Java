// src/app/layout/model/menuModel.ts

export interface MenuItem {
  label: string;
  icon?: string;
  to?: string;
  items?: MenuItem[];
  roles?: string[]; // restrict to these roles
}

/**
 * Top-level entries are SECTIONS (rendered by AppMenuitem as titled, always-expanded groups).
 * Note: The "to" paths here should match the routes defined in AppRouter.tsx
 */
export const menuModel: MenuItem[] = [
  /** EVERYONE CAN SEE THESE ITEMS */
  {
    label: 'Actions',
    items: [
      { label: 'Home', icon: 'pi pi-home', to: '/' },
      { label: 'Display', icon: 'pi pi-chart-bar', to: '/query/display' },
      { label: 'Initial Amount', icon: 'pi pi-dollar', to: '/command/transactions/initial-amount' },
      { label: 'Credits', icon: 'pi pi-dollar', to: '/command/transactions/credits' },
      { label: 'Debits', icon: 'pi pi-dollar', to: '/command/transactions/debits' },
      { label: 'Docs', icon: 'pi pi-book', to: '/docs' },
    ],
  },

  /** ADMIN-ONLY ITEMS — whole section is hidden for non-admins */
  {
    label: 'Admin',
    roles: ['ROLE_ADMIN'],
    items: [
      { label: 'Item Types', icon: 'pi pi-tags', to: '/command/admin/item-types' },
      { label: 'Time Periods', icon: 'pi pi-calendar', to: '/command/admin/time-periods' },
      { label: 'Status', icon: 'pi pi-server', to: '/status' },
    ],
  },
];
