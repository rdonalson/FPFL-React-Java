import React, { useContext, useEffect, useRef } from 'react';
import { useEventListener, useUnmountEffect } from 'primereact/hooks';
import { classNames } from 'primereact/utils';
import { PrimeReactContext } from 'primereact/api';
import { useLocation } from 'react-router-dom';

import AppFooter from './AppFooter';
import AppSidebar from './AppSidebar';
import AppTopbar from './AppTopbar';
import AppConfig from './AppConfig';

import { LayoutContext } from './context/layoutcontext';
import { ChildContainerProps, LayoutState, AppTopbarRef } from '@/types';

const Layout = ({ children }: ChildContainerProps) => {
    const { layoutConfig, layoutState, setLayoutState } = useContext(LayoutContext);
    const { setRipple } = useContext(PrimeReactContext);

    const topbarRef = useRef<AppTopbarRef>(null);
    const sidebarRef = useRef<HTMLDivElement>(null);

    const location = useLocation(); // <-- replaces Next.js pathname/searchParams

    // -----------------------------
    // Outside click listeners
    // -----------------------------
    const [bindMenuOutsideClickListener, unbindMenuOutsideClickListener] = useEventListener({
        type: 'click',
        listener: (event) => {
            const isOutsideClicked = !(
                sidebarRef.current?.contains(event.target as Node) ||
                topbarRef.current?.menubutton?.contains(event.target as Node)
            );

            if (isOutsideClicked) hideMenu();
        }
    });

    const [bindProfileMenuOutsideClickListener, unbindProfileMenuOutsideClickListener] = useEventListener({
        type: 'click',
        listener: (event) => {
            const isOutsideClicked = !(
                topbarRef.current?.topbarmenu?.contains(event.target as Node) ||
                topbarRef.current?.topbarmenubutton?.contains(event.target as Node)
            );

            if (isOutsideClicked) hideProfileMenu();
        }
    });

    // -----------------------------
    // Hide menus on route change
    // -----------------------------
    useEffect(() => {
        hideMenu();
        hideProfileMenu();
    }, [location.pathname, location.search]);

    // -----------------------------
    // Menu hide helpers
    // -----------------------------
    const hideMenu = () => {
        setLayoutState((prev: LayoutState) => ({
            ...prev,
            overlayMenuActive: false,
            staticMenuMobileActive: false,
            menuHoverActive: false
        }));

        unbindMenuOutsideClickListener();
        unblockBodyScroll();
    };

    const hideProfileMenu = () => {
        setLayoutState((prev: LayoutState) => ({
            ...prev,
            profileSidebarVisible: false
        }));

        unbindProfileMenuOutsideClickListener();
    };

    // -----------------------------
    // Scroll blocking
    // -----------------------------
    const blockBodyScroll = () => document.body.classList.add('blocked-scroll');
    const unblockBodyScroll = () => document.body.classList.remove('blocked-scroll');

    // -----------------------------
    // Bind listeners when menus open
    // -----------------------------
    useEffect(() => {
        if (layoutState.overlayMenuActive || layoutState.staticMenuMobileActive) {
            bindMenuOutsideClickListener();
        }

        if (layoutState.staticMenuMobileActive) blockBodyScroll();
    }, [layoutState.overlayMenuActive, layoutState.staticMenuMobileActive]);

    useEffect(() => {
        if (layoutState.profileSidebarVisible) {
            bindProfileMenuOutsideClickListener();
        }
    }, [layoutState.profileSidebarVisible]);

    useUnmountEffect(() => {
        unbindMenuOutsideClickListener();
        unbindProfileMenuOutsideClickListener();
    });

    // -----------------------------
    // Container classes
    // -----------------------------
    const containerClass = classNames('layout-wrapper', {
        'layout-overlay': layoutConfig.menuMode === 'overlay',
        'layout-static': layoutConfig.menuMode === 'static',
        'layout-static-inactive': layoutState.staticMenuDesktopInactive && layoutConfig.menuMode === 'static',
        'layout-overlay-active': layoutState.overlayMenuActive,
        'layout-mobile-active': layoutState.staticMenuMobileActive,
        'p-input-filled': layoutConfig.inputStyle === 'filled',
        'p-ripple-disabled': !layoutConfig.ripple
    });

    return (
        <div className={containerClass}>
            <AppTopbar ref={topbarRef} />

            <div ref={sidebarRef} className="layout-sidebar">
                <AppSidebar />
            </div>

            <div className="layout-main-container">
                <div className="layout-main">{children}</div>
                <AppFooter />
            </div>

            <AppConfig />
            <div className="layout-mask"></div>
        </div>
    );
};

export default Layout;