import React, { useEffect, useContext } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { Ripple } from 'primereact/ripple';
import { classNames } from 'primereact/utils';
import { CSSTransition } from 'react-transition-group';

import { MenuContext } from './context/menucontext';
import { AppMenuItemProps } from '@/types';

const AppMenuitem = (props: AppMenuItemProps) => {
    const { activeMenu, setActiveMenu } = useContext(MenuContext);
    const item = props.item;

    const location = useLocation(); // replaces usePathname + useSearchParams
    const currentPath = location.pathname + location.search;

    const key = props.parentKey ? `${props.parentKey}-${props.index}` : String(props.index);

    const isActiveRoute = item?.to && currentPath === item.to;
    const active = activeMenu === key || activeMenu.startsWith(key + '-');

    // -----------------------------
    // Handle route change
    // -----------------------------
    useEffect(() => {
        if (item?.to && item.to === currentPath) {
            setActiveMenu(key);
        }
    }, [currentPath]);

    // -----------------------------
    // Click handler
    // -----------------------------
    const itemClick = (event: React.MouseEvent<HTMLAnchorElement | HTMLButtonElement>) => {
        if (item?.disabled) {
            event.preventDefault();
            return;
        }

        if (item?.command) {
            item.command({ originalEvent: event, item });
        }

        if (item?.items) {
            setActiveMenu(active ? (props.parentKey as string) : key);
        } else {
            setActiveMenu(key);
        }
    };

    // -----------------------------
    // Submenu rendering
    // -----------------------------
    const subMenu =
        item?.items &&
        item?.visible !== false && (
            <CSSTransition
                timeout={{ enter: 1000, exit: 450 }}
                classNames="layout-submenu"
                in={props.root ? true : active}
                key={item?.label}
                unmountOnExit
            >
                <ul>
                    {item.items.map((child, i) => (
                        <AppMenuitem
                            item={child}
                            index={i}
                            className={child.badgeClass}
                            parentKey={key}
                            key={child?.label}
                        />
                    ))}
                </ul>
            </CSSTransition>
        );

    // -----------------------------
    // Render
    // -----------------------------
    return (
        <li className={classNames({ 'layout-root-menuitem': props.root, 'active-menuitem': active })}>
            {props.root && item?.visible !== false && (
                <div className="layout-menuitem-root-text">{item?.label}</div>
            )}

            {/* INTERNAL NAVIGATION (React Router) */}
            {item?.to && !item.items && item.visible !== false && (
                <Link
                    to={item.to}
                    replace={item.replaceUrl}
                    target={item.target}
                    onClick={itemClick}
                    className={classNames(item.class, 'p-ripple', { 'active-route': isActiveRoute })}
                    tabIndex={0}
                >
                    <i className={classNames('layout-menuitem-icon', item.icon)}></i>
                    <span className="layout-menuitem-text">{item.label}</span>
                    <Ripple />
                </Link>
            )}

            {/* EXTERNAL LINKS OR MENU TOGGLES */}
            {(!item?.to || item.items) && item?.visible !== false && (
                <a
                    href={item?.url}
                    onClick={itemClick}
                    className={classNames(item?.class, 'p-ripple')}
                    target={item?.target}
                    tabIndex={0}
                >
                    <i className={classNames('layout-menuitem-icon', item?.icon)}></i>
                    <span className="layout-menuitem-text">{item?.label}</span>
                    {item?.items && <i className="pi pi-fw pi-angle-down layout-submenu-toggler"></i>}
                    <Ripple />
                </a>
            )}

            {subMenu}
        </li>
    );
};

export default AppMenuitem;