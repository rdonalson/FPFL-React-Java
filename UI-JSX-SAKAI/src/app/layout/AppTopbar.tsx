'use client';

import './AppTopbar.css';

interface AppTopbarProps {
    onToggleSidebar: () => void;
}

export default function AppTopbar({ onToggleSidebar }: AppTopbarProps) {
    return (
        <div className="layout-topbar">
            <span className="layout-topbar-logo">UI-JSX-SAKAI</span>
            <button 
                type="button"
                className="p-link layout-menu-button"
                    onClick={() => {
                    console.log("Hamburger clicked");
                    onToggleSidebar();
                }}

                aria-label="Toggle sidebar"
            >
                <i className="pi pi-bars"></i>
            </button>
            <div className="topbar-spacer"></div>
                

        </div>
    );
}

