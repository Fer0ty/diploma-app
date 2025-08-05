import React, {useEffect, useRef, useState} from 'react';
import {useNavigate} from 'react-router-dom';
import {useAuth} from '../context/AuthContext';
import {useToast} from '../context/ToastContext';
import themeAPI from '../api/themeAPI';
import storeAPI from '../api/storeAPI';
import photoAPI from '../api/photoAPI';
import LoadingScreen from '../components/common/LoadingScreen';
import '../pages/pages.css';
import './SettingsPage.css';

const fontOptions = [
    'Arial, sans-serif',
    'Helvetica, sans-serif',
    'Roboto, sans-serif',
    'Open Sans, sans-serif',
    'Lato, sans-serif',
    'Montserrat, sans-serif',
    'Raleway, sans-serif',
    'Poppins, sans-serif',
    'Source Sans Pro, sans-serif',
    'Inter, sans-serif',
    'Georgia, serif',
    'Times New Roman, serif',
    'Playfair Display, serif',
    'Merriweather, serif'
];

const borderRadiusOptions = [
    '0px',
    '2px',
    '4px',
    '6px',
    '8px',
    '10px',
    '12px',
    '16px',
    '20px',
    '50px'
];

const shadowOptions = [
    '',
    'none',
    '0 1px 3px rgba(0,0,0,0.1)',
    '0 2px 4px rgba(0,0,0,0.1)',
    '0 4px 6px rgba(0,0,0,0.1)',
    '0 4px 8px rgba(0,0,0,0.15)',
    '0 8px 16px rgba(0,0,0,0.15)',
    '0 12px 24px rgba(0,0,0,0.15)'
];

