import axios from './axios';

const orderAPI = {
    /**
     * Получить список заказов с пагинацией
     */
    getOrders: async (params = {}) => {
        try {
            console.log('Fetching orders with params:', params);

            const response = await axios.get('/orders', { params });
            console.log('Orders fetched successfully:', response.data);
            return response.data;
        } catch (error) {
            console.error('Error fetching orders:', error);
            throw error;
        }
    },

    /**
     * Получить заказ по ID
     */
    getOrderById: async (id) => {
        try {
            console.log(`Fetching order with ID: ${id}`);

            const response = await axios.get(`/orders/${id}`);
            console.log('Order fetched successfully:', response.data);
            return response.data;
        } catch (error) {
            console.error(`Error fetching order with ID ${id}:`, error);
            throw error;
        }
    },

    /**
     * Создать новый заказ
     */
    createOrder: async (orderData) => {
        try {
            console.log('Creating order with data:', orderData);

            const response = await axios.post('/orders', orderData);
            console.log('Order created successfully:', response.data);
            return response.data;
        } catch (error) {
            console.error('Error creating order:', error);
            throw error;
        }
    },

    /**
     * Обновить статус заказа
     */
    updateOrderStatus: async (orderId, statusData) => {
        try {
            console.log(`Updating status for order ${orderId}:`, statusData);

            const response = await axios.put(`/orders/${orderId}/status`, statusData);
            console.log('Order status updated successfully:', response.data);
            return response.data;
        } catch (error) {
            console.error(`Error updating status for order ${orderId}:`, error);
            throw error;
        }
    },

    /**
     * Отменить заказ
     */
    cancelOrder: async (orderId, reason = '') => {
        try {
            console.log(`Cancelling order ${orderId} with reason: ${reason}`);

            const params = reason ? { reason } : {};
            const response = await axios.post(`/orders/${orderId}/cancel`, null, { params });

            console.log('Order cancelled successfully:', response.data);
            return response.data;
        } catch (error) {
            console.error(`Error cancelling order ${orderId}:`, error);
            throw error;
        }
    },

    /**
     * Обработать оплату заказа
     */
    processPayment: async (orderId, paymentReference) => {
        try {
            console.log(`Processing payment for order ${orderId} with reference: ${paymentReference}`);

            const params = { paymentReference };
            const response = await axios.post(`/orders/${orderId}/payment`, null, { params });

            console.log('Payment processed successfully:', response.data);
            return response.data;
        } catch (error) {
            console.error(`Error processing payment for order ${orderId}:`, error);
            throw error;
        }
    },

    /**
     * Получить все позиции заказа
     */
    getOrderItems: async (orderId) => {
        try {
            console.log(`Fetching items for order ${orderId}`);

            const response = await axios.get(`/orders/${orderId}/items`);
            console.log('Order items fetched successfully:', response.data);
            return response.data;
        } catch (error) {
            console.error(`Error fetching items for order ${orderId}:`, error);
            throw error;
        }
    },

    /**
     * Получить все статусы заказов
     */
    getOrderStatuses: async () => {
        try {
            console.log('Fetching order statuses');

            const response = await axios.get('/order-statuses');
            console.log('Order statuses fetched successfully:', response.data);
            return response.data;
        } catch (error) {
            console.error('Error fetching order statuses:', error);
            throw error;
        }
    }
};

export default orderAPI;