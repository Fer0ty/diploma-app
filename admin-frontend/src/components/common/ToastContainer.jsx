import React from 'react';
import Toast from './Toast';
import {useToast} from '../../context/ToastContext';
import './Toast.css';

const ToastContainer = () => {
    const {toasts, removeToast} = useToast();

    return (
        <div className="toast-container">
            {toasts.map(toast => (
                <Toast
                    key={toast.id}
                    message={toast.message}
                    type={toast.type}
                    duration={toast.duration}
                    onClose={() => removeToast(toast.id)}
                />
            ))}
        </div>
    );
};

export default ToastContainer;