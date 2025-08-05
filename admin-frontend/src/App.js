import React from 'react';
import {BrowserRouter as Router, Navigate, Route, Routes} from 'react-router-dom';
import {AuthProvider, useAuth} from './context/AuthContext';
import {ThemeProvider} from './context/ThemeContext';
import {ToastProvider} from './context/ToastContext';
import Layout from './components/layout/Layout';
import LandingPage from './pages/LandingPage';
import Dashboard from './pages/Dashboard';
import Login from './pages/Login';
import Register from './pages/Register';
import ProductsPage from './pages/ProductsPage';
import ProductFormPage from './pages/ProductFormPage';
import OrdersPage from './pages/OrdersPage';
import SettingsPage from './pages/SettingsPage';
import LoadingScreen from './components/common/LoadingScreen';
import ToastContainer from './components/common/ToastContainer';
import OrderDetailPage from "./pages/OrderDetailPage";
import './styles/base.css';

const ProtectedRoute = ({children}) => {
    const {isAuthenticated, loading} = useAuth();

    if (loading) return <LoadingScreen/>;

    if (!isAuthenticated) {
        return <Navigate to={`/login`}/>;
    }

    return children;
};

const GuestRoute = ({children}) => {
    const {isAuthenticated, loading} = useAuth();

    if (loading) return <LoadingScreen/>;

    if (isAuthenticated) {
        return <Navigate to={`/dashboard`}/>;
    }

    return children;
};

function AppRoutes() {
    const {isAuthenticated} = useAuth();

    return (
        <Routes>
            <Route path={`/`} element={
                isAuthenticated ? <Navigate to={`/dashboard`}/> : <LandingPage/>
            }/>
            <Route path={`/login`} element={
                <GuestRoute>
                    <Login/>
                </GuestRoute>
            }/>
            <Route path={`/register`} element={
                <GuestRoute>
                    <Register/>
                </GuestRoute>
            }/>
            <Route path={`/dashboard`} element={
                <ProtectedRoute>
                    <Dashboard/>
                </ProtectedRoute>
            }/>
            <Route path={`/products`} element={
                <ProtectedRoute>
                    <ProductsPage/>
                </ProtectedRoute>
            }/>
            <Route path={`/products/new`} element={
                <ProtectedRoute>
                    <ProductFormPage/>
                </ProtectedRoute>
            }/>
            <Route path={`/products/:productId`} element={
                <ProtectedRoute>
                    <ProductFormPage/>
                </ProtectedRoute>
            }/>
            <Route path={`/orders`} element={
                <ProtectedRoute>
                    <OrdersPage/>
                </ProtectedRoute>
            }/>
            <Route path={`/orders/:orderId`} element={
                <ProtectedRoute>
                    <OrderDetailPage/>
                </ProtectedRoute>
            }/>
            <Route path={`/settings`} element={
                <ProtectedRoute>
                    <SettingsPage/>
                </ProtectedRoute>
            }/>
            <Route path="*" element={<Navigate to="/" />}/>
        </Routes>
    );
}

function App() {
    return (
        <ThemeProvider>
            <ToastProvider>
                <AuthProvider>
                    <Router basename="/admin">
                        <Layout>
                            <AppRoutes/>
                        </Layout>
                        <ToastContainer/>
                    </Router>
                </AuthProvider>
            </ToastProvider>
        </ThemeProvider>
    );
}

export default App;