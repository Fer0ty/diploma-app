import React, {useEffect, useState, useCallback} from 'react';
import {Link, useNavigate} from 'react-router-dom';
import {useAuth} from '../context/AuthContext';
import {useToast} from '../context/ToastContext';
import productAPI from '../api/productAPI';
import LoadingScreen from '../components/common/LoadingScreen';
import './ProductsPage.css';
import ConfirmDialog from '../components/common/ConfirmDialog';

const ProductsPage = () => {
    const {user, logout} = useAuth();
    const navigate = useNavigate();
    const {showSuccess, showError, showInfo} = useToast();

    const [products, setProducts] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [productToDelete, setProductToDelete] = useState(null);
    const [showConfirmDialog, setShowConfirmDialog] = useState(false);

    const [filters, setFilters] = useState({
        nameLike: '',
        category: '',
        active: null
    });
    const [pagination, setPagination] = useState({
        page: 0,
        size: 10,
        totalElements: 0,
        totalPages: 0
    });

    const loadProducts = useCallback(async () => {
        if (!localStorage.getItem('token')) {
            setError('Необходимо авторизоваться для доступа к товарам');
            setLoading(false);
            showError('Необходимо авторизоваться для доступа к товарам');
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
                size: pagination.size,
                ...filters
            };

            Object.keys(params).forEach(key => {
                if (params[key] === '' || params[key] === null || params[key] === undefined) {
                    delete params[key];
                }
            });

            showInfo('Загрузка товаров...');
            const response = await productAPI.getProducts(params);

            setProducts(response.content || []);
            setPagination({
                page: response.number || 0,
                size: response.size || 10,
                totalElements: response.totalElements || 0,
                totalPages: response.totalPages || 0
            });

            setError(null);
        } catch (error) {
            console.error('Error loading products:', error);

            let errorMessage = 'Ошибка при загрузке товаров';

            if (error.response) {
                if (error.response.status === 403 &&
                    error.response.data?.message?.includes("Tenant context not established")) {
                    errorMessage = 'Не удалось определить магазин для текущего пользователя. Попробуйте выйти и войти снова.';
                    logout();
                    navigate('/login');
                } else {
                    errorMessage = `Ошибка при загрузке товаров: ${error.response.data?.message || error.response.statusText}`;
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
    }, [pagination.page, pagination.size, filters, user?.tenantId, navigate, logout, showError, showInfo]);

    const handleDeleteClick = (product) => {
        setProductToDelete(product);
        setShowConfirmDialog(true);
    };

    const handleConfirmDelete = async () => {
        if (!productToDelete) return;

        try {
            showInfo(`Удаление товара "${productToDelete.name}"...`);
            await productAPI.deleteProduct(productToDelete.id);

            setProducts(prevProducts => prevProducts.filter(p => p.id !== productToDelete.id));

            showSuccess(`Товар "${productToDelete.name}" успешно удален`);
        } catch (error) {
            console.error(`Error deleting product with ID ${productToDelete.id}:`, error);

            let errorMessage = 'Ошибка при удалении товара';

            if (error.response) {
                if (error.response.status === 403 &&
                    error.response.data?.message?.includes("Tenant context not established")) {
                    errorMessage = 'Не удалось определить магазин для текущего пользователя. Попробуйте выйти и войти снова.';
                    logout();
                    navigate('/login');
                } else {
                    errorMessage = `Ошибка при удалении товара: ${error.response.data?.message || error.response.statusText}`;
                }
            } else if (error.message) {
                errorMessage = error.message;

                if (error.message.includes('магазин') || error.message.includes('авторизоваться')) {
                    logout();
                    navigate('/login');
                }
            }

            showError(errorMessage);
        } finally {
            setShowConfirmDialog(false);
            setProductToDelete(null);
        }
    };

    const handleCancelDelete = () => {
        setShowConfirmDialog(false);
        setProductToDelete(null);
    };

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

    const handleFilterChange = (e) => {
        const {name, value} = e.target;
        setFilters(prev => ({
            ...prev,
            [name]: value
        }));
    };

    const handleActiveFilterChange = (e) => {
        const value = e.target.value;
        setFilters(prev => ({
            ...prev,
            active: value === 'all' ? null : value === 'active'
        }));
    };

    const applyFilters = () => {
        setPagination(prev => ({
            ...prev,
            page: 0
        }));

        loadProducts();
    };

    const resetFilters = () => {
        setFilters({
            nameLike: '',
            category: '',
            active: null
        });

        setPagination(prev => ({
            ...prev,
            page: 0
        }));

        setTimeout(() => {
            loadProducts();
        }, 0);
    };

    useEffect(() => {
        loadProducts();
    }, [loadProducts]);

    if (loading && products.length === 0) {
        return <LoadingScreen/>;
    }

    const formatPrice = (price) => {
        return new Intl.NumberFormat('ru-RU', {
            style: 'currency',
            currency: 'RUB',
            minimumFractionDigits: 2
        }).format(price);
    };

    return (
        <div className="page-container">
            <div className="page-header">
                <h1 className="page-title">Товары</h1>
                <Link to="/products/new" className="action-button">
                    <span className="action-button-icon">+</span>
                    Добавить товар
                </Link>
            </div>

            {error && (
                <div className="error-alert">
                    <strong>Ошибка:</strong> {error}
                </div>
            )}

            <div className="page-content">
                {/* Фильтры */}
                <div className="product-filters">
                    <h3 className="filters-title">Фильтры</h3>
                    <div className="filters-form">
                        <div className="filters-row">
                            <div className="filter-group">
                                <label htmlFor="nameLike" className="filter-label">Название</label>
                                <input
                                    type="text"
                                    id="nameLike"
                                    name="nameLike"
                                    className="filter-input"
                                    placeholder="Поиск по названию"
                                    value={filters.nameLike}
                                    onChange={handleFilterChange}
                                />
                            </div>

                            <div className="filter-group">
                                <label htmlFor="category" className="filter-label">Категория</label>
                                <input
                                    type="text"
                                    id="category"
                                    name="category"
                                    className="filter-input"
                                    placeholder="Фильтр по категории"
                                    value={filters.category}
                                    onChange={handleFilterChange}
                                />
                            </div>

                            <div className="filter-group">
                                <label htmlFor="active" className="filter-label">Статус</label>
                                <select
                                    id="active"
                                    className="filter-select"
                                    value={filters.active === null ? 'all' : (filters.active ? 'active' : 'inactive')}
                                    onChange={handleActiveFilterChange}
                                >
                                    <option value="all">Все товары</option>
                                    <option value="active">Активные</option>
                                    <option value="inactive">Неактивные</option>
                                </select>
                            </div>
                        </div>

                        <div className="filters-actions">
                            <button
                                type="button"
                                className="filter-button filter-apply"
                                onClick={applyFilters}
                            >
                                Применить
                            </button>
                            <button
                                type="button"
                                className="filter-button filter-reset"
                                onClick={resetFilters}
                            >
                                Сбросить
                            </button>
                        </div>
                    </div>
                </div>

                {/* Список товаров */}
                {products.length === 0 ? (
                    <div className="empty-state">
                        <h3>Товары не найдены</h3>
                        <p>Попробуйте изменить параметры поиска или добавьте новый товар</p>
                        <Link to="/products/new" className="action-button">
                            Добавить товар
                        </Link>
                    </div>
                ) : (
                    <div className="product-table-container">
                        <table className="product-table">
                            <thead>
                            <tr>
                                <th>Фото</th>
                                <th>Название</th>
                                <th>Категория</th>
                                <th>Цена</th>
                                <th>На складе</th>
                                <th>Статус</th>
                                <th>Действия</th>
                            </tr>
                            </thead>
                            <tbody>
                            {products.map(product => (
                                <tr key={product.id} className={!product.active ? 'inactive-product' : ''}>
                                    {/* Обновленная ячейка с фотографией */}
                                    <td className="product-image-cell">
                                        {product.photos && product.photos.length > 0 ? (
                                            <div className="product-image-container">
                                                <img
                                                    src={product.photos.find(photo => photo.main)?.filePath || product.photos[0].filePath}
                                                    alt={product.name}
                                                    className="product-thumbnail"
                                                />
                                                {product.photos.length > 1 && (
                                                    <div className="photo-count-badge" title={`${product.photos.length} фотографий`}>
                                                        {product.photos.length}
                                                    </div>
                                                )}
                                            </div>
                                        ) : (
                                            <div className="no-image">Нет фото</div>
                                        )}
                                    </td>
                                    <td>
                                        <Link to={`/products/${product.id}`} className="product-name-link">
                                            {product.name}
                                        </Link>
                                    </td>
                                    <td>{product.category || '—'}</td>
                                    <td>{formatPrice(product.price)}</td>
                                    <td>{product.stockQuantity}</td>
                                    <td>
                                        <span className={`status-badge ${product.active ? 'status-active' : 'status-inactive'}`}>
                                            {product.active ? 'Активен' : 'Неактивен'}
                                        </span>
                                    </td>
                                    <td className="actions-cell">
                                        <Link to={`/products/${product.id}`} className="action-link edit-link"
                                              title="Редактировать">
                                            ✏️
                                        </Link>
                                        <button
                                            className="action-link delete-link"
                                            title="Удалить"
                                            onClick={() => handleDeleteClick(product)}
                                        >
                                            🗑️
                                        </button>
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
                                    {Math.min((pagination.page + 1) * pagination.size, pagination.totalElements)} из {pagination.totalElements} товаров
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

            {/* Диалог подтверждения удаления */}
            <ConfirmDialog
                open={showConfirmDialog}
                title="Подтверждение удаления"
                message={productToDelete ?
                    `Вы уверены, что хотите удалить товар "${productToDelete.name}"? Это действие нельзя отменить.` :
                    "Вы уверены, что хотите удалить этот товар?"
                }
                confirmText="Удалить"
                cancelText="Отмена"
                onConfirm={handleConfirmDelete}
                onCancel={handleCancelDelete}
            />
        </div>
    );
};

export default ProductsPage;