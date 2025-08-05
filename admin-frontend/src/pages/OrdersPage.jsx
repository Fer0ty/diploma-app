import React, {useCallback, useEffect, useState} from 'react';
import {Link, useNavigate} from 'react-router-dom';
import {useAuth} from '../context/AuthContext';
import {useToast} from '../context/ToastContext';
import orderAPI from '../api/orderAPI';
import LoadingScreen from '../components/common/LoadingScreen';
import './OrdersPage.css';

const OrdersPage = () => {
    const {user, logout} = useAuth();
    const navigate = useNavigate();
    const {showSuccess, showError, showInfo} = useToast();

    const [orders, setOrders] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [orderStatuses, setOrderStatuses] = useState([]);

    const [pagination, setPagination] = useState({
        page: 0,
        size: 10,
        totalElements: 0,
        totalPages: 0
    });

    const loadOrders = useCallback(async () => {
        if (!localStorage.getItem('token')) {
            setError('Необходимо авторизоваться для доступа к заказам');
            setLoading(false);
            showError('Необходимо авторизоваться для доступа к заказам');
            navigate('/login');
            return;
        }

        if (!user?.tenantId) {
            setError('Информация о магазине отсутствует. Необходимо заново авторизоваться.');
            setLoading(false);
            showError('Информация о магазине отсутствует. Необходимо заново авторизоваться.');
            logout();
            navigate('/login');
            return;
        }

        setLoading(true);

        try {
            const params = {
                page: pagination.page,
                size: pagination.size
            };

            showInfo('Загрузка заказов...');
            const response = await orderAPI.getOrders(params);

            setOrders(response.content || []);
            setPagination({
                page: response.number || 0,
                size: response.size || 10,
                totalElements: response.totalElements || 0,
                totalPages: response.totalPages || 0
            });

            setError(null);
        } catch (error) {
            console.error('Error loading orders:', error);

            let errorMessage = 'Ошибка при загрузке заказов';

            if (error.response) {
                if (error.response.status === 403 &&
                    error.response.data?.message?.includes("Tenant context not established")) {
                    errorMessage = 'Не удалось определить магазин для текущего пользователя. Попробуйте выйти и войти снова.';
                    logout();
                    navigate('/login');
                } else {
                    errorMessage = `Ошибка при загрузке заказов: ${error.response.data?.message || error.response.statusText}`;
                }
            } else if (error.message) {
                errorMessage = error.message;

                if (error.message.includes('магазин') || error.message.includes('авторизоваться')) {
                    logout();
                    navigate('/login');
                }
            }

            setError(errorMessage);
            showError(errorMessage);
        } finally {
            setLoading(false);
        }
    }, [pagination.page, pagination.size, user?.tenantId, navigate, logout, showError, showInfo]);

    const loadOrderStatuses = useCallback(async () => {
        try {
            const statuses = await orderAPI.getOrderStatuses();
            setOrderStatuses(statuses);
        } catch (error) {
            console.error('Error loading order statuses:', error);
        }
    }, []);

    const handlePageChange = (newPage) => {
        setPagination(prev => ({
            ...prev,
            page: newPage
        }));
    };

    const handleSizeChange = (newSize) => {
        setPagination(prev => ({
            ...prev,
            page: 0,
            size: newSize
        }));
    };

    useEffect(() => {
        loadOrders();
    }, [loadOrders]);

    useEffect(() => {
        loadOrderStatuses();
    }, [loadOrderStatuses]);

    if (loading && orders.length === 0) {
        return <LoadingScreen/>;
    }

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

    const getStatusColor = (statusName) => {
        const statusColors = {
            'Created': 'var(--info-color)',
            'Paid': 'var(--primary-color)',
            'Processing': 'var(--warning-color)',
            'Shipped': 'var(--warning-color)',
            'Delivered': 'var(--success-color)',
            'Canceled': 'var(--error-color)',
            'Returned': 'var(--error-color)'
        };

        return statusColors[statusName] || 'var(--text-secondary)';
    };

    return (
        <div className="page-container">
            <div className="page-header">
                <h1 className="page-title">Заказы</h1>
            </div>

            {error && (
                <div className="error-alert">
                    <strong>Ошибка:</strong> {error}
                </div>
            )}

            <div className="page-content">
                {/* Список заказов */}
                {orders.length === 0 ? (
                    <div className="empty-state">
                        <h3>Заказы не найдены</h3>
                        <p>У магазина пока нет заказов</p>
                    </div>
                ) : (
                    <div className="orders-table-container">
                        <table className="orders-table">
                            <thead>
                            <tr>
                                <th>ID</th>
                                <th>Дата создания</th>
                                <th>Клиент</th>
                                <th>Статус</th>
                                <th>Сумма</th>
                                <th>Действия</th>
                            </tr>
                            </thead>
                            <tbody>
                            {orders.map(order => (
                                <tr key={order.id}>
                                    <td>{order.id}</td>
                                    <td>{formatDate(order.createdAt)}</td>
                                    <td>{order.customerName}</td>
                                    <td>
                                            <span
                                                className="order-status-badge"
                                                style={{backgroundColor: getStatusColor(order.statusName)}}
                                            >
                                                {order.statusName}
                                            </span>
                                    </td>
                                    <td>{formatAmount(order.totalAmount)}</td>
                                    <td className="actions-cell">
                                        <Link
                                            to={`/orders/${order.id}`}
                                            className="action-link view-link"
                                            title="Просмотреть"
                                        >
                                            👁️
                                        </Link>
                                    </td>
                                </tr>
                            ))}
                            </tbody>
                        </table>

                        {loading && (
                            <div className="table-loading-overlay">
                                <div className="loading-spinner"></div>
                            </div>
                        )}

                        {/* Пагинация */}
                        {pagination.totalPages > 0 && (
                            <div className="pagination">
                                <div className="pagination-info">
                                    Показано {pagination.totalElements > 0 ? (pagination.page * pagination.size + 1) : 0}-
                                    {Math.min((pagination.page + 1) * pagination.size, pagination.totalElements)} из {pagination.totalElements} заказов
                                </div>

                                <div className="pagination-controls">
                                    <button
                                        className="pagination-button"
                                        disabled={pagination.page === 0}
                                        onClick={() => handlePageChange(0)}
                                    >
                                        «
                                    </button>
                                    <button
                                        className="pagination-button"
                                        disabled={pagination.page === 0}
                                        onClick={() => handlePageChange(pagination.page - 1)}
                                    >
                                        ‹
                                    </button>

                                    {/* Номера страниц - показываем максимум 5 страниц */}
                                    {(() => {
                                        const pageButtons = [];
                                        const maxButtons = 5;
                                        const halfMax = Math.floor(maxButtons / 2);

                                        let startPage = Math.max(0, pagination.page - halfMax);
                                        let endPage = Math.min(pagination.totalPages - 1, pagination.page + halfMax);

                                        if (endPage - startPage + 1 < maxButtons) {
                                            if (startPage === 0) {
                                                endPage = Math.min(pagination.totalPages - 1, maxButtons - 1);
                                            } else if (endPage === pagination.totalPages - 1) {
                                                startPage = Math.max(0, pagination.totalPages - maxButtons);
                                            }
                                        }

                                        for (let i = startPage; i <= endPage; i++) {
                                            pageButtons.push(
                                                <button
                                                    key={i}
                                                    className={`pagination-button ${i === pagination.page ? 'active' : ''}`}
                                                    onClick={() => handlePageChange(i)}
                                                >
                                                    {i + 1}
                                                </button>
                                            );
                                        }

                                        return pageButtons;
                                    })()}

                                    <button
                                        className="pagination-button"
                                        disabled={pagination.page >= pagination.totalPages - 1}
                                        onClick={() => handlePageChange(pagination.page + 1)}
                                    >
                                        ›
                                    </button>
                                    <button
                                        className="pagination-button"
                                        disabled={pagination.page >= pagination.totalPages - 1}
                                        onClick={() => handlePageChange(pagination.totalPages - 1)}
                                    >
                                        »
                                    </button>
                                </div>

                                <div className="pagination-size">
                                    <label htmlFor="pageSize">Элементов на странице:</label>
                                    <select
                                        id="pageSize"
                                        value={pagination.size}
                                        onChange={(e) => handleSizeChange(Number(e.target.value))}
                                        className="pagination-size-select"
                                    >
                                        <option value={10}>10</option>
                                        <option value={20}>20</option>
                                        <option value={50}>50</option>
                                        <option value={100}>100</option>
                                    </select>
                                </div>
                            </div>
                        )}
                    </div>
                )}
            </div>
        </div>
    );
};

export default OrdersPage;