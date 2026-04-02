import React, { useContext } from 'react';
import { LayoutContext } from './context/layoutcontext';
import AppMenu from './AppMenu';
import { classNames } from 'primereact/utils';

export default function AppSidebar() {
    const { layoutConfig, layoutState } = useContext(LayoutContext);

    // Full Sakai menu model
    const model = [
        {
            label: 'Home',
            items: [
                { label: 'Dashboard', icon: 'pi pi-fw pi-home', to: '/' }
            ]
        },
        {
            label: 'Items',
            items: [
                { label: 'Item Types', icon: 'pi pi-fw pi-list', to: '/item-types' },
                { label: 'Specific Item', icon: 'pi pi-fw pi-tag', to: '/items/1' }
            ]
        },
        {
            label: 'Pages',
            items: [
                { label: 'Credits', icon: 'pi pi-fw pi-info-circle', to: '/credits' },
                { label: 'Docs', icon: 'pi pi-fw pi-book', to: '/docs' }
            ]
        }
    ];

    return (
        <div
            className={classNames('layout-sidebar', {
                'layout-sidebar-light': layoutConfig.colorScheme === 'dark'
            })}
        >
            {/* LOGO */}
            <div className="layout-logo">
                <img
                    src={`/images/logo-${layoutConfig.colorScheme === 'light' ? 'dark' : 'white'}.svg`}
                    alt="logo"
                />
                <span>SAKAI</span>
            </div>

            {/* MENU */}
            <AppMenu model={model} />
        </div>
    );
}