import React, { useEffect, useState } from 'react';
import './ConfirmDialog.css';

const ConfirmDialog = ({
                           open,
                           title,
                           message,
                           confirmText = 'Подтвердить',
                           cancelText = 'Отмена',
                           onConfirm,
                           onCancel
                       }) => {
    const [isVisible, setIsVisible] = useState(false);

    useEffect(() => {
        if (open) {
            setIsVisible(true);
        } else {
            const timer = setTimeout(() => {
                setIsVisible(false);
            }, 300);

            return () => clearTimeout(timer);
        }
    }, [open]);

    if (!isVisible) return null;

    return (
        <div className={`confirm-dialog-backdrop ${open ? 'active' : 'inactive'}`}>
            <div className={`confirm-dialog ${open ? 'active' : 'inactive'}`}>
                <div className="confirm-dialog-header">
                    <h2 className="confirm-dialog-title">{title}</h2>
                    <button
                        className="confirm-dialog-close"
                        onClick={onCancel}
                        aria-label="Закрыть"
                    >
                        &times;
                    </button>
                </div>

                <div className="confirm-dialog-content">
                    <p className="confirm-dialog-message">{message}</p>
                </div>

                <div className="confirm-dialog-actions">
                    <button
                        className="confirm-dialog-button confirm-dialog-cancel"
                        onClick={onCancel}
                    >
                        {cancelText}
                    </button>
                    <button
                        className="confirm-dialog-button confirm-dialog-confirm"
                        onClick={onConfirm}
                    >
                        {confirmText}
                    </button>
                </div>
            </div>
        </div>
    );
};

export default ConfirmDialog;