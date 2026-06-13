// src/app/layout/model/menuModel.ts

export interface MenuItem {
  label: string;
  icon?: string;
  to?: string;
  items?: MenuItem[];
  roles?: string[]; // <-- NEW: restrict to these roles
}

export const menuModel: MenuItem[] = [
  { label: 'Home', icon: 'pi pi-home', to: '/' },
  /** ADMIN-ONLY ITEMS */
  {
    label: 'Item Types',
    icon: 'pi pi-tags',
    to: '/command/admin/item-types',
    roles: ['ROLE_ADMIN'],
  },
  {
    label: 'Time Periods',
    icon: 'pi pi-calendar',
    to: '/command/admin/time-periods',
    roles: ['ROLE_ADMIN'],
  },
  {
    label: 'Status',
    icon: 'pi pi-server',
    to: '/status',
    roles: ['ROLE_ADMIN'],
  },
  /** EVERYONE CAN SEE THESE ITEMS */
  /** Note: The "to" paths here should match the routes defined in AppRouter.tsx */
  {
    label: 'Initial Amount',
    icon: 'pi pi-dollar',
    to: '/command/transactions/initial-amount',
  },
  {
    label: 'Credits',
    icon: 'pi pi-dollar',
    to: '/command/transactions/credits',
  },
  {
    label: 'Debits',
    icon: 'pi pi-dollar',
    to: '/command/transactions/debits',
  },

  { label: 'Docs', icon: 'pi pi-book', to: '/docs' },
];
