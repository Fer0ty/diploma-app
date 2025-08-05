import React from 'react';
import {Link, useLocation, useNavigate} from 'react-router-dom';
import {useAuth} from '../../hooks/useAuth';
import {useTheme} from '../../context/ThemeContext';
import {useToast} from '../../context/ToastContext';
import LogoutIcon from '../../assets/icons/LogoutIcon';
import SunIcon from '../../assets/icons/SunIcon';
import MoonIcon from '../../assets/icons/MoonIcon';
import './Header.css';

const Header = () => {
    const {isAuthenticated, logout, user} = useAuth();
    const {isDarkMode, toggleTheme} = useTheme();
    const {showSuccess} = useToast();
    const navigate = useNavigate();
    const location = useLocation();

    const isLandingPage = location.pathname === '/';

    if (isLandingPage && !isAuthenticated) {
        return null;
    }

    const handleLogout = () => {
        logout();
        showSuccess('Вы успешно вышли из аккаунта');
        navigate('/');
    };

    return (
        <header className="header">
            <div className="header-title">
                <Link to={isAuthenticated ? "/dashboard" : "/"}>Редактор Магазина</Link>
            </div>
            <div className="header-auth-controls">
                <button
                    className="theme-toggle"
                    onClick={toggleTheme}
                    aria-label={isDarkMode ? 'Переключить на светлую тему' : 'Переключить на темную тему'}
                >
                    {isDarkMode ? <SunIcon width={18} height={18}/> : <MoonIcon width={18} height={18}/>}
                </button>

                {isAuthenticated ? (
                    <div className="header-user">
                        <span className="header-username">{user?.username || 'Пользователь'}</span>
                        <button className="header-logout-button" onClick={handleLogout}>
                            <LogoutIcon width={16} height={16}/>
                            <span className="header-logout-text">Выйти</span>
                        </button>
                    </div>
                ) : (
                    <nav className="header-nav">
                        <Link to="/login" className="header-nav-item">Вход</Link>
                        <Link to="/register" className="header-nav-item">Регистрация</Link>
                    </nav>
                )}
            </div>
        </header>
    );
};

export default Header;