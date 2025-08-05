import axios from './axios';

const productAPI = {
    /**
     * Получить список товаров с фильтрацией и пагинацией
     */
    getProducts: async (params = {}) => {
        try {
            console.log('Fetching products with params:', params);
            const response = await axios.get('/products', { params });
            console.log('Products fetched successfully:', response.data);
            return response.data;
        } catch (error) {
            console.error('Error fetching products:', error);

            if (error.response && error.response.status === 403) {
                console.error('Tenant context error in getProducts');
                if (error.response.data?.message?.includes('Tenant context not established')) {
                    throw new Error('Не удалось определить магазин для текущего пользователя. Необходимо заново авторизоваться.');
                }
            }
            throw error;
        }
    },

    /**
     * Получить товар по ID
     */
    getProductById: async (id) => {
        try {
            console.log(`Fetching product with ID: ${id}`);

            const response = await axios.get(`/products/${id}`);
            console.log('Product fetched successfully:', response.data);
            return response.data;
        } catch (error) {
            console.error(`Error fetching product with ID ${id}:`, error);

            if (error.response && error.response.status === 403) {
                console.error('Tenant context error in getProductById');
                if (error.response.data?.message?.includes('Tenant context not established')) {
                    throw new Error('Не удалось определить магазин для текущего пользователя. Необходимо заново авторизоваться.');
                }
            }
            throw error;
        }
    },

    /**
     * Создать новый товар
     */
    createProduct: async (productData) => {
        try {
            console.log('Creating product with data:', productData);

            const response = await axios.post('/products', productData);
            console.log('Product created successfully:', response.data);
            return response.data;
        } catch (error) {
            console.error('Error creating product:', error);

            if (error.response && error.response.status === 403) {
                console.error('Tenant context error in createProduct');
                if (error.response.data?.message?.includes('Tenant context not established')) {
                    throw new Error('Не удалось определить магазин для текущего пользователя. Необходимо заново авторизоваться.');
                }
            }
            throw error;
        }
    },

    /**
     * Обновить существующий товар
     */
    updateProduct: async (id, productData) => {
        try {
            console.log(`Updating product with ID: ${id}`, productData);

            const response = await axios.put(`/products/${id}`, productData);
            console.log('Product updated successfully:', response.data);
            return response.data;
        } catch (error) {
            console.error(`Error updating product with ID ${id}:`, error);

            if (error.response && error.response.status === 403) {
                console.error('Tenant context error in updateProduct');
                if (error.response.data?.message?.includes('Tenant context not established')) {
                    throw new Error('Не удалось определить магазин для текущего пользователя. Необходимо заново авторизоваться.');
                }
            }
            throw error;
        }
    },

    /**
     * Удалить товар
     */
    deleteProduct: async (id) => {
        try {
            console.log(`Deleting product with ID: ${id}`);

            const response = await axios.delete(`/products/${id}`);
            console.log('Product deleted successfully');
            return true;
        } catch (error) {
            console.error(`Error deleting product with ID ${id}:`, error);

            if (error.response && error.response.status === 403) {
                console.error('Tenant context error in deleteProduct');
                if (error.response.data?.message?.includes('Tenant context not established')) {
                    throw new Error('Не удалось определить магазин для текущего пользователя. Необходимо заново авторизоваться.');
                }
            }
            throw error;
        }
    }
};

export default productAPI;