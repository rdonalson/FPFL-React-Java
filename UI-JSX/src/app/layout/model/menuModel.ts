// src/app/layout/model/menuModel.ts

export interface MenuItem {
  label: string;
  icon?: string;
  to?: string;
  items?: MenuItem[];
}

export const menuModel: MenuItem[] = [
  { label: 'Home', icon: 'pi pi-home', to: '/' },
  { label: 'Item Types', icon: 'pi pi-tags', to: '/item-types' },
  { label: 'Status', icon: 'pi pi-server', to: '/status' },
  { label: 'Docs', icon: 'pi pi-book', to: '/docs' },
];
