import React from 'react';
import {useLocation} from 'react-router-dom';
import Header from './Header';
import Sidebar from './Sidebar';
import Footer from './Footer';
import {useAuth} from '../../context/AuthContext';
import './Layout.css';

const Layout = ({ children }) => {
    const { isAuthenticated } = useAuth();
    const location = useLocation();

    const isLandingPage = location.pathname === '/';

    if (isLandingPage && !isAuthenticated) {
        return (
            <>
                {children}
            </>
        );
    }

    return (
        <div className="layout">
            <Header />
            <div className="layout-content">
                {isAuthenticated && <Sidebar />}
                <main className={`layout-main ${!isAuthenticated ? 'layout-main-full' : ''}`}>
                    {children}
                </main>
            </div>
            <Footer />
        </div>
    );
};

export default Layout;