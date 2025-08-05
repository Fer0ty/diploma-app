import React, { useState, useRef } from 'react';
import { useToast } from '../../context/ToastContext';
import photoAPI from '../../api/photoAPI';
import './ProductPhotosManager.css';

const ProductPhotosManager = ({ productId, photos, onPhotosChanged }) => {
    const { showSuccess, showError, showInfo } = useToast();
    const [isUploading, setIsUploading] = useState(false);
    const [dragging, setDragging] = useState(false);
    const fileInputRef = useRef(null);

    const handleFileSelected = async (e) => {
        const files = Array.from(e.target.files);
        if (files.length === 0) return;

        await uploadFiles(files);
    };

    const handleDragOver = (e) => {
        e.preventDefault();
        setDragging(true);
    };

    const handleDragLeave = () => {
        setDragging(false);
    };

    const handleDrop = async (e) => {
        e.preventDefault();
        setDragging(false);

        const files = Array.from(e.dataTransfer.files);
        if (files.length === 0) return;

        await uploadFiles(files);
    };

    const uploadFiles = async (files) => {
        setIsUploading(true);
        showInfo(`Загрузка ${files.length} файлов...`);

        try {
            for (const file of files) {
                if (!file.type.startsWith('image/')) {
                    showError(`Файл ${file.name} не является изображением`);
                    continue;
                }

                const uploadResponse = await photoAPI.uploadFile(file, 'products');

                if (uploadResponse.success && uploadResponse.fileUrl) {
                    await photoAPI.addPhotoToProduct(productId, {
                        filePath: uploadResponse.fileUrl,
                        displayOrder: photos.length + 1,
                        main: photos.length === 0
                    });
                    const updatedPhotos = await photoAPI.getProductPhotos(productId);
                    onPhotosChanged(updatedPhotos);
                    showSuccess(`Фото "${file.name}" успешно добавлено`);
                } else {
                    showError(`Ошибка при загрузке файла ${file.name}`);
                }
            }
        } catch (error) {
            console.error('Error uploading files:', error);
            showError('Ошибка при загрузке файлов');
        } finally {
            setIsUploading(false);
            if (fileInputRef.current) {
                fileInputRef.current.value = '';
            }
        }
    };

    const handleDeletePhoto = async (photoId) => {
        if (!window.confirm('Вы уверены, что хотите удалить это фото?')) {
            return;
        }

        try {
            showInfo('Удаление фотографии...');
            await photoAPI.deleteProductPhoto(productId, photoId);

            const updatedPhotos = await photoAPI.getProductPhotos(productId);
            onPhotosChanged(updatedPhotos);
            showSuccess('Фотография успешно удалена');
        } catch (error) {
            console.error('Error deleting photo:', error);
            showError('Ошибка при удалении фотографии');
        }
    };

    const handleSetMainPhoto = async (photoId) => {
        try {
            showInfo('Установка главной фотографии...');
            await photoAPI.setMainPhoto(productId, photoId);

            const updatedPhotos = await photoAPI.getProductPhotos(productId);
            onPhotosChanged(updatedPhotos);
            showSuccess('Главная фотография установлена');
        } catch (error) {
            console.error('Error setting main photo:', error);
            showError('Ошибка при установке главной фотографии');
        }
    };

    return (
        <div className="photos-manager">
            <h3 className="photos-manager-title">Фотографии товара ({photos.length})</h3>

            {/* Drag & Drop зона */}
            <div
                className={`photo-dropzone ${dragging ? 'photo-dropzone-active' : ''}`}
                onDragOver={handleDragOver}
                onDragLeave={handleDragLeave}
                onDrop={handleDrop}
                onClick={() => fileInputRef.current.click()}
            >
                <input
                    type="file"
                    ref={fileInputRef}
                    className="photo-input"
                    accept="image/*"
                    multiple
                    onChange={handleFileSelected}
                />

                {isUploading ? (
                    <div className="upload-spinner"></div>
                ) : (
                    <>
                        <div className="dropzone-icon">📁</div>
                        <p className="dropzone-text">
                            Перетащите фотографии сюда или <span className="dropzone-browse">выберите файлы</span>
                        </p>
                        <p className="dropzone-hint">Поддерживаются форматы JPG, PNG, GIF и WebP</p>
                    </>
                )}
            </div>

            {/* Превью загруженных фото */}
            {photos.length > 0 && (
                <div className="photos-grid">
                    {photos.map(photo => (
                        <div
                            key={photo.id}
                            className={`photo-item ${photo.main ? 'photo-main' : ''}`}
                        >
                            <img
                                src={photo.filePath}
                                alt="Product"
                                className="photo-thumbnail"
                            />
                            <div className="photo-actions">
                                <button
                                    className={`photo-action-btn ${photo.main ? 'photo-main-active' : 'photo-main-btn'}`}
                                    onClick={() => handleSetMainPhoto(photo.id)}
                                    disabled={photo.main}
                                    title={photo.main ? 'Это главное фото' : 'Сделать главным фото'}
                                >
                                    ⭐
                                </button>
                                <button
                                    className="photo-action-btn photo-delete-btn"
                                    onClick={() => handleDeletePhoto(photo.id)}
                                    title="Удалить фото"
                                >
                                    🗑️
                                </button>
                            </div>
                            {photo.main && <div className="main-photo-badge">Главное</div>}
                        </div>
                    ))}
                </div>
            )}

            {photos.length === 0 && !isUploading && (
                <div className="no-photos">
                    <p>У товара пока нет фотографий</p>
                </div>
            )}
        </div>
    );
};

export default ProductPhotosManager;