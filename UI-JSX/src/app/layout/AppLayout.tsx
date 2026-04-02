import React, { useContext, useEffect, useRef } from 'react';
import { useEventListener, useUnmountEffect } from 'primereact/hooks';
import { classNames } from 'primereact/utils';
import { LayoutContext } from './context/layoutcontext';
import { LayoutState, AppTopbarRef } from '@/types';
import { useLocation, Outlet } from 'react-router-dom';

import AppTopbar from './AppTopbar';
import AppSidebar from './AppSidebar';
import AppFooter from './AppFooter';
import AppConfig from './AppConfig';

export default function AppLayout() {
    const { layoutConfig, layoutState, setLayoutState } = useContext(LayoutContext);

    const topbarRef = useRef<AppTopbarRef>(null);
    const sidebarRef = useRef<HTMLDivElement>(null);

    const location = useLocation();

    // -----------------------------
    // MENU OUTSIDE CLICK LISTENER
    // -----------------------------
    const [bindMenuOutsideClickListener, unbindMenuOutsideClickListener] = useEventListener({
        type: 'click',
        listener: (event) => {
            const isOutsideClicked = !(
                sidebarRef.current?.isSameNode(event.target as Node) ||
                sidebarRef.current?.contains(event.target as Node) ||
                topbarRef.current?.menubutton?.isSameNode(event.target as Node) ||
                topbarRef.current?.menubutton?.contains(event.target as Node)
            );

            if (isOutsideClicked) hideMenu();
        }
    });

    // -----------------------------
    // PROFILE MENU OUTSIDE CLICK
    // -----------------------------
    const [bindProfileMenuOutsideClickListener, unbindProfileMenuOutsideClickListener] = useEventListener({
        type: 'click',
        listener: (event) => {
            const isOutsideClicked = !(
                topbarRef.current?.topbarmenu?.isSameNode(event.target as Node) ||
                topbarRef.current?.topbarmenu?.contains(event.target as Node) ||
                topbarRef.current?.topbarmenubutton?.isSameNode(event.target as Node) ||
                topbarRef.current?.topbarmenubutton?.contains(event.target as Node)
            );

            if (isOutsideClicked) hideProfileMenu();
        }
    });

    // -----------------------------
    // ROUTE CHANGE → CLOSE MENUS
    // -----------------------------
    useEffect(() => {
        hideMenu();
        hideProfileMenu();
    }, [location.pathname]);

    // -----------------------------
    // MENU CONTROL FUNCTIONS
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

    const blockBodyScroll = () => {
        document.body.classList.add('blocked-scroll');
    };

    const unblockBodyScroll = () => {
        document.body.classList.remove('blocked-scroll');
    };

    // -----------------------------
    // MENU ACTIVE EFFECTS
    // -----------------------------
    useEffect(() => {
        if (layoutState.overlayMenuActive || layoutState.staticMenuMobileActive) {
            bindMenuOutsideClickListener();
        }

        if (layoutState.staticMenuMobileActive) {
            blockBodyScroll();
        }
    }, [layoutState.overlayMenuActive, layoutState.staticMenuMobileActive]);

    // -----------------------------
    // PROFILE MENU EFFECTS
    // -----------------------------
    useEffect(() => {
        if (layoutState.profileSidebarVisible) {
            bindProfileMenuOutsideClickListener();
        }
    }, [layoutState.profileSidebarVisible]);

    // -----------------------------
    // CLEANUP
    // -----------------------------
    useUnmountEffect(() => {
        unbindMenuOutsideClickListener();
        unbindProfileMenuOutsideClickListener();
    });

    // -----------------------------
    // LAYOUT CSS CLASSES
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
                <div className="layout-main">
                    <Outlet />
                </div>
                <AppFooter />
            </div>

            <AppConfig />

            <div className="layout-mask"></div>
        </div>
    );
}