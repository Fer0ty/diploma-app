import React, {useEffect, useState} from 'react';
import './Toast.css';

const Toast = ({message, type = 'info', duration = 5000, onClose}) => {
    const [isVisible, setIsVisible] = useState(true);

    useEffect(() => {
        const timer = setTimeout(() => {
            setIsVisible(false);
            setTimeout(() => {
                if (onClose) onClose();
            }, 300);
        }, duration);

        return () => clearTimeout(timer);
    }, [duration, onClose]);

    return (
        <div className={`toast ${type} ${isVisible ? 'visible' : 'hidden'}`}>
            <div className="toast-content">
                {type === 'success' && <span className="toast-icon">✓</span>}
                {type === 'error' && <span className="toast-icon">✗</span>}
                {type === 'warning' && <span className="toast-icon">⚠</span>}
                {type === 'info' && <span className="toast-icon">ℹ</span>}
                <span className="toast-message">{message}</span>
            </div>
            <button className="toast-close" onClick={() => setIsVisible(false)}>×</button>
        </div>
    );
};

export default Toast;