/* eslint-disable @typescript-eslint/no-explicit-any */
import React, { useState, useContext } from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import { classNames } from 'primereact/utils';
import { LayoutContext } from './context/layoutcontext';

export default function AppMenuitem(props: any) {
    const { item, root, index } = props;
    const { layoutState, setLayoutState } = useContext(LayoutContext);

    const navigate = useNavigate();
    const [active, setActive] = useState(false);

    const hasSubmenu = item.items && item.items.length > 0;

    const onMenuItemClick = (event: React.MouseEvent) => {
        // Disabled item
        if (item.disabled) {
            event.preventDefault();
            return;
        }

        // Toggle submenu
        if (hasSubmenu) {
            setActive((prev) => !prev);
        }

        // Navigation
        if (item.to) {
            navigate(item.to);
        }

        // Close overlay menu on mobile
        if (!hasSubmenu) {
            setLayoutState((prev) => ({
                ...prev,
                overlayMenuActive: false,
                staticMenuMobileActive: false
            }));
        }
    };

    const itemClassName = classNames('layout-menuitem', {
        'active-menuitem': active
    });

    return (
        <li className={itemClassName}>
            {/* GROUP LABEL */}
            {item.label && item.items && root && (
                <div className="layout-menuitem-root-text">{item.label}</div>
            )}

            {/* MENU ITEM */}
            <a
                href={item.to || item.url || '#'}
                onClick={(e) => {
                    e.preventDefault();
                    onMenuItemClick(e);
                }}
                className="p-ripple"
                target={item.target}
                rel={item.target === '_blank' ? 'noopener noreferrer' : undefined}
            >
                {item.icon && <i className={classNames('layout-menuitem-icon', item.icon)}></i>}
                <span className="layout-menuitem-text">{item.label}</span>
                {hasSubmenu && <i className="pi pi-fw pi-angle-down layout-submenu-toggler"></i>}
            </a>

            {/* SUBMENU */}
            {hasSubmenu && (
                <ul className={active ? 'layout-submenu-expanded' : ''}>
                    {item.items.map((child: any, i: number) => {
                        return (
                            <AppMenuitem
                                item={child}
                                index={i}
                                key={child.label}
                            />
                        );
                    })}
                </ul>
            )}
        </li>
    );
}