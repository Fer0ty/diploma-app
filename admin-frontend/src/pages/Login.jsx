import React, { useState } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import authAPI from '../api/authAPI';
import { jwtDecode } from 'jwt-decode';
import '../pages/pages.css';

const Login = () => {
    const [formData, setFormData] = useState({
        username: '',
        password: ''
    });
    const [errors, setErrors] = useState({});
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [serverError, setServerError] = useState('');

    const { login } = useAuth();
    const { showSuccess, showError } = useToast();
    const navigate = useNavigate();
    const location = useLocation();

    const from = location.state?.from?.pathname || '/dashboard';

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: value
        }));

        if (errors[name]) {
            setErrors(prev => ({
                ...prev,
                [name]: ''
            }));
        }

        if (serverError) {
            setServerError('');
        }
    };

    const validateForm = () => {
        const newErrors = {};

        if (!formData.username.trim()) {
            newErrors.username = 'Имя пользователя обязательно';
        }

        if (!formData.password) {
            newErrors.password = 'Пароль обязателен';
        }

        setErrors(newErrors);
        return Object.keys(newErrors).length === 0;
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        if (!validateForm()) return;

        setIsSubmitting(true);
        setServerError('');

        try {
            console.log('Attempting to login with:', formData.username);

            const response = await authAPI.login(formData.username, formData.password);

            if (!response || !response.token) {
                throw new Error('Получен некорректный ответ от сервера');
            }

            const token = response.token;

            try {
                const decoded = jwtDecode(token);
                console.log('Decoded token on login:', decoded);

                const userData = {
                    username: decoded.sub,
                    tenantId: decoded.tenant_id,
                    role: decoded.roles ? decoded.roles.split(',')[0] : 'ROLE_ADMIN',
                    tenantName: decoded.tenantName || 'Мой магазин'
                };

                if (!userData.tenantId) {
                    console.warn('Warning: JWT token is missing tenantId');
                    showError('Предупреждение: токен не содержит информацию о магазине');
                }

                login(token, userData);

                showSuccess('Вход выполнен успешно');
                navigate(from, { replace: true });
            } catch (decodeError) {
                console.error('Error decoding JWT token:', decodeError);
                login(token);
                showError('Ошибка при обработке данных пользователя');
                navigate(from, { replace: true });
            }
        } catch (error) {
            console.error('Login error:', error);

            if (error.response) {
                if (error.response.status === 401) {
                    setServerError('Неверное имя пользователя или пароль');
                    showError('Неверное имя пользователя или пароль');
                } else {
                    setServerError('Ошибка сервера. Пожалуйста, попробуйте позже.');
                    showError('Ошибка сервера. Пожалуйста, попробуйте позже.');
                }
            } else if (error.request) {
                setServerError('Сервер не отвечает. Проверьте подключение к интернету.');
                showError('Сервер не отвечает. Проверьте подключение к интернету.');
            } else {
                setServerError('Произошла ошибка при отправке запроса.');
                showError('Произошла ошибка при отправке запроса.');
            }
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <div className="page-container">
            <div className="form-container">
                <h1 className="form-title">Вход в аккаунт</h1>

                {serverError && (
                    <div className="error-message">
                        {serverError}
                    </div>
                )}

                <form onSubmit={handleSubmit}>
                    <div className="form-group">
                        <label htmlFor="username" className="form-label">Имя пользователя</label>
                        <input
                            type="text"
                            id="username"
                            name="username"
                            className={`form-input ${errors.username ? 'error' : ''}`}
                            placeholder="Введите имя пользователя"
                            value={formData.username}
                            onChange={handleChange}
                            disabled={isSubmitting}
                            autoComplete="username"
                        />
                        {errors.username && <div className="form-error">{errors.username}</div>}
                    </div>

                    <div className="form-group">
                        <label htmlFor="password" className="form-label">Пароль</label>
                        <input
                            type="password"
                            id="password"
                            name="password"
                            className={`form-input ${errors.password ? 'error' : ''}`}
                            placeholder="Введите пароль"
                            value={formData.password}
                            onChange={handleChange}
                            disabled={isSubmitting}
                            autoComplete="current-password"
                        />
                        {errors.password && <div className="form-error">{errors.password}</div>}
                    </div>

                    <button
                        type="submit"
                        className="form-button"
                        disabled={isSubmitting}
                    >
                        {isSubmitting ? (
                            <>
                                <span className="spinner-icon">⟳</span>
                                Вход...
                            </>
                        ) : (
                            'Войти'
                        )}
                    </button>
                </form>

                <p className="text-center mt-md">
                    Нет аккаунта?{' '}
                    <Link to="/register" className="form-link">Зарегистрироваться</Link>
                </p>
            </div>
        </div>
    );
};

export default Login;