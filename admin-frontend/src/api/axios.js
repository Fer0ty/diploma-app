import axios from 'axios';

// Используем относительный путь для работы через прокси
const currentHost = window.location.origin;
const API_BASE_URL = `${currentHost}/api/v1`;

console.log('Using API base URL:', API_BASE_URL);

const axiosInstance = axios.create({
    baseURL: API_BASE_URL,
    headers: {
        'Content-Type': 'application/json',
    },
    withCredentials: true,
    timeout: 15000
});

// Перехватчик запросов для добавления JWT токена
axiosInstance.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('token');

        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
            console.log(`Request with auth to ${config.url}`);
        }

        // Логируем полный URL для отладки
        console.log(`Making request to: ${config.baseURL}${config.url}`);

        // Добавляем для отладки CORS
        console.log('Request headers:', config.headers);

        return config;
    },
    (error) => {
        console.error('Request interceptor error:', error);
        return Promise.reject(error);
    }
);

// Перехватчик ответов для обработки ошибок
axiosInstance.interceptors.response.use(
    (response) => {
        console.log(`Response from ${response.config.url}:`, response.status);
        // Для отладки CORS
        console.log('Response headers:', response.headers);
        return response;
    },
    (error) => {
        // Улучшенная отладка CORS-ошибок
        if (error.name === 'AxiosError' && error.message.includes('Network Error')) {
            console.error('⚠️ Possible CORS error:', error.message);
            console.error('Check browser console for more details. The request was blocked by CORS policy.');
        }
        else if (error.response) {
            const { status, data, config } = error.response;
            console.error(`Error ${status} from ${config.url}:`, data);

            if (status === 401) {
                if (!config.url.includes('/auth/login')) {
                    console.warn('Authentication error - logging out');
                    localStorage.removeItem('token');
                    localStorage.removeItem('user');
                    window.location.href = '/login';
                }
            }

            if (status === 403 && data && data.message?.includes("Tenant context not established")) {
                console.error('Tenant context error - user needs to log in again');
                localStorage.removeItem('token');
                localStorage.removeItem('user');

                alert('Ошибка контекста магазина. Необходимо заново авторизоваться.');
                window.location.href = '/login';
            }
        } else if (error.request) {
            console.error('No response received from server:', error.request);
        } else {
            console.error('Error setting up request:', error.message);
        }

        return Promise.reject(error);
    }
);

// Добавим функцию для тестирования CORS
axiosInstance.testCORS = async () => {
    try {
        console.log('Testing CORS with a simple request...');
        const response = await axiosInstance.get('/health');
        console.log('CORS test successful!', response.data);
        return { success: true, data: response.data };
    } catch (error) {
        console.error('CORS test failed!', error);
        return { success: false, error };
    }
};


export default axiosInstance;