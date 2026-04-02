/* eslint-disable @typescript-eslint/no-explicit-any */
import React, { forwardRef, useContext, useImperativeHandle, useRef } from 'react';
import { classNames } from 'primereact/utils';
import { LayoutContext } from './context/layoutcontext';
import { Link } from 'react-router-dom';
import { AppTopbarRef } from '@/types';

const AppTopbar = forwardRef<AppTopbarRef>((props, ref) => {
    const { layoutConfig, layoutState, setLayoutState } = useContext(LayoutContext);

    const menubuttonRef = useRef<HTMLButtonElement>(null);
    const topbarmenuRef = useRef<HTMLDivElement>(null);
    const topbarmenubuttonRef = useRef<HTMLButtonElement>(null);

    // Expose refs to AppLayout (for outside-click detection)
    useImperativeHandle(ref, () => ({
        menubutton: menubuttonRef.current,
        topbarmenu: topbarmenuRef.current,
        topbarmenubutton: topbarmenubuttonRef.current
    }));

    const onMenuToggle = () => {
        setLayoutState((prev) => ({
            ...prev,
            overlayMenuActive: !prev.overlayMenuActive
        }));
    };

    const showProfileSidebar = () => {
        setLayoutState((prev) => ({
            ...prev,
            profileSidebarVisible: !prev.profileSidebarVisible
        }));
    };

    return (
        <div className="layout-topbar">
            {/* LOGO */}
            <Link to="/" className="layout-topbar-logo">
                <img
                    src={`/images/logo-${layoutConfig.colorScheme === 'light' ? 'dark' : 'white'}.svg`}
                    width="47.22"
                    height="35"
                    alt="logo"
                />
                <span>SAKAI</span>
            </Link>

            {/* MENU BUTTON (hamburger) */}
            <button
                ref={menubuttonRef}
                type="button"
                className="p-link layout-menu-button layout-topbar-button"
                onClick={onMenuToggle}
                aria-label="Toggle menu"
            >
                <i className="pi pi-bars" />
            </button>

            {/* PROFILE MENU BUTTON (ellipsis) */}
            <button
                ref={topbarmenubuttonRef}
                type="button"
                className="p-link layout-topbar-menu-button layout-topbar-button"
                onClick={showProfileSidebar}
                aria-label="Show profile menu"
            >
                <i className="pi pi-ellipsis-v" />
            </button>

            {/* TOPBAR MENU (Calendar, Profile, Settings) */}
            <div
                ref={topbarmenuRef}
                className={classNames('layout-topbar-menu', {
                    'layout-topbar-menu-mobile-active': layoutState.profileSidebarVisible
                })}
            >
                <button type="button" className="p-link layout-topbar-button">
                    <i className="pi pi-calendar"></i>
                    <span>Calendar</span>
                </button>

                <button type="button" className="p-link layout-topbar-button">
                    <i className="pi pi-user"></i>
                    <span>Profile</span>
                </button>

                <Link to="/documentation">
                    <button type="button" className="p-link layout-topbar-button">
                        <i className="pi pi-cog"></i>
                        <span>Settings</span>
                    </button>
                </Link>
            </div>
        </div>
    );
});

AppTopbar.displayName = 'AppTopbar';

export default AppTopbar;