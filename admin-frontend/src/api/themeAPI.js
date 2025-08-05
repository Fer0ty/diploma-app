import axios from './axios';

const themeAPI = {
    /**
     * Получить текущие настройки темы
     */
    getTheme: async () => {
        try {
            console.log('Fetching theme settings');
            const response = await axios.get('/theme');
            console.log('Theme settings fetched successfully:', response.data);
            return response.data;
        } catch (error) {
            console.error('Error fetching theme settings:', error);
            throw error;
        }
    },

    /**
     * Обновить настройки темы
     */
    updateTheme: async (themeData) => {
        try {
            console.log('Updating theme settings:', themeData);

            const hexRegex = /^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$/;
            const urlRegex = /^https?:\/\/.+/;
            const pixelRegex = /^\d+px$/;
            const shadowRegex = /^[\d\s\w(),.-]+$/;

            const validatedData = {};

            // Базовые цвета
            if (themeData.primaryColor && hexRegex.test(themeData.primaryColor)) {
                validatedData.primaryColor = themeData.primaryColor;
            }
            if (themeData.secondaryColor && hexRegex.test(themeData.secondaryColor)) {
                validatedData.secondaryColor = themeData.secondaryColor;
            }
            if (themeData.accentColor && hexRegex.test(themeData.accentColor)) {
                validatedData.accentColor = themeData.accentColor;
            }
            if (themeData.textColor && hexRegex.test(themeData.textColor)) {
                validatedData.textColor = themeData.textColor;
            }
            if (themeData.backgroundColor && hexRegex.test(themeData.backgroundColor)) {
                validatedData.backgroundColor = themeData.backgroundColor;
            }

            // Цвета элементов
            if (themeData.buttonTextColor && hexRegex.test(themeData.buttonTextColor)) {
                validatedData.buttonTextColor = themeData.buttonTextColor;
            }
            if (themeData.footerBackgroundColor && hexRegex.test(themeData.footerBackgroundColor)) {
                validatedData.footerBackgroundColor = themeData.footerBackgroundColor;
            }
            if (themeData.footerTextColor && hexRegex.test(themeData.footerTextColor)) {
                validatedData.footerTextColor = themeData.footerTextColor;
            }

            // Цвета статусов
            if (themeData.successColor && hexRegex.test(themeData.successColor)) {
                validatedData.successColor = themeData.successColor;
            }
            if (themeData.errorColor && hexRegex.test(themeData.errorColor)) {
                validatedData.errorColor = themeData.errorColor;
            }
            if (themeData.warningColor && hexRegex.test(themeData.warningColor)) {
                validatedData.warningColor = themeData.warningColor;
            }
            if (themeData.infoColor && hexRegex.test(themeData.infoColor)) {
                validatedData.infoColor = themeData.infoColor;
            }

            // Интерактивные цвета
            if (themeData.hoverColor && hexRegex.test(themeData.hoverColor)) {
                validatedData.hoverColor = themeData.hoverColor;
            }
            if (themeData.activeColor && hexRegex.test(themeData.activeColor)) {
                validatedData.activeColor = themeData.activeColor;
            }

            // Шрифты
            if (themeData.fontFamily && themeData.fontFamily.trim()) {
                validatedData.fontFamily = themeData.fontFamily.trim();
            }
            if (themeData.headingFontFamily && themeData.headingFontFamily.trim()) {
                validatedData.headingFontFamily = themeData.headingFontFamily.trim();
            }
            if (themeData.bodyFontFamily && themeData.bodyFontFamily.trim()) {
                validatedData.bodyFontFamily = themeData.bodyFontFamily.trim();
            }

            // URL изображений
            if (themeData.logoUrl && (urlRegex.test(themeData.logoUrl) || themeData.logoUrl === '')) {
                validatedData.logoUrl = themeData.logoUrl;
            }
            if (themeData.headerImageUrl && (urlRegex.test(themeData.headerImageUrl) || themeData.headerImageUrl === '')) {
                validatedData.headerImageUrl = themeData.headerImageUrl;
            }
            if (themeData.footerLogoUrl && (urlRegex.test(themeData.footerLogoUrl) || themeData.footerLogoUrl === '')) {
                validatedData.footerLogoUrl = themeData.footerLogoUrl;
            }
            if (themeData.faviconUrl && (urlRegex.test(themeData.faviconUrl) || themeData.faviconUrl === '')) {
                validatedData.faviconUrl = themeData.faviconUrl;
            }

            // Скругления элементов
            if (themeData.buttonRadius && (pixelRegex.test(themeData.buttonRadius) || themeData.buttonRadius === '')) {
                validatedData.buttonRadius = themeData.buttonRadius;
            }
            if (themeData.cardRadius && (pixelRegex.test(themeData.cardRadius) || themeData.cardRadius === '')) {
                validatedData.cardRadius = themeData.cardRadius;
            }
            if (themeData.inputRadius && (pixelRegex.test(themeData.inputRadius) || themeData.inputRadius === '')) {
                validatedData.inputRadius = themeData.inputRadius;
            }

            // Тени
            if (themeData.boxShadow && (shadowRegex.test(themeData.boxShadow) || themeData.boxShadow === '')) {
                validatedData.boxShadow = themeData.boxShadow;
            }
            if (themeData.cardShadow && (shadowRegex.test(themeData.cardShadow) || themeData.cardShadow === '')) {
                validatedData.cardShadow = themeData.cardShadow;
            }

            console.log('Validated theme data:', validatedData);

            const response = await axios.put('/theme', validatedData);
            console.log('Theme settings updated successfully:', response.data);
            return response.data;
        } catch (error) {
            console.error('Error updating theme settings:', error);
            throw error;
        }
    },

    /**
     * Получить публичные настройки темы (для предпросмотра)
     */
    getPublicTheme: async () => {
        try {
            console.log('Fetching public theme settings');
            const response = await axios.get('/public/theme');
            console.log('Public theme settings fetched successfully:', response.data);
            return response.data;
        } catch (error) {
            console.error('Error fetching public theme settings:', error);
            throw error;
        }
    }
};

export default themeAPI;