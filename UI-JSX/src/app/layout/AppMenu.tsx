// src/app/layout/AppMenu.tsx
import AppMenuitem from './AppMenuitem';

export default function AppMenu() {
  const model = [
    {
      label: 'Home',
      icon: 'pi pi-home',
      to: '/',
    },
    {
      label: 'Item Types',
      icon: 'pi pi-tags',
      items: [
        { label: 'All Item Types', icon: 'pi pi-list', to: '/item-types' },
        { label: 'Item Type Details', icon: 'pi pi-list', to: '/item-types/1' },
        { label: 'Create Item Type', icon: 'pi pi-plus', to: '/item-types/create' },
      ],
    },
    {
      label: 'Status',
      icon: 'pi pi-server',
      to: '/status',
    },
    {
      label: 'Docs',
      icon: 'pi pi-book',
      to: '/docs',
    },
  ];

  return (
    <ul className="layout-menu">
      {model.map((item, i) => (
        <AppMenuitem item={item} index={i} root key={item.label} />
      ))}
    </ul>
  );
}
