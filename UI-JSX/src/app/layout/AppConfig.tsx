/* eslint-disable @typescript-eslint/no-explicit-any */
import React, { useContext, useEffect, useRef } from 'react';
import { LayoutContext } from './context/layoutcontext';
import { classNames } from 'primereact/utils';
import { Slider } from 'primereact/slider';
import { InputSwitch } from 'primereact/inputswitch';

export default function AppConfig() {
    const { layoutConfig, setLayoutConfig } = useContext(LayoutContext);
    const configRef = useRef<HTMLDivElement>(null);

    // -----------------------------
    // THEME SWITCHING
    // -----------------------------
    const changeTheme = (theme: string, colorScheme: 'light' | 'dark') => {
        const themeLink = document.getElementById('theme-css') as HTMLLinkElement;

        if (themeLink) {
            const newHref = `/themes/${theme}/theme.css`;
            themeLink.setAttribute('href', newHref);
        }

        setLayoutConfig((prev) => ({
            ...prev,
            theme,
            colorScheme
        }));
    };

    // -----------------------------
    // MENU MODE SWITCHING
    // -----------------------------
    const changeMenuMode = (mode: 'static' | 'overlay') => {
        setLayoutConfig((prev) => ({
            ...prev,
            menuMode: mode
        }));
    };

    // -----------------------------
    // INPUT STYLE SWITCHING
    // -----------------------------
    const changeInputStyle = (style: 'outlined' | 'filled') => {
        setLayoutConfig((prev) => ({
            ...prev,
            inputStyle: style
        }));
    };

    // -----------------------------
    // RIPPLE TOGGLE
    // -----------------------------
    const changeRipple = (value: boolean) => {
        setLayoutConfig((prev) => ({
            ...prev,
            ripple: value
        }));
    };

    // -----------------------------
    // FONT SCALE
    // -----------------------------
    const onFontScaleChange = (value: number) => {
        setLayoutConfig((prev) => ({
            ...prev,
            scale: value
        }));
    };

    useEffect(() => {
        document.documentElement.style.fontSize = `${layoutConfig.scale}px`;
    }, [layoutConfig.scale]);

    return (
        <div ref={configRef} className="layout-config">
            <div className="layout-config-content">
                <h5>Theme</h5>

                <div className="flex gap-3 mb-4">
                    <button
                        className={classNames('p-button p-component', {
                            'p-button-outlined': layoutConfig.theme !== 'lara-light-indigo'
                        })}
                        onClick={() => changeTheme('lara-light-indigo', 'light')}
                    >
                        Light
                    </button>

                    <button
                        className={classNames('p-button p-component', {
                            'p-button-outlined': layoutConfig.theme !== 'lara-dark-indigo'
                        })}
                        onClick={() => changeTheme('lara-dark-indigo', 'dark')}
                    >
                        Dark
                    </button>
                </div>

                <h5>Menu Mode</h5>
                <div className="flex gap-3 mb-4">
                    <button
                        className={classNames('p-button p-component', {
                            'p-button-outlined': layoutConfig.menuMode !== 'static'
                        })}
                        onClick={() => changeMenuMode('static')}
                    >
                        Static
                    </button>

                    <button
                        className={classNames('p-button p-component', {
                            'p-button-outlined': layoutConfig.menuMode !== 'overlay'
                        })}
                        onClick={() => changeMenuMode('overlay')}
                    >
                        Overlay
                    </button>
                </div>

                <h5>Input Style</h5>
                <div className="flex gap-3 mb-4">
                    <button
                        className={classNames('p-button p-component', {
                            'p-button-outlined': layoutConfig.inputStyle !== 'outlined'
                        })}
                        onClick={() => changeInputStyle('outlined')}
                    >
                        Outlined
                    </button>

                    <button
                        className={classNames('p-button p-component', {
                            'p-button-outlined': layoutConfig.inputStyle !== 'filled'
                        })}
                        onClick={() => changeInputStyle('filled')}
                    >
                        Filled
                    </button>
                </div>

                <h5>Ripple Effect</h5>
                <InputSwitch
                    checked={layoutConfig.ripple}
                    onChange={(e) => changeRipple(e.value)}
                />

                <h5 className="mt-4">Font Size</h5>
                <div className="flex align-items-center gap-3">
                    <i className="pi pi-minus" />
                    <Slider
                        value={layoutConfig.scale}
                        onChange={(e) => onFontScaleChange(e.value as number)}
                        min={10}
                        max={20}
                        style={{ width: '12rem' }}
                    />
                    <i className="pi pi-plus" />
                </div>
            </div>
        </div>
    );
}