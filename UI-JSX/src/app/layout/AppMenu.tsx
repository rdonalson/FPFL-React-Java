// src/app/layout/AppMenu.tsx
import { menuModel } from './model/menuModel';
import AppMenuitem from './AppMenuitem';
import { useSessionStore } from '../state/sessionStore';

export default function AppMenu() {
  const { userID } = useSessionStore();
  if (!userID) return null;

  return (
    <ul className="layout-menu">
      {menuModel.map((item, i) => (
        <AppMenuitem key={item.label} item={item} index={`root-${i}`} root />
      ))}
    </ul>
  );
}
