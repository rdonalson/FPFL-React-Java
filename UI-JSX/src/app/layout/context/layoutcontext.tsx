import React, { createContext, useState } from 'react';

export interface LayoutConfig {
    ripple: boolean;
    inputStyle: 'outlined' | 'filled';
    menuMode: 'static' | 'overlay';
    colorScheme: 'light' | 'dark';
    theme: string; // lara-light-indigo or lara-dark-indigo
    scale: number;
}

export interface LayoutState {
    staticMenuDesktopInactive: boolean;
    overlayMenuActive: boolean;
    profileSidebarVisible: boolean;
    staticMenuMobileActive: boolean;
    menuHoverActive: boolean;
}

export interface LayoutContextType {
    layoutConfig: LayoutConfig;
    setLayoutConfig: React.Dispatch<React.SetStateAction<LayoutConfig>>;
    layoutState: LayoutState;
    setLayoutState: React.Dispatch<React.SetStateAction<LayoutState>>;
}

export const LayoutContext = createContext<LayoutContextType>({
    layoutConfig: {
        ripple: true,
        inputStyle: 'outlined',
        menuMode: 'static',
        colorScheme: 'light',
        theme: 'lara-light-indigo',   // DEFAULT THEME
        scale: 14
    },
    setLayoutConfig: () => {},
    layoutState: {
        staticMenuDesktopInactive: false,
        overlayMenuActive: true,
        profileSidebarVisible: false,
        staticMenuMobileActive: false,
        menuHoverActive: false
    },
    setLayoutState: () => {}
});

export const LayoutProvider = ({ children }: { children: React.ReactNode }) => {
    const [layoutConfig, setLayoutConfig] = useState<LayoutConfig>({
        ripple: true,
        inputStyle: 'outlined',
        menuMode: 'static',
        colorScheme: 'light',
        theme: 'lara-light-indigo',   // DEFAULT THEME
        scale: 14
    });

    const [layoutState, setLayoutState] = useState<LayoutState>({
        staticMenuDesktopInactive: false,
        overlayMenuActive: false,
        profileSidebarVisible: false,
        staticMenuMobileActive: false,
        menuHoverActive: false
    });

    return (
        <LayoutContext.Provider
            value={{
                layoutConfig,
                setLayoutConfig,
                layoutState,
                setLayoutState
            }}
        >
            {children}
        </LayoutContext.Provider>
    );
};