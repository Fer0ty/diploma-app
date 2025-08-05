import React, { createContext, useEffect, useState, useContext } from 'react';
import { jwtDecode } from 'jwt-decode';

export const AuthContext = createContext();

export const useAuth = () => {
    const context = useContext(AuthContext);
    if (context === undefined) {
        throw new Error('useAuth must be used within an AuthProvider');
    }
    return context;
};

export const AuthProvider = ({ children }) => {
    const [user, setUser] = useState(null);
    const [isAuthenticated, setIsAuthenticated] = useState(false);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    // Добавим функцию для API-запросов с автоматическим включением токена
    const fetchWithAuth = async (url, options = {}) => {
        const token = localStorage.getItem('token');
        const headers = {
            'Content-Type': 'application/json',
            ...options.headers,
        };

        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }

        // URL конвертируем, чтобы всегда начинался с /api/
        const apiUrl = url.startsWith('/api/') ? url : `/api${url.startsWith('/') ? url : '/' + url}`;

        console.log(`Making authenticated fetch to: ${apiUrl}`);

        try {
            const response = await fetch(apiUrl, {
                ...options,
                headers,
            });

            // Проверка на истекший токен или ошибку аутентификации
            if (response.status === 401) {
                console.warn('Authentication failed during API request, logging out...');
                logout();
                throw new Error('Authentication failed. Please login again.');
            }

            return response;
        } catch (err) {
            console.error('API request failed:', err);
            throw err;
        }
    };

    // Улучшим инициализацию для более надежной обработки токена
    useEffect(() => {
        const initAuth = async () => {
            console.log('Initializing authentication...');
            const token = localStorage.getItem('token');
            if (!token) {
                console.log('No token found in localStorage');
                setLoading(false);
                return;
            }

            try {
                // Проверка токена и извлечение данных
                const decoded = jwtDecode(token);
                console.log('Token decoded during initialization:', decoded);

                if (decoded.exp * 1000 <= Date.now()) {
                    console.warn('Token expired, logging out');
                    localStorage.removeItem('token');
                    localStorage.removeItem('user');
                    setLoading(false);
                    return;
                }

                // Также проверим токен через бэкенд (опционально)
                try {
                    const validateResponse = await fetch('/api/v1/auth/validate', {
                        method: 'GET',
                        headers: {
                            'Authorization': `Bearer ${token}`
                        }
                    });

                    if (!validateResponse.ok) {
                        console.warn('Token validation failed on server, logging out');
                        localStorage.removeItem('token');
                        localStorage.removeItem('user');
                        setLoading(false);
                        return;
                    }

                    console.log('Token validated successfully on server');
                } catch (validationError) {
                    console.warn('Token validation error:', validationError);
                    // Продолжаем, даже если валидация не удалась - это опциональный шаг
                }

                let userData;
                const storedUser = localStorage.getItem('user');

                if (storedUser) {
                    userData = JSON.parse(storedUser);
                    if (!userData.tenantId && decoded.tenant_id) {
                        userData = {
                            ...userData,
                            tenantId: decoded.tenant_id
                        };
                        localStorage.setItem('user', JSON.stringify(userData));
                    }
                } else {
                    userData = {
                        username: decoded.sub,
                        tenantId: decoded.tenant_id,
                        role: decoded.roles ? decoded.roles.split(',')[0] : 'ROLE_ADMIN',
                        tenantName: decoded.tenantName || 'Мой магазин'
                    };
                    localStorage.setItem('user', JSON.stringify(userData));
                }

                console.log('Auth initialized with user:', userData);
                setUser(userData);
                setIsAuthenticated(true);
            } catch (error) {
                console.error('Invalid token during initialization:', error);
                localStorage.removeItem('token');
                localStorage.removeItem('user');
            } finally {
                setLoading(false);
            }
        };

        initAuth();
    }, []);

    // Функция аутентификации через API
    const loginWithCredentials = async (username, password) => {
        setLoading(true);
        setError(null);

        try {
            console.log('Attempting login with username:', username);

            const response = await fetch('/api/v1/auth/login', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ username, password }),
            });

            if (!response.ok) {
                const errorData = await response.json().catch(() => ({}));
                const errorMessage = errorData.message || 'Authentication failed';
                console.error('Login failed:', errorMessage);
                setError(errorMessage);
                setLoading(false);
                return false;
            }

            const data = await response.json();

            if (!data.token) {
                console.error('Login API did not return a token');
                setError('No authentication token received');
                setLoading(false);
                return false;
            }

            console.log('Login successful, received token');

            // Используем существующую функцию для сохранения токена и данных пользователя
            login(data.token);
            setLoading(false);
            return true;

        } catch (error) {
            console.error('Login error:', error);
            setError('An error occurred during login');
            setLoading(false);
            return false;
        }
    };

    const login = (token, userData = null) => {
        if (!token) {
            console.error('Login failed: No token provided');
            return;
        }

        localStorage.setItem('token', token);

        try {
            const decoded = jwtDecode(token);
            console.log('Token decoded during login:', decoded);

            const updatedUserData = userData ? { ...userData } : {};

            updatedUserData.username = updatedUserData.username || decoded.sub;
            updatedUserData.tenantId = updatedUserData.tenantId || decoded.tenant_id;
            updatedUserData.role = updatedUserData.role || (decoded.roles ? decoded.roles.split(',')[0] : 'ROLE_ADMIN');
            updatedUserData.tenantName = updatedUserData.tenantName || decoded.tenantName || 'Мой магазин';
            updatedUserData.subdomain = updatedUserData.subdomain || decoded.subdomain;

            console.log('User data after login:', updatedUserData);

            localStorage.setItem('user', JSON.stringify(updatedUserData));
            setUser(updatedUserData);
            setIsAuthenticated(true);
            setError(null);
        } catch (error) {
            console.error('Error processing token during login:', error);

            if (userData) {
                localStorage.setItem('user', JSON.stringify(userData));
                setUser(userData);
                setIsAuthenticated(true);
            }
        }
    };

    const register = async (tenantData, adminData) => {
        setLoading(true);
        setError(null);

        try {
            console.log('Attempting registration with tenant data:', tenantData);

            const requestData = {
                tenantName: tenantData.name,
                subdomain: tenantData.subdomain,
                username: adminData.username,
                password: adminData.password,
                email: adminData.email,
                firstName: adminData.firstName,
                lastName: adminData.lastName
            };

            console.log('Registration request data:', requestData);

            const response = await fetch('/api/v1/auth/register', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(requestData),
            });

            if (!response.ok) {
                const errorData = await response.json().catch(() => ({}));
                const errorMessage = errorData.message || 'Registration failed';
                console.error('Registration failed:', errorMessage);
                setError(errorMessage);
                setLoading(false);
                return false;
            }

            const data = await response.json();

            if (!data.accessToken) {
                console.error('Registration API did not return a token');
                setError('No authentication token received');
                setLoading(false);
                return false;
            }

            console.log('Registration successful, received data:', data);

            // Сохраняем токен и информацию о пользователе
            const userData = {
                username: adminData.username,
                email: adminData.email,
                tenantId: data.tenantId,
                tenantName: tenantData.name,
                subdomain: tenantData.subdomain,
                role: 'ROLE_ADMIN'
            };

            login(data.accessToken, userData);
            setLoading(false);
            return true;

        } catch (error) {
            console.error('Registration error:', error);
            setError('An error occurred during registration');
            setLoading(false);
            return false;
        }
    };

    const logout = () => {
        console.log('Logging out user');
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        setUser(null);
        setIsAuthenticated(false);
        // Перенаправляем на страницу входа
        if (window.location.pathname !== '/login') {
            window.location.href = '/admin/login';
        }
    };

    const updateUser = (newUserData) => {
        if (!user) return;

        const updatedUser = { ...user, ...newUserData };
        console.log('Updating user data:', updatedUser);
        localStorage.setItem('user', JSON.stringify(updatedUser));
        setUser(updatedUser);
    };

    const hasTenantContext = () => {
        return !!user?.tenantId;
    };

    return (
        <AuthContext.Provider value={{
            user,
            isAuthenticated,
            loading,
            error,
            login,
            loginWithCredentials,
            register,
            logout,
            updateUser,
            setAuthError: setError,
            hasTenantContext,
            fetchWithAuth, // Добавляем функцию для аутентифицированных запросов
        }}>
            {children}
        </AuthContext.Provider>
    );
};