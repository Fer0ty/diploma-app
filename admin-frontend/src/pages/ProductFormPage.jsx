import React, { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import productAPI from '../api/productAPI';
import photoAPI from '../api/photoAPI';
import LoadingScreen from '../components/common/LoadingScreen';
import ProductPhotosManager from '../components/products/ProductPhotosManager';
import './ProductFormPage.css';

const ProductFormPage = () => {
    const { productId } = useParams();
    const navigate = useNavigate();
    const isNewProduct = !productId || productId === 'new';

    const { user, logout } = useAuth();
    const { showSuccess, showError, showInfo } = useToast();

    const [formData, setFormData] = useState({
        name: '',
        description: '',
        price: '',
        stockQuantity: '',
        category: '',
        active: true
    });

    const [currentProduct, setCurrentProduct] = useState(null);
    const [errors, setErrors] = useState({});
    const [serverError, setServerError] = useState('');
    const [loading, setLoading] = useState(false);
    const [saving, setSaving] = useState(false);

    const [photos, setPhotos] = useState([]);
    const [loadingPhotos, setLoadingPhotos] = useState(false);

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

    useEffect(() => {
        if (!isNewProduct) {
            if (!checkAuth()) return;

            setLoading(true);
            showInfo('Загрузка данных товара...');

            productAPI.getProductById(productId)
                .then(product => {
                    setCurrentProduct(product);
                    setFormData({
                        name: product.name || '',
                        description: product.description || '',
                        price: product.price?.toString() || '',
                        stockQuantity: product.stockQuantity?.toString() || '',
                        category: product.category || '',
                        active: product.active !== undefined ? product.active : true
                    });

                    if (product.photos && product.photos.length > 0) {
                        setPhotos(product.photos);
                    }

                    showSuccess('Данные товара загружены');
                })
                .catch(error => {
                    console.error('Error loading product:', error);

                    let errorMessage;
                    if (error.response) {
                        if (error.response.status === 403 &&
                            error.response.data?.message?.includes("Tenant context not established")) {
                            errorMessage = 'Не удалось определить магазин для текущего пользователя. Необходимо заново авторизоваться.';
                            logout();
                            navigate('/login');
                        } else {
                            errorMessage = `Ошибка при загрузке товара: ${error.response.data?.message || error.response.statusText}`;
                        }
                    } else if (error.message) {
                        errorMessage = error.message;

                        if (error.message.includes('магазин') || error.message.includes('авторизоваться')) {
                            logout();
                            navigate('/login');
                        }
                    } else {
                        errorMessage = 'Ошибка при загрузке товара. Проверьте соединение с сервером.';
                    }

                    setServerError(errorMessage);
                    showError(errorMessage);
                })
                .finally(() => {
                    setLoading(false);
                });
        }
    }, [isNewProduct, productId, checkAuth, logout, navigate, showError, showInfo, showSuccess]);

    useEffect(() => {
        if (!isNewProduct && currentProduct && productId) {
            if (!currentProduct.photos || currentProduct.photos.length === 0) {
                loadProductPhotos(productId);
            }
        }
    }, [isNewProduct, currentProduct, productId]);

    const loadProductPhotos = async (prodId) => {
        if (!prodId) return;

        try {
            setLoadingPhotos(true);
            const photosList = await photoAPI.getProductPhotos(prodId);
            setPhotos(photosList);
        } catch (error) {
            console.error('Error loading product photos:', error);
            showError('Не удалось загрузить фотографии товара');
        } finally {
            setLoadingPhotos(false);
        }
    };

    const handlePhotosChanged = (newPhotos) => {
        setPhotos(newPhotos);
        if (currentProduct) {
            setCurrentProduct({
                ...currentProduct,
                photos: newPhotos
            });
        }
    };

    const handleChange = (e) => {
        const { name, value, type, checked } = e.target;

        setFormData(prev => ({
            ...prev,
            [name]: type === 'checkbox' ? checked : value
        }));

        if (errors[name]) {
            setErrors(prev => ({
                ...prev,
                [name]: ''
            }));
        }

        if (serverError) {
            setServerError('');
        }
    };

    const validateForm = () => {
        const newErrors = {};

        if (!formData.name.trim()) {
            newErrors.name = 'Название товара обязательно';
        }

        if (!formData.price.trim()) {
            newErrors.price = 'Цена товара обязательна';
        } else {
            const price = parseFloat(formData.price.replace(',', '.'));
            if (isNaN(price) || price <= 0) {
                newErrors.price = 'Цена должна быть положительным числом';
            }
        }

        if (!formData.stockQuantity.trim()) {
            newErrors.stockQuantity = 'Количество на складе обязательно';
        } else {
            const quantity = parseInt(formData.stockQuantity);
            if (isNaN(quantity) || quantity < 0) {
                newErrors.stockQuantity = 'Количество должно быть неотрицательным целым числом';
            }
        }

        setErrors(newErrors);
        return Object.keys(newErrors).length === 0;
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        if (!checkAuth()) return;

        if (!validateForm()) {
            showError('Пожалуйста, исправьте ошибки в форме');
            return;
        }

        setSaving(true);
        setServerError('');

        try {
            const productData = {
                name: formData.name,
                description: formData.description || null,
                price: parseFloat(formData.price.replace(',', '.')),
                stockQuantity: parseInt(formData.stockQuantity),
                category: formData.category || null,
                active: formData.active
            };

            let result;
            showInfo(isNewProduct ? 'Создание товара...' : 'Обновление товара...');

            if (isNewProduct) {
                result = await productAPI.createProduct(productData);
                showSuccess('Товар успешно создан');
                navigate(`/products/${result.id}`);
            } else {
                result = await productAPI.updateProduct(productId, productData);
                setCurrentProduct(result);
                showSuccess('Товар успешно обновлен');
            }

            return result;
        } catch (error) {
            console.error('Error saving product:', error);

            let errorMessage = '';

            if (error.response) {
                if (error.response.status === 403 &&
                    error.response.data?.message?.includes("Tenant context not established")) {
                    errorMessage = 'Не удалось определить магазин для текущего пользователя. Необходимо заново авторизоваться.';
                    showError(errorMessage);
                    logout();
                    navigate('/login');
                } else {
                    errorMessage = `Ошибка при ${isNewProduct ? 'создании' : 'обновлении'} товара: ${error.response.data?.message || error.response.statusText}`;
                    showError(errorMessage);
                }
            } else if (error.message) {
                errorMessage = error.message;
                showError(errorMessage);

                if (error.message.includes('магазин') || error.message.includes('авторизоваться')) {
                    logout();
                    navigate('/login');
                }
            } else {
                errorMessage = `Ошибка при ${isNewProduct ? 'создании' : 'обновлении'} товара`;
                showError(errorMessage);
            }

            setServerError(errorMessage);
            return null;
        } finally {
            setSaving(false);
        }
    };

    const handleCancel = () => {
        navigate('/products');
    };

    if (loading) {
        return <LoadingScreen />;
    }

    return (
        <div className="page-container">
            <div className="page-header">
                <h1 className="page-title">
                    {isNewProduct ? 'Создание нового товара' : 'Редактирование товара'}
                </h1>
            </div>

            {serverError && (
                <div className="error-alert">
                    <strong>Ошибка:</strong> {serverError}
                </div>
            )}

            <div className="product-form-content">
                <form onSubmit={handleSubmit} className="product-form">
                    <div className="form-section">
                        <h2 className="form-section-title">Основная информация</h2>

                        <div className="form-group">
                            <label htmlFor="name" className="form-label">
                                Название товара <span className="required">*</span>
                            </label>
                            <input
                                type="text"
                                id="name"
                                name="name"
                                className={`form-input ${errors.name ? 'error' : ''}`}
                                value={formData.name}
                                onChange={handleChange}
                                maxLength={255}
                            />
                            {errors.name && <div className="form-error">{errors.name}</div>}
                        </div>

                        <div className="form-group">
                            <label htmlFor="description" className="form-label">
                                Описание
                            </label>
                            <textarea
                                id="description"
                                name="description"
                                className="form-textarea"
                                value={formData.description || ''}
                                onChange={handleChange}
                                rows={5}
                            />
                        </div>

                        <div className="form-row">
                            <div className="form-group form-group-half">
                                <label htmlFor="price" className="form-label">
                                    Цена <span className="required">*</span>
                                </label>
                                <input
                                    type="text"
                                    id="price"
                                    name="price"
                                    className={`form-input ${errors.price ? 'error' : ''}`}
                                    value={formData.price}
                                    onChange={handleChange}
                                    placeholder="0.00"
                                />
                                {errors.price && <div className="form-error">{errors.price}</div>}
                                <small className="form-hint">Используйте точку или запятую в качестве разделителя</small>
                            </div>

                            <div className="form-group form-group-half">
                                <label htmlFor="stockQuantity" className="form-label">
                                    Количество на складе <span className="required">*</span>
                                </label>
                                <input
                                    type="text"
                                    id="stockQuantity"
                                    name="stockQuantity"
                                    className={`form-input ${errors.stockQuantity ? 'error' : ''}`}
                                    value={formData.stockQuantity}
                                    onChange={handleChange}
                                    placeholder="0"
                                />
                                {errors.stockQuantity && <div className="form-error">{errors.stockQuantity}</div>}
                            </div>
                        </div>

                        <div className="form-group">
                            <label htmlFor="category" className="form-label">
                                Категория
                            </label>
                            <input
                                type="text"
                                id="category"
                                name="category"
                                className="form-input"
                                value={formData.category || ''}
                                onChange={handleChange}
                                maxLength={100}
                            />
                        </div>

                        <div className="form-group form-checkbox-group">
                            <label className="form-checkbox-label">
                                <input
                                    type="checkbox"
                                    name="active"
                                    checked={formData.active}
                                    onChange={handleChange}
                                    className="form-checkbox"
                                />
                                <span className="checkbox-text">Товар активен (доступен для продажи)</span>
                            </label>
                        </div>
                    </div>

                    {/* Добавляем раздел управления фотографиями */}
                    {!isNewProduct && (
                        <div className="form-section">
                            <ProductPhotosManager
                                productId={productId}
                                photos={photos}
                                onPhotosChanged={handlePhotosChanged}
                            />
                        </div>
                    )}

                    {/* Примечание про добавление фотографий */}
                    {isNewProduct && (
                        <div className="form-section">
                            <h2 className="form-section-title">Фотографии товара</h2>
                            <div className="photos-note">
                                <p>Фотографии можно будет добавить после создания товара.</p>
                            </div>
                        </div>
                    )}

                    <div className="form-actions">
                        <button
                            type="button"
                            className="form-button secondary"
                            onClick={handleCancel}
                            disabled={saving}
                        >
                            Отмена
                        </button>
                        <button
                            type="submit"
                            className="form-button primary"
                            disabled={saving}
                        >
                            {saving ? (
                                <>
                                    <span className="spinner-icon">⟳</span>
                                    {isNewProduct ? 'Создание...' : 'Сохранение...'}
                                </>
                            ) : (
                                isNewProduct ? 'Создать товар' : 'Сохранить изменения'
                            )}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default ProductFormPage;