import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import authAPI from '../api/authAPI';
import '../pages/pages.css';

const Register = () => {
    const [formData, setFormData] = useState({
        tenantName: '',
        subdomain: '',
        username: '',
        email: '',
        password: '',
        confirmPassword: '',
        firstName: '',
        lastName: ''
    });

    const [errors, setErrors] = useState({});
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [serverError, setServerError] = useState('');
    const [checkingSubdomain, setCheckingSubdomain] = useState(false);
    const [subdomainAvailable, setSubdomainAvailable] = useState(null);
    const [subdomainTimer, setSubdomainTimer] = useState(null);

    const { register } = useAuth();
    const { showSuccess, showError, showInfo } = useToast();
    const navigate = useNavigate();

    useEffect(() => {
        if (subdomainTimer) {
            clearTimeout(subdomainTimer);
        }

        if (formData.subdomain && formData.subdomain.length >= 2) {
            const timer = setTimeout(() => {
                checkSubdomain(formData.subdomain);
            }, 500);
            setSubdomainTimer(timer);
        } else {
            setSubdomainAvailable(null);
        }

        return () => {
            if (subdomainTimer) {
                clearTimeout(subdomainTimer);
            }
        };
    }, [formData.subdomain]);

    const checkSubdomain = async (subdomain) => {
        if (!subdomain || subdomain.length < 2) return;

        try {
            setCheckingSubdomain(true);
            const response = await authAPI.checkSubdomain(subdomain);
            setSubdomainAvailable(response.available);

            if (!response.available) {
                setErrors(prev => ({
                    ...prev,
                    subdomain: 'Этот поддомен уже занят'
                }));
            } else {
                setErrors(prev => ({
                    ...prev,
                    subdomain: ''
                }));
            }
        } catch (error) {
            console.error('Error checking subdomain:', error);
            setSubdomainAvailable(null);
        } finally {
            setCheckingSubdomain(false);
        }
    };

    const handleChange = (e) => {
        const { name, value } = e.target;

        if (name === 'subdomain') {
            const sanitizedValue = value.toLowerCase().replace(/[^a-z0-9-]/g, '');
            setFormData(prev => ({
                ...prev,
                [name]: sanitizedValue
            }));
        } else {
            setFormData(prev => ({
                ...prev,
                [name]: value
            }));
        }

        if (errors[name]) {
            setErrors(prev => ({
                ...prev,
                [name]: ''
            }));
        }

        if (serverError) {
            setServerError('');
        }

        if (name === 'confirmPassword' || (name === 'password' && formData.confirmPassword)) {
            if ((name === 'password' && value !== formData.confirmPassword) ||
                (name === 'confirmPassword' && value !== formData.password)) {
                setErrors(prev => ({
                    ...prev,
                    confirmPassword: 'Пароли не совпадают'
                }));
            } else {
                setErrors(prev => ({
                    ...prev,
                    confirmPassword: ''
                }));
            }
        }
    };

    const validateForm = () => {
        const newErrors = {};

        if (!formData.tenantName.trim()) {
            newErrors.tenantName = 'Название магазина обязательно';
        } else if (formData.tenantName.length < 2) {
            newErrors.tenantName = 'Название магазина должно содержать не менее 2 символов';
        }

        if (!formData.subdomain) {
            newErrors.subdomain = 'Поддомен обязателен';
        } else if (formData.subdomain.length < 2) {
            newErrors.subdomain = 'Поддомен должен содержать не менее 2 символов';
        } else if (!/^[a-z0-9](?:[a-z0-9\-]{0,61}[a-z0-9])?$/.test(formData.subdomain)) {
            newErrors.subdomain = 'Поддомен может содержать только строчные латинские буквы, цифры и дефисы, не может начинаться или заканчиваться дефисом';
        } else if (subdomainAvailable === false) {
            newErrors.subdomain = 'Этот поддомен уже занят';
        }

        if (!formData.username.trim()) {
            newErrors.username = 'Имя пользователя обязательно';
        } else if (formData.username.length < 3) {
            newErrors.username = 'Имя пользователя должно содержать не менее 3 символов';
        } else if (!/^[a-zA-Z0-9_]+$/.test(formData.username)) {
            newErrors.username = 'Имя пользователя может содержать только латинские буквы, цифры и символ подчеркивания';
        }

        if (!formData.email.trim()) {
            newErrors.email = 'Email обязателен';
        } else if (!/\S+@\S+\.\S+/.test(formData.email)) {
            newErrors.email = 'Введите корректный email';
        }

        if (!formData.password) {
            newErrors.password = 'Пароль обязателен';
        } else if (formData.password.length < 8) {
            newErrors.password = 'Пароль должен содержать не менее 8 символов';
        }

        if (!formData.confirmPassword) {
            newErrors.confirmPassword = 'Подтверждение пароля обязательно';
        } else if (formData.password !== formData.confirmPassword) {
            newErrors.confirmPassword = 'Пароли не совпадают';
        }

        setErrors(newErrors);
        return Object.keys(newErrors).length === 0;
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        if (!validateForm()) return;

        if (subdomainAvailable === null || subdomainAvailable === false) {
            try {
                showInfo('Проверка доступности поддомена...');
                const response = await authAPI.checkSubdomain(formData.subdomain);

                if (!response.available) {
                    setErrors(prev => ({
                        ...prev,
                        subdomain: 'Этот поддомен уже занят'
                    }));
                    showError('Поддомен уже занят. Пожалуйста, выберите другой.');
                    return;
                }

                setSubdomainAvailable(true);
            } catch (error) {
                console.error('Error checking subdomain:', error);
                setServerError('Ошибка при проверке поддомена');
                showError('Ошибка при проверке поддомена');
                return;
            }
        }

        setIsSubmitting(true);
        setServerError('');

        try {
            const { confirmPassword, ...registerData } = formData;

            showInfo('Создание магазина...');

            const response = await authAPI.register(registerData);

            if (!response || !response.accessToken) {
                throw new Error('Некорректный ответ от сервера при регистрации');
            }

            register(response.accessToken, {
                username: formData.username,
                tenantId: response.tenantId,
                tenantName: response.tenantName,
                subdomain: response.subdomain,
                role: 'ROLE_ADMIN'
            });

            showSuccess(`Магазин "${response.tenantName}" успешно создан!`);
            navigate('/dashboard');
        } catch (error) {
            console.error('Registration error:', error);

            if (error.response) {
                if (error.response.status === 400) {
                    const errorMessage = error.response.data?.message || 'Некорректные данные для регистрации';
                    setServerError(errorMessage);
                    showError(errorMessage);
                } else if (error.response.status === 409) {
                    setServerError('Указанные данные уже используются');
                    showError('Указанные данные уже используются');
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

    const renderSubdomainAvailability = () => {
        if (checkingSubdomain) {
            return <div className="subdomain-checking">Проверка доступности...</div>;
        }

        if (formData.subdomain && formData.subdomain.length >= 2) {
            if (subdomainAvailable === true) {
                return <div className="subdomain-available">Поддомен доступен</div>;
            } else if (subdomainAvailable === false) {
                return <div className="subdomain-unavailable">Поддомен занят</div>;
            }
        }

        return null;
    };

    return (
        <div className="page-container">
            <div className="form-container register-form">
                <h1 className="form-title">Создание магазина</h1>

                {serverError && (
                    <div className="error-message">
                        {serverError}
                    </div>
                )}

                <form onSubmit={handleSubmit}>
                    <div className="form-section">
                        <h2 className="form-section-title">Информация о магазине</h2>

                        <div className="form-group">
                            <label htmlFor="tenantName" className="form-label">Название магазина</label>
                            <input
                                type="text"
                                id="tenantName"
                                name="tenantName"
                                className={`form-input ${errors.tenantName ? 'error' : ''}`}
                                placeholder="Введите название магазина"
                                value={formData.tenantName}
                                onChange={handleChange}
                                disabled={isSubmitting}
                                maxLength={255}
                            />
                            {errors.tenantName && <div className="form-error">{errors.tenantName}</div>}
                        </div>

                        <div className="form-group">
                            <label htmlFor="subdomain" className="form-label">Поддомен</label>
                            <div className="subdomain-input-group">
                                <input
                                    type="text"
                                    id="subdomain"
                                    name="subdomain"
                                    className={`form-input ${errors.subdomain ? 'error' : ''}`}
                                    placeholder="your-store"
                                    value={formData.subdomain}
                                    onChange={handleChange}
                                    disabled={isSubmitting}
                                    maxLength={63}
                                />
                                <span className="subdomain-suffix">.diploma.com</span>
                            </div>
                            {renderSubdomainAvailability()}
                            {errors.subdomain && <div className="form-error">{errors.subdomain}</div>}
                        </div>
                    </div>

                    <div className="form-section">
                        <h2 className="form-section-title">Информация об администраторе</h2>

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
                                maxLength={50}
                            />
                            {errors.username && <div className="form-error">{errors.username}</div>}
                        </div>

                        <div className="form-group">
                            <label htmlFor="email" className="form-label">Email</label>
                            <input
                                type="email"
                                id="email"
                                name="email"
                                className={`form-input ${errors.email ? 'error' : ''}`}
                                placeholder="Введите email"
                                value={formData.email}
                                onChange={handleChange}
                                disabled={isSubmitting}
                                autoComplete="email"
                            />
                            {errors.email && <div className="form-error">{errors.email}</div>}
                        </div>

                        <div className="form-row">
                            <div className="form-group form-group-half">
                                <label htmlFor="firstName" className="form-label">Имя (необязательно)</label>
                                <input
                                    type="text"
                                    id="firstName"
                                    name="firstName"
                                    className={`form-input ${errors.firstName ? 'error' : ''}`}
                                    placeholder="Введите имя"
                                    value={formData.firstName}
                                    onChange={handleChange}
                                    disabled={isSubmitting}
                                    autoComplete="given-name"
                                    maxLength={100}
                                />
                                {errors.firstName && <div className="form-error">{errors.firstName}</div>}
                            </div>

                            <div className="form-group form-group-half">
                                <label htmlFor="lastName" className="form-label">Фамилия (необязательно)</label>
                                <input
                                    type="text"
                                    id="lastName"
                                    name="lastName"
                                    className={`form-input ${errors.lastName ? 'error' : ''}`}
                                    placeholder="Введите фамилию"
                                    value={formData.lastName}
                                    onChange={handleChange}
                                    disabled={isSubmitting}
                                    autoComplete="family-name"
                                    maxLength={100}
                                />
                                {errors.lastName && <div className="form-error">{errors.lastName}</div>}
                            </div>
                        </div>

                        <div className="form-group">
                            <label htmlFor="password" className="form-label">Пароль</label>
                            <input
                                type="password"
                                id="password"
                                name="password"
                                className={`form-input ${errors.password ? 'error' : ''}`}
                                placeholder="Минимум 8 символов"
                                value={formData.password}
                                onChange={handleChange}
                                disabled={isSubmitting}
                                autoComplete="new-password"
                                maxLength={72}
                            />
                            {errors.password && <div className="form-error">{errors.password}</div>}
                        </div>

                        <div className="form-group">
                            <label htmlFor="confirmPassword" className="form-label">Подтверждение пароля</label>
                            <input
                                type="password"
                                id="confirmPassword"
                                name="confirmPassword"
                                className={`form-input ${errors.confirmPassword ? 'error' : ''}`}
                                placeholder="Повторите пароль"
                                value={formData.confirmPassword}
                                onChange={handleChange}
                                disabled={isSubmitting}
                                autoComplete="new-password"
                            />
                            {errors.confirmPassword && <div className="form-error">{errors.confirmPassword}</div>}
                        </div>
                    </div>

                    <button
                        type="submit"
                        className="form-button"
                        disabled={isSubmitting || checkingSubdomain}
                    >
                        {isSubmitting ? (
                            <>
                                <span className="spinner-icon">⟳</span>
                                Создание магазина...
                            </>
                        ) : (
                            'Создать магазин'
                        )}
                    </button>
                </form>

                <p className="text-center mt-md">
                    Уже есть аккаунт?{' '}
                    <Link to="/login" className="form-link">Войти</Link>
                </p>
            </div>
        </div>
    );
};

export default Register;