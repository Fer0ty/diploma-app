import React, { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import orderAPI from '../api/orderAPI';
import LoadingScreen from '../components/common/LoadingScreen';
import ConfirmDialog from '../components/common/ConfirmDialog';
import './OrderDetailPage.css';

const OrderDetailPage = () => {
    const { orderId } = useParams();
    const navigate = useNavigate();

    const { user, logout } = useAuth();
    const { showSuccess, showError, showInfo } = useToast();

    const [order, setOrder] = useState(null);
    const [orderStatuses, setOrderStatuses] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const [selectedStatus, setSelectedStatus] = useState('');
    const [statusComment, setStatusComment] = useState('');
    const [processing, setProcessing] = useState(false);

    const [showCancelDialog, setShowCancelDialog] = useState(false);
    const [cancelReason, setCancelReason] = useState('');

    const [showProcessPaymentDialog, setShowProcessPaymentDialog] = useState(false);
    const [paymentReference, setPaymentReference] = useState('');

    const checkAuth = useCallback(() => {
        if (!localStorage.getItem('token')) {
            showError('Вы не авторизованы. Необходимо войти в систему.');
            navigate('/login');
            return false;
        }

        if (!user?.tenantId) {
            showError('Информация о магазине отсутствует. Необходимо заново авторизоваться.');
            logout();
            navigate('/login');
            return false;
        }

        return true;
    }, [user, navigate, logout, showError]);

    const loadOrder = useCallback(async () => {
        if (!checkAuth()) return;

        setLoading(true);
        showInfo('Загрузка данных заказа...');

        try {
            const orderData = await orderAPI.getOrderById(orderId);
            setOrder(orderData);
            setSelectedStatus(orderData.statusId.toString());
            setError(null);
        } catch (error) {
            console.error('Error loading order:', error);

            let errorMessage;
            if (error.response) {
                if (error.response.status === 403 &&
                    error.response.data?.message?.includes("Tenant context not established")) {
                    errorMessage = 'Не удалось определить магазин для текущего пользователя. Необходимо заново авторизоваться.';
                    logout();
                    navigate('/login');
                } else if (error.response.status === 404) {
                    errorMessage = 'Заказ не найден';
                } else {
                    errorMessage = `Ошибка при загрузке заказа: ${error.response.data?.message || error.response.statusText}`;
                }
            } else if (error.message) {
                errorMessage = error.message;

                if (error.message.includes('магазин') || error.message.includes('авторизоваться')) {
                    logout();
                    navigate('/login');
                }
            } else {
                errorMessage = 'Ошибка при загрузке заказа';
            }

            setError(errorMessage);
            showError(errorMessage);
        } finally {
            setLoading(false);
        }
    }, [orderId, checkAuth, logout, navigate, showError, showInfo]);

    const loadOrderStatuses = useCallback(async () => {
        try {
            const statuses = await orderAPI.getOrderStatuses();
            setOrderStatuses(statuses);
        } catch (error) {
            console.error('Error loading order statuses:', error);
        }
    }, []);

    useEffect(() => {
        loadOrder();
        loadOrderStatuses();
    }, [loadOrder, loadOrderStatuses]);

    const handleUpdateStatus = async () => {
        if (selectedStatus === order.statusId.toString()) {
            showInfo('Статус не был изменен');
            return;
        }

        setProcessing(true);

        try {
            showInfo('Обновление статуса заказа...');

            const updatedOrder = await orderAPI.updateOrderStatus(orderId, {
                statusId: parseInt(selectedStatus),
                comment: statusComment
            });

            setOrder(updatedOrder);
            setStatusComment('');
            showSuccess('Статус заказа успешно обновлен');
        } catch (error) {
            console.error('Error updating order status:', error);

            let errorMessage = 'Ошибка при обновлении статуса заказа';

            if (error.response?.data?.message) {
                errorMessage = error.response.data.message;
            } else if (error.message) {
                errorMessage = error.message;
            }

            showError(errorMessage);
        } finally {
            setProcessing(false);
        }
    };

    const handleCancelOrder = async () => {
        setProcessing(true);

        try {
            showInfo('Отмена заказа...');

            const updatedOrder = await orderAPI.cancelOrder(orderId, cancelReason);

            setOrder(updatedOrder);
            setSelectedStatus(updatedOrder.statusId.toString());
            showSuccess('Заказ успешно отменен');
        } catch (error) {
            console.error('Error canceling order:', error);

            let errorMessage = 'Ошибка при отмене заказа';

            if (error.response?.data?.message) {
                errorMessage = error.response.data.message;
            } else if (error.message) {
                errorMessage = error.message;
            }

            showError(errorMessage);
        } finally {
            setProcessing(false);
            setShowCancelDialog(false);
            setCancelReason('');
        }
    };

    const handleProcessPayment = async () => {
        if (!paymentReference) {
            showError('Необходимо указать идентификатор транзакции');
            return;
        }

        setProcessing(true);

        try {
            showInfo('Обработка оплаты...');

            const updatedOrder = await orderAPI.processPayment(orderId, paymentReference);

            setOrder(updatedOrder);
            setSelectedStatus(updatedOrder.statusId.toString());
            showSuccess('Оплата успешно обработана');
        } catch (error) {
            console.error('Error processing payment:', error);

            let errorMessage = 'Ошибка при обработке оплаты';

            if (error.response?.data?.message) {
                errorMessage = error.response.data.message;
            } else if (error.message) {
                errorMessage = error.message;
            }

            showError(errorMessage);
        } finally {
            setProcessing(false);
            setShowProcessPaymentDialog(false);
            setPaymentReference('');
        }
    };

    const formatDate = (dateString) => {
        if (!dateString) return '';
        const date = new Date(dateString);
        return new Intl.DateTimeFormat('ru-RU', {
            day: '2-digit',
            month: '2-digit',
            year: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        }).format(date);
    };

    const formatAmount = (amount) => {
        return new Intl.NumberFormat('ru-RU', {
            style: 'currency',
            currency: 'RUB',
            minimumFractionDigits: 2
        }).format(amount);
    };

    if (loading) {
        return <LoadingScreen />;
    }

    if (error) {
        return (
            <div className="page-container">
                <div className="page-header">
                    <h1 className="page-title">Заказ #{orderId}</h1>
                    <Link to="/orders" className="action-button secondary">
                        Назад к списку
                    </Link>
                </div>

                <div className="error-alert">
                    <strong>Ошибка:</strong> {error}
                </div>
            </div>
        );
    }

    if (!order) {
        return (
            <div className="page-container">
                <div className="page-header">
                    <h1 className="page-title">Заказ #{orderId}</h1>
                    <Link to="/orders" className="action-button secondary">
                        Назад к списку
                    </Link>
                </div>

                <div className="empty-state">
                    <h3>Заказ не найден</h3>
                    <p>Заказ с ID {orderId} не существует или был удален</p>
                </div>
            </div>
        );
    }

    const canCancel = ['Created', 'Paid', 'Processing'].includes(order.statusName);
    const canProcessPayment = order.statusName === 'Created';

    return (
        <div className="page-container">
            <div className="page-header">
                <h1 className="page-title">Заказ #{order.id}</h1>
                <div className="header-actions">
                    {canCancel && (
                        <button
                            className="action-button danger"
                            onClick={() => setShowCancelDialog(true)}
                            disabled={processing}
                        >
                            Отменить заказ
                        </button>
                    )}

                    {canProcessPayment && (
                        <button
                            className="action-button primary"
                            onClick={() => setShowProcessPaymentDialog(true)}
                            disabled={processing}
                        >
                            Обработать оплату
                        </button>
                    )}

                    <Link to="/orders" className="action-button secondary">
                        Назад к списку
                    </Link>
                </div>
            </div>

            <div className="order-detail-content">
                {/* Основная информация о заказе */}
                <div className="order-info-card">
                    <h2 className="card-title">Основная информация</h2>
                    <div className="order-info-grid">
                        <div className="info-group">
                            <label className="info-label">ID заказа:</label>
                            <div className="info-value">{order.id}</div>
                        </div>

                        <div className="info-group">
                            <label className="info-label">Дата создания:</label>
                            <div className="info-value">{formatDate(order.createdAt)}</div>
                        </div>

                        <div className="info-group">
                            <label className="info-label">Последнее обновление:</label>
                            <div className="info-value">{formatDate(order.updatedAt)}</div>
                        </div>

                        <div className="info-group">
                            <label className="info-label">Статус:</label>
                            <div className="info-value">
                                <span
                                    className="order-status-badge detail-page"
                                    style={{
                                        backgroundColor:
                                            order.statusName === 'Created' ? 'var(--info-color)' :
                                                order.statusName === 'Paid' ? 'var(--primary-color)' :
                                                    order.statusName === 'Processing' ? 'var(--warning-color)' :
                                                        order.statusName === 'Shipped' ? 'var(--warning-color)' :
                                                            order.statusName === 'Delivered' ? 'var(--success-color)' :
                                                                order.statusName === 'Canceled' ? 'var(--error-color)' :
                                                                    order.statusName === 'Returned' ? 'var(--error-color)' :
                                                                        'var(--text-secondary)'
                                    }}
                                >
                                    {order.statusName}
                                </span>
                            </div>
                        </div>

                        <div className="info-group">
                            <label className="info-label">Клиент:</label>
                            <div className="info-value">{order.customerName}</div>
                        </div>

                        <div className="info-group">
                            <label className="info-label">Общая сумма:</label>
                            <div className="info-value order-total">{formatAmount(order.totalAmount)}</div>
                        </div>
                    </div>
                </div>

                {/* Товары в заказе */}
                <div className="order-items-card">
                    <h2 className="card-title">Товары в заказе</h2>
                    <div className="order-items-table-container">
                        <table className="order-items-table">
                            <thead>
                            <tr>
                                <th>Товар</th>
                                <th>Цена</th>
                                <th>Количество</th>
                                <th>Итого</th>
                            </tr>
                            </thead>
                            <tbody>
                            {order.orderItems && order.orderItems.map(item => (
                                <tr key={item.id}>
                                    <td>{item.productName}</td>
                                    <td>{formatAmount(item.unitPrice)}</td>
                                    <td>{item.quantity}</td>
                                    <td>{formatAmount(item.totalPrice)}</td>
                                </tr>
                            ))}
                            </tbody>
                            <tfoot>
                            <tr>
                                <td colSpan="3" className="total-label">Итого:</td>
                                <td className="total-value">{formatAmount(order.totalAmount)}</td>
                            </tr>
                            </tfoot>
                        </table>
                    </div>
                </div>

                {/* Комментарии к заказу */}
                {order.comment && (
                    <div className="order-comments-card">
                        <h2 className="card-title">Комментарии к заказу</h2>
                        <div className="order-comments">
                            <pre className="order-comments-text">{order.comment}</pre>
                        </div>
                    </div>
                )}

                {/* Форма обновления статуса */}
                <div className="order-update-card">
                    <h2 className="card-title">Обновить статус</h2>
                    <div className="status-update-form">
                        <div className="form-group">
                            <label htmlFor="statusId" className="form-label">Новый статус</label>
                            <select
                                id="statusId"
                                className="form-select"
                                value={selectedStatus}
                                onChange={(e) => setSelectedStatus(e.target.value)}
                                disabled={processing}
                            >
                                {orderStatuses.map(status => (
                                    <option key={status.id} value={status.id.toString()}>
                                        {status.statusName}
                                    </option>
                                ))}
                            </select>
                        </div>

                        <div className="form-group">
                            <label htmlFor="comment" className="form-label">Комментарий к изменению статуса</label>
                            <textarea
                                id="comment"
                                className="form-textarea"
                                value={statusComment}
                                onChange={(e) => setStatusComment(e.target.value)}
                                disabled={processing}
                                placeholder="Добавьте комментарий к изменению статуса (опционально)"
                                rows={3}
                            />
                        </div>

                        <div className="form-actions">
                            <button
                                className="form-button primary"
                                onClick={handleUpdateStatus}
                                disabled={processing || selectedStatus === order.statusId.toString()}
                            >
                                {processing ? 'Обновление...' : 'Обновить статус'}
                            </button>
                        </div>
                    </div>
                </div>
            </div>

            {/* Диалог отмены заказа */}
            <ConfirmDialog
                open={showCancelDialog}
                title="Отмена заказа"
                message={
                    <div>
                        <p>Вы уверены, что хотите отменить заказ #{order.id}?</p>
                        <div className="form-group dialog-form-group">
                            <label htmlFor="cancelReason" className="form-label">Причина отмены</label>
                            <textarea
                                id="cancelReason"
                                className="form-textarea"
                                value={cancelReason}
                                onChange={(e) => setCancelReason(e.target.value)}
                                disabled={processing}
                                placeholder="Укажите причину отмены заказа"
                                rows={3}
                            />
                        </div>
                    </div>
                }
                confirmText={processing ? "Отмена..." : "Отменить заказ"}
                cancelText="Не отменять"
                onConfirm={handleCancelOrder}
                onCancel={() => {
                    setShowCancelDialog(false);
                    setCancelReason('');
                }}
            />

            {/* Диалог обработки оплаты */}
            <ConfirmDialog
                open={showProcessPaymentDialog}
                title="Обработка оплаты"
                message={
                    <div>
                        <p>Введите идентификатор транзакции для подтверждения оплаты:</p>
                        <div className="form-group dialog-form-group">
                            <label htmlFor="paymentReference" className="form-label">Идентификатор транзакции <span className="required">*</span></label>
                            <input
                                type="text"
                                id="paymentReference"
                                className="form-input"
                                value={paymentReference}
                                onChange={(e) => setPaymentReference(e.target.value)}
                                disabled={processing}
                                placeholder="Например: INV-12345 или PAYMENT-ID-123"
                            />
                        </div>
                    </div>
                }
                confirmText={processing ? "Обработка..." : "Подтвердить оплату"}
                cancelText="Отмена"
                onConfirm={handleProcessPayment}
                onCancel={() => {
                    setShowProcessPaymentDialog(false);
                    setPaymentReference('');
                }}
            />
        </div>
    );
};

export default OrderDetailPage;