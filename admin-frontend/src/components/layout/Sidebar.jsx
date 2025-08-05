import React from 'react';
import {Link, useLocation} from 'react-router-dom';
import {useAuth} from '../../hooks/useAuth';
import './Sidebar.css';

const DashboardIcon = () => <span>📊</span>;
const ProductsIcon = () => <span>📦</span>;
const OrdersIcon = () => <span>🛒</span>;
const SettingsIcon = () => <span>⚙️</span>;

const menuItems = [
    {text: 'Дашборд', icon: <DashboardIcon/>, path: '/dashboard'},
    {text: 'Товары', icon: <ProductsIcon/>, path: '/products'},
    {text: 'Заказы', icon: <OrdersIcon/>, path: '/orders'},
    {text: 'Настройки', icon: <SettingsIcon/>, path: '/settings'},
];

const Sidebar = () => {
    const {isAuthenticated, user} = useAuth();
    const location = useLocation();

    if (!isAuthenticated) return null;

    return (
        <div className="sidebar">
            <div className="sidebar-header">
                <div className="sidebar-store-name">
                    {user?.storeName || 'Мой Магазин'}
                </div>
            </div>
            <ul className="sidebar-menu">
                {menuItems.map((item) => (
                    <Link
                        key={item.text}
                        to={item.path}
                        className={`sidebar-menu-item ${location.pathname === item.path ? 'active' : ''}`}
                    >
                        <span className="sidebar-menu-icon">{item.icon}</span>
                        <span className="sidebar-menu-text">{item.text}</span>
                    </Link>
                ))}
            </ul>
        </div>
    );
};

export default Sidebar;