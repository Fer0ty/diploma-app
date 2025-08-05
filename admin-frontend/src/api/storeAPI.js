import axios from './axios';

const storeAPI = {
    /**
     * Получить информацию о текущем магазине
     */
    getStore: async () => {
        try {
            console.log('Fetching store information');
            const response = await axios.get('/store');
            console.log('Store information fetched successfully:', response.data);
            return response.data;
        } catch (error) {
            console.error('Error fetching store information:', error);

            if (error.response && error.response.status === 403) {
                console.error('Tenant context error in getStore');
                if (error.response.data?.message?.includes('Tenant context not established')) {
                    throw new Error('Не удалось определить магазин для текущего пользователя. Необходимо заново авторизоваться.');
                }
            }
            throw error;
        }
    },

    /**
     * Обновить информацию о магазине
     */
    updateStore: async (storeData) => {
        try {
            console.log('Updating store information:', storeData);

            const response = await axios.put('/store', storeData);
            console.log('Store information updated successfully:', response.data);
            return response.data;
        } catch (error) {
            console.error('Error updating store information:', error);

            if (error.response && error.response.status === 403) {
                console.error('Tenant context error in updateStore');
                if (error.response.data?.message?.includes('Tenant context not established')) {
                    throw new Error('Не удалось определить магазин для текущего пользователя. Необходимо заново авторизоваться.');
                }
            }
            throw error;
        }
    }
};

export default storeAPI;