import React from 'react';
import {useLocation} from 'react-router-dom';
import {useAuth} from '../../hooks/useAuth';
import './Footer.css';

const Footer = () => {
    const location = useLocation();
    const {isAuthenticated} = useAuth();

    const isLandingPage = location.pathname === '/';

    if (isLandingPage && !isAuthenticated) {
        return null;
    }

    return (
        <footer className="footer">
        </footer>
    );
};

export default Footer;