// src/app/layout/AppMenu.tsx
import { menuModel } from './model/menuModel';
import AppMenuitem from './AppMenuitem';

export default function AppMenu() {
  return (
    <ul className="layout-menu">
      {menuModel.map((item, i) => (
        <AppMenuitem key={item.label} item={item} index={`root-${i}`} root />
      ))}
    </ul>
  );
}
