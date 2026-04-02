import React from 'react';
import AppMenuitem from './AppMenuitem';

export default function AppMenu(props: any) {
    return (
        <ul className="layout-menu">
            {props.model &&
                props.model.map((item: any, i: number) => {
                    return !item.separator ? (
                        <AppMenuitem
                            item={item}
                            root={true}
                            index={i}
                            key={item.label}
                        />
                    ) : (
                        <li className="menu-separator" key={`separator-${i}`}></li>
                    );
                })}
        </ul>
    );
}