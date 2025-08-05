class CartService {
    constructor() {
        this.storageKey = 'shopCart';
    }

    // Получение локального идентификатора магазина из хоста
    getStoreIdentifier() {
        return window.location.hostname;
    }

    // Получение ключа хранилища с учетом магазина
    getStorageKey() {
        const storeId = this.getStoreIdentifier();
        return `${this.storageKey}_${storeId}`;
    }

    // Получение корзины из localStorage
    getCart() {
        const cart = localStorage.getItem(this.getStorageKey());
        return cart ? JSON.parse(cart) : [];
    }

    // Сохранение корзины в localStorage
    saveCart(cart) {
        localStorage.setItem(this.getStorageKey(), JSON.stringify(cart));
        return cart;
    }

    // Добавление товара в корзину
    addToCart(product, quantity = 1) {
        const cart = this.getCart();
        const existingItemIndex = cart.findIndex(item => item.id === product.id);

        if (existingItemIndex !== -1) {
            cart[existingItemIndex].quantity += quantity;
        } else {
            let imagePath = null;
            if (product.photos && product.photos.length > 0) {
                const mainPhoto = product.photos.find(photo => photo.main);
                imagePath = mainPhoto ? mainPhoto.filePath : product.photos[0].filePath;
            }
            cart.push({
                id: product.id,
                name: product.name,
                price: product.price,
                image: imagePath,
                quantity: quantity
            });
        }

        return this.saveCart(cart);
    }

    // Обновление количества товаров
    updateQuantity(productId, quantity) {
        let cart = this.getCart();

        if (quantity <= 0) {
            return this.removeFromCart(productId);
        }
        cart = cart.map(item => {
            if (item.id === productId) {
                return {...item, quantity};
            }
            return item;
        });

        return this.saveCart(cart);
    }

    // Удаление товара из корзины
    removeFromCart(productId) {
        const cart = this.getCart().filter(item => item.id !== productId);
        return this.saveCart(cart);
    }

    // Очистка корзины
    clearCart() {
        return this.saveCart([]);
    }

    // Получение общей суммы
    getTotalAmount() {
        return this.getCart().reduce((total, item) => {
            return total + (item.price * item.quantity);
        }, 0);
    }

    // Получение общего количества товаров
    getTotalItems() {
        return this.getCart().reduce((total, item) => total + item.quantity, 0);
    }
}

export default new CartService();