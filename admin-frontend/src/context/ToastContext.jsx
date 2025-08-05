import React, {createContext, useCallback, useState} from 'react';

export const ToastContext = createContext();

export const ToastProvider = ({children}) => {
    const [toasts, setToasts] = useState([]);

    const addToast = useCallback((message, type = 'info', duration = 5000) => {
        const id = Date.now().toString();
        setToasts(prevToasts => [...prevToasts, {id, message, type, duration}]);
        return id;
    }, []);

    const removeToast = useCallback((id) => {
        setToasts(prevToasts => prevToasts.filter(toast => toast.id !== id));
    }, []);

    const showSuccess = useCallback((message, duration = 5000) => {
        return addToast(message, 'success', duration);
    }, [addToast]);

    const showError = useCallback((message, duration = 5000) => {
        return addToast(message, 'error', duration);
    }, [addToast]);

    const showWarning = useCallback((message, duration = 5000) => {
        return addToast(message, 'warning', duration);
    }, [addToast]);

    const showInfo = useCallback((message, duration = 5000) => {
        return addToast(message, 'info', duration);
    }, [addToast]);

    return (
        <ToastContext.Provider value={{
            toasts,
            addToast,
            removeToast,
            showSuccess,
            showError,
            showWarning,
            showInfo
        }}>
            {children}
        </ToastContext.Provider>
    );
};

export const useToast = () => {
    const context = React.useContext(ToastContext);
    if (context === undefined) {
        throw new Error('useToast must be used within a ToastProvider');
    }
    return context;
};