import axios from './axios';

const authAPI = {
    /**
     * Авторизация пользователя
     * @param {string} username - Имя пользователя
     * @param {string} password - Пароль
     * @returns {Promise<{token: string}>} - JWT токен
     */
    login: async (username, password) => {
        console.log('Sending login request for username:', username);
        try {
            const response = await axios.post('/auth/login', { username, password });
            console.log('Login response received, status:', response.status);
            return response.data;
        } catch (error) {
            console.error('Login error:', error.response?.status || error.message);
            throw error;
        }
    },

    /**
     * Регистрация нового тенанта (магазина) и администратора
     * @param {Object} data - Данные для регистрации
     * @returns {Promise<{tenantId: number, tenantName: string, subdomain: string, accessToken: string, loginUrl: string}>}
     */
    register: async (data) => {
        console.log('Sending register request for tenant:', data.tenantName);
        try {
            const response = await axios.post('/auth/register', data);
            console.log('Registration successful, status:', response.status);
            return response.data;
        } catch (error) {
            console.error('Registration error:', error.response?.status || error.message);
            throw error;
        }
    },

    /**
     * Проверка доступности поддомена
     * @param {string} subdomain - Поддомен для проверки
     * @returns {Promise<{available: boolean}>} - Результат проверки
     */
    checkSubdomain: async (subdomain) => {
        console.log('Checking subdomain availability:', subdomain);
        try {
            const response = await axios.get(`/auth/check-subdomain/${subdomain}`);
            console.log('Subdomain check result:', response.data);
            return response.data;
        } catch (error) {
            console.error('Subdomain check error:', error.response?.status || error.message);
            throw error;
        }
    },
};

export default authAPI;