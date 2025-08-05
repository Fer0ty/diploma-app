import React, {createContext, useCallback, useEffect, useState} from 'react';
import CartService from '../services/CartService';

// Создаем контекст
export const CartContext = createContext();

// Провайдер контекста
export const CartProvider = ({children}) => {
    const [cartItems, setCartItems] = useState([]);
    const [totalAmount, setTotalAmount] = useState(0);
    const [totalItems, setTotalItems] = useState(0);

    // Загружаем корзину при монтировании компонента
    useEffect(() => {
        const loadCart = () => {
            const cart = CartService.getCart();
            setCartItems(cart);
            setTotalAmount(CartService.getTotalAmount());
            setTotalItems(CartService.getTotalItems());
        };

        loadCart();

        // Можно добавить слушатель событий storage для синхронизации между вкладками
        const handleStorageChange = (e) => {
            if (e.key === CartService.getStorageKey()) {
                loadCart();
            }
        };

        window.addEventListener('storage', handleStorageChange);

        return () => {
            window.removeEventListener('storage', handleStorageChange);
        };
    }, []);

    // Функция добавления товара в корзину
    const addToCart = useCallback((product, quantity = 1) => {
        const updatedCart = CartService.addToCart(product, quantity);
        setCartItems(updatedCart);
        setTotalAmount(CartService.getTotalAmount());
        setTotalItems(CartService.getTotalItems());
    }, []);

    // Функция обновления количества товара
    const updateQuantity = useCallback((productId, quantity) => {
        const updatedCart = CartService.updateQuantity(productId, quantity);
        setCartItems(updatedCart);
        setTotalAmount(CartService.getTotalAmount());
        setTotalItems(CartService.getTotalItems());
    }, []);

    // Функция удаления товара из корзины
    const removeFromCart = useCallback((productId) => {
        const updatedCart = CartService.removeFromCart(productId);
        setCartItems(updatedCart);
        setTotalAmount(CartService.getTotalAmount());
        setTotalItems(CartService.getTotalItems());
    }, []);

    // Функция очистки корзины
    const clearCart = useCallback(() => {
        const emptyCart = CartService.clearCart();
        setCartItems(emptyCart);
        setTotalAmount(0);
        setTotalItems(0);
    }, []);

    // Проверка, есть ли товар в корзине
    const isInCart = useCallback((productId) => {
        return cartItems.some(item => item.id === productId);
    }, [cartItems]);

    // Получение количества конкретного товара в корзине
    const getItemQuantity = useCallback((productId) => {
        const item = cartItems.find(item => item.id === productId);
        return item ? item.quantity : 0;
    }, [cartItems]);

    // Значение контекста
    const contextValue = {
        cartItems,
        totalAmount,
        totalItems,
        addToCart,
        updateQuantity,
        removeFromCart,
        clearCart,
        isInCart,
        getItemQuantity
    };

    return (
        <CartContext.Provider value={contextValue}>
            {children}
        </CartContext.Provider>
    );
};

// Хук для использования контекста корзины
export const useCart = () => {
    const context = React.useContext(CartContext);
    if (context === undefined) {
        throw new Error('useCart must be used within a CartProvider');
    }
    return context;
};