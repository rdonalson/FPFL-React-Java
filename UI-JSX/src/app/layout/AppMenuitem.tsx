// src/app/layout/AppMenuitem.tsx

import { Link, useLocation } from 'react-router-dom';
import { useMenuContext } from './context/MenuContext';
import { useEffect } from 'react';
import type { MenuItem } from './model/menuModel';

interface Props {
  item: MenuItem;
  index: string;
  root?: boolean;
}

export default function AppMenuitem({ item, index }: Props) {
  const location = useLocation();
  const { activeIndex, setActiveIndex, onMenuToggle } = useMenuContext();

  const isActiveRoute = item.to === location.pathname;
  const isActive = activeIndex === index;
  const expanded = isActive;

  useEffect(() => {
    if (isActiveRoute) {
      setActiveIndex(index);
    }
  }, [isActiveRoute, index, setActiveIndex]);

  const onItemClick = () => {
    if (item.items) {
      setActiveIndex(isActive ? null : index);
    } else {
      onMenuToggle();
    }
  };

  const content = (
    <div
      className="p-menuitem-link flex items-center cursor-pointer"
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
