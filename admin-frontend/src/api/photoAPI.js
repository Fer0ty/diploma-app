import axios from './axios';

const photoAPI = {
    /**
     * Загрузить файл изображения на сервер
     */
    uploadFile: async (file, category = 'products') => {
        try {
            console.log(`Uploading file to category: ${category}`, file.name);

            const formData = new FormData();
            formData.append('file', file);

            const response = await axios.post(`/files/upload/${category}`, formData, {
                headers: {
                    'Content-Type': 'multipart/form-data'
                }
            });

            console.log('File uploaded successfully:', response.data);
            return response.data;
        } catch (error) {
            console.error('Error uploading file:', error);
            throw error;
        }
    },

    /**
     * Получить фотографии для продукта
     */
    getProductPhotos: async (productId) => {
        try {
            console.log(`Fetching photos for product with ID: ${productId}`);

            const response = await axios.get(`/products/${productId}/photos`);
            console.log('Photos fetched successfully:', response.data);
            return response.data;
        } catch (error) {
            console.error(`Error fetching photos for product with ID ${productId}:`, error);
            throw error;
        }
    },

    /**
     * Добавить фотографию к продукту
     */
    addPhotoToProduct: async (productId, photoData) => {
        try {
            console.log(`Adding photo to product with ID: ${productId}`, photoData);

            const response = await axios.post(`/products/${productId}/photos`, photoData);
            console.log('Photo added successfully:', response.data);
            return response.data;
        } catch (error) {
            console.error(`Error adding photo to product with ID ${productId}:`, error);
            throw error;
        }
    },

    /**
     * Удалить фотографию продукта
     */
    deleteProductPhoto: async (productId, photoId) => {
        try {
            console.log(`Deleting photo ${photoId} from product ${productId}`);

            await axios.delete(`/products/${productId}/photos/${photoId}`);
            console.log('Photo deleted successfully');
            return true;
        } catch (error) {
            console.error(`Error deleting photo ${photoId} from product ${productId}:`, error);
            throw error;
        }
    },

    /**
     * Установить фотографию как главную
     */
    setMainPhoto: async (productId, photoId) => {
        try {
            console.log(`Setting photo ${photoId} as main for product ${productId}`);

            const response = await axios.put(`/products/${productId}/photos/${photoId}/set-main`);
            console.log('Photo set as main successfully:', response.data);
            return response.data;
        } catch (error) {
            console.error(`Error setting photo ${photoId} as main for product ${productId}:`, error);
            throw error;
        }
    }
};

export default photoAPI;