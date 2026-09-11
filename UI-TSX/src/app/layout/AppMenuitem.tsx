// src/app/layout/AppMenuitem.tsx
import { Link, useLocation } from 'react-router-dom';
import { useMenuContext } from './context/useMenuContext';
import { useEffect } from 'react';
import type { MenuItem } from './model/menuModel';

interface Props {
  item: MenuItem;
  index: string;
  root?: boolean;
}

export default function AppMenuitem({ item, index, root }: Props) {
  const location = useLocation();
  const { activeIndex, setActiveIndex, toggleIndex, closeMenu } = useMenuContext();

  const isActiveRoute = item.to === location.pathname;
  const expanded = activeIndex === index;

  // Auto-expand parent when route matches
  useEffect(() => {
    if (isActiveRoute) {
      setActiveIndex(index);
    }
  }, [isActiveRoute, index, setActiveIndex]);

  // ── Root-level group (e.g. "Actions", "Admin"): titled section, always expanded ──
  if (root && item.items) {
    const isFirstSection = index === 'root-0';

    return (
      <li className={`list-none ${isFirstSection ? '' : 'mt-3 pt-3 border-top-1 surface-border'}`}>
        <div className="px-2 mb-2 text-xs font-bold uppercase text-color-secondary">
          {item.label}
        </div>
        <ul className="list-none p-0 m-0">
          {item.items.map((child, i) => (
            <AppMenuitem key={child.label} item={child} index={`${index}-${i}`} />
          ))}
        </ul>
      </li>
    );
  }

  const onItemClick = () => {
    if (item.items) {
      toggleIndex(index);
    } else {
      closeMenu();
    }
  };

  const content = (
    <div
      className="p-menuitem-link flex align-items-center cursor-pointer"
      aria-expanded={expanded ? 'true' : 'false'}
    >
      {item.icon && <i className={`${item.icon} mr-2`} />}
      <span>{item.label}</span>
      {item.items && <i className={`pi pi-chevron-${expanded ? 'down' : 'right'} ml-auto`} />}
    </div>
  );

  return (
    <li className={isActiveRoute ? 'active-menuitem' : ''}>
      {item.to ? (
        <Link to={item.to} onClick={onItemClick}>
          {content}
        </Link>
      ) : (
        <div onClick={onItemClick}>{content}</div>
      )}

      {item.items && expanded && (
        <ul className="ml-4 mt-2">
          {item.items.map((child, i) => (
            <AppMenuitem key={child.label} item={child} index={`${index}-${i}`} />
          ))}
        </ul>
      )}
    </li>
  );
}
