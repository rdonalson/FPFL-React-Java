'use client';

import { Menu } from 'primereact/menu';
import menuModel from '../../assets/layout/menu/menu.json';

export default function AppMenu() {
    return <Menu model={menuModel} />;
}
