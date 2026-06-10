// src/app/layout/AppMenu.tsx
import { menuModel } from './model/menuModel';
import { filterMenuByRoles } from './model/filterMenu';
import AppMenuitem from './AppMenuitem';
import { useAuth } from '@/app/auth/hooks/useAuth';
import { useSessionStore } from '../state/sessionStore';

export default function AppMenu() {
  const { userId } = useSessionStore();
  const { roles } = useAuth();

  if (!userId) return null;

  const filteredModel = filterMenuByRoles(menuModel, roles);

  return (
    <ul className="layout-menu">
      {filteredModel.map((item, i) => (
        <AppMenuitem key={item.label} item={item} index={`root-${i}`} root />
      ))}
    </ul>
  );
}
