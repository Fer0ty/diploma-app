import axios from 'axios';

// Получаем базовый URL из окружения или используем относительный путь
const API_BASE_URL = process.env.REACT_APP_API_URL || '/api/v1';

// Создаем экземпляр axios с базовой конфигурацией
const api = axios.create({
    baseURL: API_BASE_URL,
    headers: {
        'Content-Type': 'application/json',
    },
    withCredentials: false
});

// Перехватчик для логирования
api.interceptors.request.use(
    (config) => {
        console.log('API Request:', config.method?.toUpperCase(), config.url, config.params || config.data);
        return config;
    },
    (error) => {
        console.error('API Request Error:', error);
        return Promise.reject(error);
    }
);

api.interceptors.response.use(
    (response) => {
        console.log('API Response:', response.status, response.config.url);
        return response;
    },
    (error) => {
        console.error('API Response Error:', error.response?.status, error.config?.url, error.response?.data);
        return Promise.reject(error);
    }
);

// Функция для получения полного URL изображения
export const getImageUrl = (path) => {
    if (!path) return '/images/no-image.jpg';
    if (path.startsWith('http://') || path.startsWith('https://')) {
        return path;
    }
    // Если путь начинается с /uploads/, добавляем базовый URL
    if (path.startsWith('/uploads/')) {
        return `${window.location.origin}${path}`;
    }
    return `${window.location.origin}${path.startsWith('/') ? '' : '/'}${path}`;
};

// Theme API
export const themeApi = {
    getPublicTheme: () => api.get('/public/theme'),
};

// Product API
export const productApi = {
    getProducts: (params) => api.get('/products', { params }),
    getProductById: (id) => api.get(`/products/${id}`),
};

// User API
export const userApi = {
    createUser: (userData) => api.post('/users', userData),
};

// Address API
export const addressApi = {
    createAddress: (addressData) => api.post('/addresses', addressData),
};

// Order API
export const orderApi = {
    createOrder: (orderData) => api.post('/orders', orderData),
};

// Health API
export const healthApi = {
    check: () => api.get('/health'),
    test: () => api.get('/test'),
};

export default api;