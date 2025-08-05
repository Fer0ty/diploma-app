import React, {useEffect, useState} from 'react';
import {Link} from 'react-router-dom';
import {useTheme} from '../../context/ThemeContext';
import './LandingNav.css';

const LandingNav = () => {
    const {isDarkMode, toggleTheme} = useTheme();
    const [scrolled, setScrolled] = useState(false);

    useEffect(() => {
        const handleScroll = () => {
            if (window.scrollY > 50) {
                setScrolled(true);
            } else {
                setScrolled(false);
            }
        };

        window.addEventListener('scroll', handleScroll);

        return () => {
            window.removeEventListener('scroll', handleScroll);
        };
    }, []);

    return (
        <nav className={`landing-nav ${scrolled ? 'scrolled' : ''}`}>
            <div className="landing-nav-container">
                <div className="landing-nav-logo">
                    <Link to="/">Редактор Магазина</Link>
                </div>

                <div className="landing-nav-right">
                    <button
                        className="landing-theme-toggle"
                        onClick={toggleTheme}
                        aria-label={isDarkMode ? 'Переключить на светлую тему' : 'Переключить на темную тему'}
                    >
                        {isDarkMode ? '☀️' : '🌙'}
                    </button>

                    <Link to="/login" className="landing-nav-login">Вход</Link>
                    <Link to="/register" className="landing-nav-register">Регистрация</Link>
                </div>
            </div>
        </nav>
    );
};

export default LandingNav;