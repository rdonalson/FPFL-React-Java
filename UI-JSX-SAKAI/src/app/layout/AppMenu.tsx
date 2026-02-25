'use client';


import { Menu } from 'primereact/menu';
import menuModel from '../../assets/layout/menu/menu.json';

interface AppMenuProps {
    onItemClick?: () => void;
}

export default function AppMenu({ onItemClick }: AppMenuProps) {
    const model = menuModel.map(item => ({
        ...item,
        command: () => {
            if (onItemClick) onItemClick();
        }
    }));

    return <Menu model={model} />;
}

