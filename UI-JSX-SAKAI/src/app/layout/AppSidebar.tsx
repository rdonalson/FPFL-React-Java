'use client';

import AppMenu from './AppMenu';

interface AppSidebarProps {
    onHide: () => void;
}

export default function AppSidebar({ onHide }: AppSidebarProps) {
    return (
        <div
            className="layout-sidebar"
            onClick={(e) => e.stopPropagation()}
        >
            <AppMenu onItemClick={onHide} />
        </div>
    );
}