const SettingsPage = () => {
    const {user, logout} = useAuth();
    const navigate = useNavigate();
    const {showSuccess, showError, showInfo} = useToast();

    const logoInputRef = useRef(null);
    const headerImageInputRef = useRef(null);
    const footerLogoInputRef = useRef(null);
    const faviconInputRef = useRef(null);

    const [theme, setTheme] = useState({
        // Базовые цвета
        primaryColor: '#3498db',
        secondaryColor: '#2ecc71',
        accentColor: '#e74c3c',
        textColor: '#333333',
        backgroundColor: '#ffffff',

        // Шрифты
        fontFamily: 'Roboto, sans-serif',
        headingFontFamily: '',
        bodyFontFamily: '',

        // Изображения
        logoUrl: '',
        headerImageUrl: '',
        footerLogoUrl: '',
        faviconUrl: '',

        // Скругления элементов
        buttonRadius: '4px',
        cardRadius: '8px',
        inputRadius: '4px',

        // Цвета элементов
        buttonTextColor: '#ffffff',
        footerBackgroundColor: '#2c3e50',
        footerTextColor: '#ecf0f1',

        // Цвета статусов
        successColor: '#27ae60',
        errorColor: '#c0392b',
        warningColor: '#f39c12',
        infoColor: '#3498db',

        // Интерактивные цвета
        hoverColor: '#34495e',
        activeColor: '#2980b9',

        // Тени
        boxShadow: '0 2px 4px rgba(0,0,0,0.1)',
        cardShadow: '0 4px 8px rgba(0,0,0,0.1)'
    });

    const [storeInfo, setStoreInfo] = useState({
        name: '',
        subdomain: '',
        contactEmail: '',
        contactPhone: '',
        integrations: {
            ozonClientId: '',
            ozonApiKey: '',
            wildberriesApiKey: '',
            yookassaIdempotencyKey: '',
            yookassaSecretKey: ''
        }
    });


    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);
    const [error, setError] = useState(null);
    const [activeColorPicker, setActiveColorPicker] = useState(null);
    const [activeTab, setActiveTab] = useState('colors');

    useEffect(() => {
        const loadSettings = async () => {
            if (!localStorage.getItem('token')) {
                setError('Необходимо авторизоваться для доступа к настройкам');
                setLoading(false);
                showError('Необходимо авторизоваться для доступа к настройкам');
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
                // Загрузка темы
                showInfo('Загрузка настроек темы...');
                const themeData = await themeAPI.getTheme();

                setTheme({
                    // Базовые цвета
                    primaryColor: themeData.primaryColor || '#3498db',
                    secondaryColor: themeData.secondaryColor || '#2ecc71',
                    accentColor: themeData.accentColor || '#e74c3c',
                    textColor: themeData.textColor || '#333333',
                    backgroundColor: themeData.backgroundColor || '#ffffff',

                    // Шрифты
                    fontFamily: themeData.fontFamily || 'Roboto, sans-serif',
                    headingFontFamily: themeData.headingFontFamily || '',
                    bodyFontFamily: themeData.bodyFontFamily || '',

                    // Изображения
                    logoUrl: themeData.logoUrl || '',
                    headerImageUrl: themeData.headerImageUrl || '',
                    footerLogoUrl: themeData.footerLogoUrl || '',
                    faviconUrl: themeData.faviconUrl || '',

                    // Скругления элементов
                    buttonRadius: themeData.buttonRadius || '4px',
                    cardRadius: themeData.cardRadius || '8px',
                    inputRadius: themeData.inputRadius || '4px',

                    // Цвета элементов
                    buttonTextColor: themeData.buttonTextColor || '#ffffff',
                    footerBackgroundColor: themeData.footerBackgroundColor || '#2c3e50',
                    footerTextColor: themeData.footerTextColor || '#ecf0f1',

                    // Цвета статусов
                    successColor: themeData.successColor || '#27ae60',
                    errorColor: themeData.errorColor || '#c0392b',
                    warningColor: themeData.warningColor || '#f39c12',
                    infoColor: themeData.infoColor || '#3498db',

                    // Интерактивные цвета
                    hoverColor: themeData.hoverColor || '#34495e',
                    activeColor: themeData.activeColor || '#2980b9',

                    // Тени
                    boxShadow: themeData.boxShadow || '0 2px 4px rgba(0,0,0,0.1)',
                    cardShadow: themeData.cardShadow || '0 4px 8px rgba(0,0,0,0.1)'
                });

                // Загрузка информации о магазине
                try {
                    showInfo('Загрузка информации о магазине...');
                    const storeData = await storeAPI.getStore();

                    setStoreInfo({
                        name: storeData.name || '',
                        subdomain: storeData.subdomain || '',
                        contactEmail: storeData.contactEmail || '',
                        contactPhone: storeData.contactPhone || '',
                        integrations: {
                            ozonApiKey: storeData.ozonApiKey || '',
                            wildberriesApiKey: storeData.wildberriesApiKey || '',
                            yookassaIdempotencyKey: storeData.yookassaIdempotencyKey || '',
                            yookassaSecretKey: storeData.yookassaSecretKey || ''
                        }
                    });
                } catch (error) {
                    console.error('Error loading store info:', error);
                    // Не критично, продолжаем работу
                }

                setError(null);
            } catch (error) {
                console.error('Error loading settings:', error);
                handleApiError(error, 'загрузке настроек');
            } finally {
                setLoading(false);
            }
        };

        loadSettings();
    }, [user, navigate, logout, showError, showInfo]);

    const handleApiError = (error, action) => {
        let errorMessage = `Ошибка при ${action}`;

        if (error.response) {
            if (error.response.status === 403 &&
                error.response.data?.message?.includes("Tenant context not established")) {
                errorMessage = 'Не удалось определить магазин для текущего пользователя. Необходимо заново авторизоваться.';
                logout();
                navigate('/login');
            } else {
                errorMessage = `Ошибка при ${action}: ${error.response.data?.message || error.response.statusText}`;
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
    };

    const handleChange = (e) => {
        const {name, value} = e.target;
        setTheme(prev => ({
            ...prev,
            [name]: value
        }));
    };

    const handleColorChange = (color, name) => {
        setTheme(prev => ({
            ...prev,
            [name]: color.hex
        }));
    };

    const handleStoreChange = (e) => {
        const { name, value } = e.target;
        setStoreInfo(prev => ({
            ...prev,
            [name]: value
        }));
    };

    const handleIntegrationChange = (e) => {
        const { name, value } = e.target;
        setStoreInfo(prev => ({
            ...prev,
            integrations: {
                ...prev.integrations,
                [name]: value
            }
        }));
    };

    const handleFileUpload = async (e, type) => {
        const file = e.target.files[0];
        if (!file) return;

        const typeMap = {
            'logo': { category: 'logos', field: 'logoUrl', label: 'логотипа', ref: logoInputRef },
            'header': { category: 'headers', field: 'headerImageUrl', label: 'изображения заголовка', ref: headerImageInputRef },
            'footerLogo': { category: 'logos', field: 'footerLogoUrl', label: 'логотипа футера', ref: footerLogoInputRef },
            'favicon': { category: 'other', field: 'faviconUrl', label: 'иконки сайта', ref: faviconInputRef }
        };

        const config = typeMap[type];
        if (!config) return;

        try {
            setSaving(true);
            showInfo(`Загрузка ${config.label}...`);

            const result = await photoAPI.uploadFile(file, config.category);

            if (result.success) {
                setTheme(prev => ({
                    ...prev,
                    [config.field]: result.fileUrl
                }));
                showSuccess(`${config.label.charAt(0).toUpperCase() + config.label.slice(1)} успешно загружен`);
            } else {
                showError(`Ошибка при загрузке файла: ${result.message || 'Неизвестная ошибка'}`);
            }
        } catch (error) {
            console.error(`Error uploading ${type}:`, error);
            showError(`Ошибка при загрузке ${config.label}`);
        } finally {
            setSaving(false);
            if (config.ref.current) {
                config.ref.current.value = '';
            }
        }
    };

    const handleSave = async () => {
        try {
            setSaving(true);
            showInfo('Сохранение настроек...');

            // Сохраняем тему
            await themeAPI.updateTheme(theme);

            // Сохраняем информацию о магазине
            await storeAPI.updateStore({
                name: storeInfo.name,
                contactEmail: storeInfo.contactEmail,
                contactPhone: storeInfo.contactPhone,
                ozonApiKey: storeInfo.integrations.ozonApiKey,
                ozonClientId: storeInfo.integrations.ozonClientId,
                wildberriesApiKey: storeInfo.integrations.wildberriesApiKey,
                yookassaIdempotencyKey: storeInfo.integrations.yookassaIdempotencyKey,
                yookassaSecretKey: storeInfo.integrations.yookassaSecretKey
            });

            showSuccess('Настройки успешно сохранены');
        } catch (error) {
            console.error('Error saving settings:', error);
            handleApiError(error, 'сохранении настроек');
        } finally {
            setSaving(false);
        }
    };

    const ColorPreview = ({color, name, label}) => (
        <div className="color-setting">
            <label className="setting-label">{label}</label>
            <div className="color-input-group">
                <input
                    type="text"
                    name={name}
                    value={color}
                    onChange={handleChange}
                    className="color-input"
                />
                <div
                    className="color-preview"
                    style={{backgroundColor: color}}
                    onClick={() => setActiveColorPicker(name)}
                ></div>
            </div>
        </div>
    );

    const ImageUpload = ({type, url, label, inputRef}) => (
        <div className="image-setting">
            <label className="setting-label">{label}</label>
            {url && (
                <div className="image-preview">
                    <img src={url} alt={label} />
                    <button
                        className="remove-image-button"
                        onClick={() => setTheme(prev => ({...prev, [type]: ''}))}
                        title="Удалить изображение"
                    >
                        ✕
                    </button>
                </div>
            )}
            <div className="file-upload">
                <input
                    type="file"
                    ref={inputRef}
                    accept="image/*"
                    onChange={(e) => handleFileUpload(e, type)}
                    className="file-input"
                />
                <button
                    className="upload-button"
                    onClick={() => inputRef.current.click()}
                    disabled={saving}
                >
                    Загрузить {label.toLowerCase()}
                </button>
            </div>
        </div>
    );

    if (loading) {
        return <LoadingScreen />;
    }

    return (
        <div className="page-container">
            <div className="page-header">
                <h1 className="page-title">Настройки темы магазина</h1>
            </div>

            {error && (
                <div className="error-alert">
                    <strong>Ошибка:</strong> {error}
                </div>
            )}

            <div className="settings-content">
                <div className="theme-settings-card">
                    {/* Навигация по вкладкам */}
                    <div className="settings-tabs">
                        <button
                            className={`tab-button ${activeTab === 'colors' ? 'active' : ''}`}
                            onClick={() => setActiveTab('colors')}
                        >
                            Цвета
                        </button>
                        <button
                            className={`tab-button ${activeTab === 'typography' ? 'active' : ''}`}
                            onClick={() => setActiveTab('typography')}
                        >
                            Типографика
                        </button>
                        <button
                            className={`tab-button ${activeTab === 'images' ? 'active' : ''}`}
                            onClick={() => setActiveTab('images')}
                        >
                            Изображения
                        </button>
                        <button
                            className={`tab-button ${activeTab === 'layout' ? 'active' : ''}`}
                            onClick={() => setActiveTab('layout')}
                        >
                            Элементы
                        </button>
                        <button
                            className={`tab-button ${activeTab === 'store' ? 'active' : ''}`}
                            onClick={() => setActiveTab('store')}
                        >
                            Магазин
                        </button>
                        <button
                            className={`tab-button ${activeTab === 'preview' ? 'active' : ''}`}
                            onClick={() => setActiveTab('preview')}
                        >
                            Предпросмотр
                        </button>
                    </div>

                    {/* Содержимое вкладок */}
                    <div className="tab-content">
                        {activeTab === 'colors' && (
                            <div className="settings-section">
                                <h3 className="settings-subsection-title">Основные цвета</h3>
                                <div className="colors-grid">
                                    <ColorPreview color={theme.primaryColor} name="primaryColor" label="Основной цвет" />
                                    <ColorPreview color={theme.secondaryColor} name="secondaryColor" label="Вторичный цвет" />
                                    <ColorPreview color={theme.accentColor} name="accentColor" label="Акцентный цвет" />
                                    <ColorPreview color={theme.textColor} name="textColor" label="Цвет текста" />
                                    <ColorPreview color={theme.backgroundColor} name="backgroundColor" label="Цвет фона" />
                                </div>

                                <h3 className="settings-subsection-title">Цвета элементов интерфейса</h3>
                                <div className="colors-grid">
                                    <ColorPreview color={theme.buttonTextColor} name="buttonTextColor" label="Текст кнопок" />
                                    <ColorPreview color={theme.footerBackgroundColor} name="footerBackgroundColor" label="Фон футера" />
                                    <ColorPreview color={theme.footerTextColor} name="footerTextColor" label="Текст футера" />
                                    <ColorPreview color={theme.hoverColor} name="hoverColor" label="Цвет наведения" />
                                    <ColorPreview color={theme.activeColor} name="activeColor" label="Цвет активации" />
                                </div>

                                <h3 className="settings-subsection-title">Цвета статусов</h3>
                                <div className="colors-grid">
                                    <ColorPreview color={theme.successColor} name="successColor" label="Успех" />
                                    <ColorPreview color={theme.errorColor} name="errorColor" label="Ошибка" />
                                    <ColorPreview color={theme.warningColor} name="warningColor" label="Предупреждение" />
                                    <ColorPreview color={theme.infoColor} name="infoColor" label="Информация" />
                                </div>
                            </div>
                        )}

                        {activeTab === 'typography' && (
                            <div className="settings-section">
                                <h3 className="settings-subsection-title">Шрифты</h3>
                                <div className="typography-settings">
                                    <div className="font-setting">
                                        <label className="setting-label">Основной шрифт</label>
                                        <select
                                            name="fontFamily"
                                            value={theme.fontFamily}
                                            onChange={handleChange}
                                            className="font-select"
                                        >
                                            {fontOptions.map(font => (
                                                <option key={font} value={font} style={{fontFamily: font}}>
                                                    {font.split(',')[0]}
                                                </option>
                                            ))}
                                        </select>
                                    </div>

                                    <div className="font-setting">
                                        <label className="setting-label">Шрифт заголовков</label>
                                        <select
                                            name="headingFontFamily"
                                            value={theme.headingFontFamily}
                                            onChange={handleChange}
                                            className="font-select"
                                        >
                                            <option value="">Использовать основной шрифт</option>
                                            {fontOptions.map(font => (
                                                <option key={font} value={font} style={{fontFamily: font}}>
                                                    {font.split(',')[0]}
                                                </option>
                                            ))}
                                        </select>
                                    </div>

                                    <div className="font-setting">
                                        <label className="setting-label">Шрифт основного текста</label>
                                        <select
                                            name="bodyFontFamily"
                                            value={theme.bodyFontFamily}
                                            onChange={handleChange}
                                            className="font-select"
                                        >
                                            <option value="">Использовать основной шрифт</option>
                                            {fontOptions.map(font => (
                                                <option key={font} value={font} style={{fontFamily: font}}>
                                                    {font.split(',')[0]}
                                                </option>
                                            ))}
                                        </select>
                                    </div>
                                </div>
                            </div>
                        )}

                        {activeTab === 'images' && (
                            <div className="settings-section">
                                <h3 className="settings-subsection-title">Изображения</h3>
                                <div className="images-settings">
                                    <ImageUpload
                                        type="logoUrl"
                                        url={theme.logoUrl}
                                        label="Логотип"
                                        inputRef={logoInputRef}
                                    />
                                    <ImageUpload
                                        type="headerImageUrl"
                                        url={theme.headerImageUrl}
                                        label="Изображение заголовка"
                                        inputRef={headerImageInputRef}
                                    />
                                    <ImageUpload
                                        type="footerLogoUrl"
                                        url={theme.footerLogoUrl}
                                        label="Логотип для футера"
                                        inputRef={footerLogoInputRef}
                                    />
                                    <ImageUpload
                                        type="faviconUrl"
                                        url={theme.faviconUrl}
                                        label="Иконка сайта (Favicon)"
                                        inputRef={faviconInputRef}
                                    />
                                </div>
                            </div>
                        )}

                        {activeTab === 'layout' && (
                            <div className="settings-section">
                                <h3 className="settings-subsection-title">Скругления элементов</h3>
                                <div className="layout-settings">
                                    <div className="setting-group">
                                        <label className="setting-label">Скругление кнопок</label>
                                        <select
                                            name="buttonRadius"
                                            value={theme.buttonRadius}
                                            onChange={handleChange}
                                            className="radius-select"
                                        >
                                            {borderRadiusOptions.map(radius => (
                                                <option key={radius} value={radius}>{radius}</option>
                                            ))}
                                        </select>
                                    </div>

                                    <div className="setting-group">
                                        <label className="setting-label">Скругление карточек</label>
                                        <select
                                            name="cardRadius"
                                            value={theme.cardRadius}
                                            onChange={handleChange}
                                            className="radius-select"
                                        >
                                            {borderRadiusOptions.map(radius => (
                                                <option key={radius} value={radius}>{radius}</option>
                                            ))}
                                        </select>
                                    </div>

                                    <div className="setting-group">
                                        <label className="setting-label">Скругление полей ввода</label>
                                        <select
                                            name="inputRadius"
                                            value={theme.inputRadius}
                                            onChange={handleChange}
                                            className="radius-select"
                                        >
                                            {borderRadiusOptions.map(radius => (
                                                <option key={radius} value={radius}>{radius}</option>
                                            ))}
                                        </select>
                                    </div>
                                </div>

                                <h3 className="settings-subsection-title">Тени элементов</h3>
                                <div className="layout-settings">
                                    <div className="setting-group">
                                        <label className="setting-label">Тень элементов</label>
                                        <select
                                            name="boxShadow"
                                            value={theme.boxShadow}
                                            onChange={handleChange}
                                            className="shadow-select"
                                        >
                                            {shadowOptions.map((shadow, index) => (
                                                <option key={index} value={shadow}>
                                                    {shadow === '' ? 'По умолчанию' :
                                                        shadow === 'none' ? 'Без тени' :
                                                            `Тень ${index}`}
                                                </option>
                                            ))}
                                        </select>
                                    </div>

                                    <div className="setting-group">
                                        <label className="setting-label">Тень карточек</label>
                                        <select
                                            name="cardShadow"
                                            value={theme.cardShadow}
                                            onChange={handleChange}
                                            className="shadow-select"
                                        >
                                            {shadowOptions.map((shadow, index) => (
                                                <option key={index} value={shadow}>
                                                    {shadow === '' ? 'По умолчанию' :
                                                        shadow === 'none' ? 'Без тени' :
                                                            `Тень ${index}`}
                                                </option>
                                            ))}
                                        </select>
                                    </div>
                                </div>
                            </div>
                        )}

                        {activeTab === 'store' && (
                            <div className="settings-section">
                                <h3 className="settings-subsection-title">Основная информация</h3>
                                <div className="store-settings">
                                    <div className="setting-group">
                                        <label className="setting-label">Название магазина</label>
                                        <input
                                            type="text"
                                            name="name"
                                            value={storeInfo.name}
                                            onChange={handleStoreChange}
                                            className="form-input"
                                            placeholder="Введите название магазина"
                                        />
                                    </div>

                                    <div className="setting-group">
                                        <label className="setting-label">Поддомен</label>
                                        <input
                                            type="text"
                                            value={storeInfo.subdomain}
                                            className="form-input"
                                            disabled
                                            style={{backgroundColor: 'var(--background-hover)', cursor: 'not-allowed'}}
                                        />
                                        <small className="form-hint">Поддомен нельзя изменить после создания
                                            магазина</small>
                                    </div>
                                </div>

                                <h3 className="settings-subsection-title">Контактная информация</h3>
                                <div className="store-settings">
                                    <div className="setting-group">
                                        <label className="setting-label">Телефон</label>
                                        <input
                                            type="tel"
                                            name="contactPhone"
                                            value={storeInfo.contactPhone}
                                            onChange={handleStoreChange}
                                            className="form-input"
                                            placeholder="+7 (999) 999-99-99"
                                        />
                                    </div>

                                    <div className="setting-group">
                                        <label className="setting-label">Email</label>
                                        <input
                                            type="email"
                                            name="contactEmail"
                                            value={storeInfo.contactEmail}
                                            onChange={handleStoreChange}
                                            className="form-input"
                                            placeholder="shop@example.com"
                                        />
                                    </div>
                                </div>

                                <h3 className="settings-subsection-title">Интеграции</h3>

                                <div className="integration-card">
                                    <div className="integration-header">
                                        <h4 className="integration-title">Ozon</h4>
                                        <span className="integration-status">
            {storeInfo.integrations.ozonApiKey && storeInfo.integrations.ozonClientId ? '✓ Подключено' : '○ Не подключено'}
        </span>
                                    </div>
                                    <div className="setting-group">
                                        <label className="setting-label">Client ID Ozon</label>
                                        <input
                                            type="text"
                                            name="ozonClientId"
                                            value={storeInfo.integrations.ozonClientId}
                                            onChange={handleIntegrationChange}
                                            className="form-input"
                                            placeholder="Введите Client ID"
                                        />
                                    </div>
                                    <div className="setting-group">
                                        <label className="setting-label">API ключ Ozon</label>
                                        <input
                                            type="password"
                                            name="ozonApiKey"
                                            value={storeInfo.integrations.ozonApiKey}
                                            onChange={handleIntegrationChange}
                                            className="form-input"
                                            placeholder="Введите API ключ"
                                        />
                                    </div>
                                </div>


                                <div className="integration-card">
                                    <div className="integration-header">
                                        <h4 className="integration-title">Wildberries</h4>
                                        <span className="integration-status">
                                            {storeInfo.integrations.wildberriesApiKey ? '✓ Подключено' : '○ Не подключено'}
                                        </span>
                                    </div>
                                    <div className="setting-group">
                                        <label className="setting-label">API ключ Wildberries</label>
                                        <input
                                            type="password"
                                            name="wildberriesApiKey"
                                            value={storeInfo.integrations.wildberriesApiKey}
                                            onChange={handleIntegrationChange}
                                            className="form-input"
                                            placeholder="Введите API ключ"
                                        />
                                    </div>
                                </div>

                                <div className="integration-card">
                                    <div className="integration-header">
                                        <h4 className="integration-title">ЮKassa</h4>
                                        <span className="integration-status">
                                            {storeInfo.integrations.yookassaIdempotencyKey && storeInfo.integrations.yookassaSecretKey
                                                ? '✓ Подключено'
                                                : '○ Не подключено'}
                                        </span>
                                    </div>
                                    <div className="setting-group">
                                        <label className="setting-label">Ключ идемпотентности</label>
                                        <input
                                            type="password"
                                            name="yookassaIdempotencyKey"
                                            value={storeInfo.integrations.yookassaIdempotencyKey}
                                            onChange={handleIntegrationChange}
                                            className="form-input"
                                            placeholder="Введите ключ идемпотентности"
                                        />
                                    </div>
                                    <div className="setting-group">
                                        <label className="setting-label">Секретный ключ</label>
                                        <input
                                            type="password"
                                            name="yookassaSecretKey"
                                            value={storeInfo.integrations.yookassaSecretKey}
                                            onChange={handleIntegrationChange}
                                            className="form-input"
                                            placeholder="Введите секретный ключ"
                                        />
                                    </div>
                                </div>
                            </div>
                        )}

                        {activeTab === 'preview' && (
                            <div className="settings-section">
                                <h3 className="settings-subsection-title">Предпросмотр темы</h3>
                                <div className="preview-container" style={{
                                    backgroundColor: theme.backgroundColor,
                                    color: theme.textColor,
                                    fontFamily: theme.fontFamily,
                                    borderRadius: theme.cardRadius,
                                    boxShadow: theme.cardShadow
                                }}>
                                    <div className="preview-header" style={{
                                        backgroundColor: theme.primaryColor,
                                        padding: '1rem',
                                        borderTopLeftRadius: theme.cardRadius,
                                        borderTopRightRadius: theme.cardRadius
                                    }}>
                                        {theme.logoUrl && <img src={theme.logoUrl} alt="Logo" className="preview-logo"/>}
                                        <div className="preview-title" style={{
                                            color: theme.buttonTextColor,
                                            fontFamily: theme.headingFontFamily || theme.fontFamily
                                        }}>
                                            {storeInfo.name || 'Название магазина'}
                                        </div>
                                    </div>

                                    <div className="preview-content" style={{padding: '1rem'}}>
                                        <div className="preview-card" style={{
                                            backgroundColor: theme.backgroundColor,
                                            border: `1px solid ${theme.primaryColor}20`,
                                            borderRadius: theme.cardRadius,
                                            padding: '1rem',
                                            marginBottom: '1rem',
                                            boxShadow: theme.cardShadow
                                        }}>
                                            <h4 className="preview-card-title" style={{
                                                fontFamily: theme.headingFontFamily || theme.fontFamily,
                                                color: theme.textColor
                                            }}>
                                                Пример карточки товара
                                            </h4>

                                            <div className="preview-button" style={{
                                                backgroundColor: theme.secondaryColor,
                                                color: theme.buttonTextColor,
                                                borderRadius: theme.buttonRadius,
                                                boxShadow: theme.boxShadow,
                                                padding: '0.5rem 1rem',
                                                border: 'none',
                                                cursor: 'pointer',
                                                marginBottom: '0.5rem',
                                                display: 'inline-block'
                                            }}>
                                                Добавить в корзину
                                            </div>

                                            <div className="preview-accent" style={{color: theme.accentColor, marginBottom: '0.5rem'}}>
                                                Акцентный текст
                                            </div>

                                            <p style={{
                                                fontFamily: theme.bodyFontFamily || theme.fontFamily,
                                                color: theme.textColor
                                            }}>
                                                Пример обычного текста в теме магазина
                                            </p>

                                            <div className="status-examples" style={{display: 'flex', gap: '0.5rem', flexWrap: 'wrap'}}>
                                                <span style={{color: theme.successColor, fontSize: '0.9rem'}}>✓ Успех</span>
                                                <span style={{color: theme.errorColor, fontSize: '0.9rem'}}>✗ Ошибка</span>
                                                <span style={{color: theme.warningColor, fontSize: '0.9rem'}}>⚠ Предупреждение</span>
                                                <span style={{color: theme.infoColor, fontSize: '0.9rem'}}>ℹ Информация</span>
                                            </div>
                                        </div>

                                        <div className="preview-footer" style={{
                                            backgroundColor: theme.footerBackgroundColor,
                                            color: theme.footerTextColor,
                                            padding: '1rem',
                                            borderRadius: theme.cardRadius,
                                            textAlign: 'center'
                                        }}>
                                            {theme.footerLogoUrl &&
                                                <img src={theme.footerLogoUrl} alt="Footer Logo"
                                                     style={{height: '30px', marginBottom: '0.5rem'}} />
                                            }
                                            <div>Пример футера сайта</div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        )}
                    </div>

                    <div className="settings-actions">
                        <button
                            className="save-button"
                            onClick={handleSave}
                            disabled={saving}
                        >
                            {saving ? (
                                <>
                                    <span className="spinner-icon">⟳</span>
                                    Сохранение...
                                </>
                            ) : 'Сохранить настройки'}
                        </button>
                    </div>
                </div>
            </div>

            {/* Модальное окно для выбора цвета */}
            {activeColorPicker && (
                <div className="color-picker-modal">
                    <div className="modal-backdrop" onClick={() => setActiveColorPicker(null)}></div>
                    <div className="modal-content">
                        <h3 className="modal-title">Выбор цвета</h3>
                        <div className="color-picker-wrapper">
                            <input
                                type="color"
                                value={theme[activeColorPicker]}
                                onChange={(e) => handleColorChange({hex: e.target.value}, activeColorPicker)}
                                className="color-picker-input"
                            />
                        </div>
                        <div className="modal-actions">
                            <button
                                className="modal-button"
                                onClick={() => setActiveColorPicker(null)}
                            >
                                Закрыть
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default SettingsPage;