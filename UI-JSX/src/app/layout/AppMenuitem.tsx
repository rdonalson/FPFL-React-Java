import { Link, useLocation } from 'react-router-dom';
import { useMenuContext } from './context/MenuContext';
import { useState } from 'react';

interface AppMenuitemProps {
  item: any;
  index: number;
  root?: boolean;
}

export default function AppMenuitem({ item, index }: AppMenuitemProps) {
  const location = useLocation();
  const { activeIndex, setActiveIndex, onMenuToggle } = useMenuContext();
  const [expanded, setExpanded] = useState(false);

  const isActiveRoute = item.to && location.pathname === item.to;
  const isActive = activeIndex === index;

  const onItemClick = () => {
    if (item.items) {
      setExpanded(!expanded);
      setActiveIndex(isActive ? null : index);
    } else {
      onMenuToggle(); // close sidebar
    }
  };

  const content = (
    <div className="p-menuitem-link flex items-center cursor-pointer">
      {item.icon && <i className={`${item.icon} mr-2`} />}
      <span>{item.label}</span>
      {item.items && <i className={`pi pi-chevron-${expanded ? 'down' : 'right'} ml-auto`} />}
    </div>
  );

  return (
    <li className={isActiveRoute ? 'active-menuitem' : ''}>
      {/* If the item has a route, wrap the content in a Link */}
      {item.to ? (
        <Link to={item.to} onClick={onItemClick}>
          {content}
        </Link>
      ) : (
        <div onClick={onItemClick}>{content}</div>
      )}

      {/* CHILDREN */}
      {item.items && expanded && (
        <ul className="ml-4 mt-2">
          {item.items.map((child: any, i: number) => (
            <AppMenuitem item={child} index={i} key={child.label} />
          ))}
        </ul>
      )}
    </li>
  );
}
