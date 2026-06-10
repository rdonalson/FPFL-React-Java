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
    label: 'Initial Amount',
    icon: 'pi pi-dollar',
    to: '/command/transactions/initial-amount',
  },

  {
    label: 'Status',
    icon: 'pi pi-server',
    to: '/status',
    roles: ['ROLE_ADMIN'],
  },

  { label: 'Docs', icon: 'pi pi-book', to: '/docs' },
];
