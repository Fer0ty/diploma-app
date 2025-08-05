const API_BASE_URL = process.env.REACT_APP_API_BASE_URL || '/api/v1';

export default {
    API_BASE_URL,
    AUTH_ENDPOINT: `${API_BASE_URL}/auth`,
};