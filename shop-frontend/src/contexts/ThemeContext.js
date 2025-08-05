import React, { createContext, useContext, useState, useEffect } from 'react';
import { ThemeProvider as StyledThemeProvider } from 'styled-components';
import { themeApi } from '../services/api';
import { createThemeFromApi, defaultTheme } from '../styles/theme';
import LoadingScreen from '../components/LoadingScreen/LoadingScreen';

const ThemeContext = createContext();

export const useTheme = () => {
    const context = useContext(ThemeContext);
    if (!context) {
        throw new Error('useTheme must be used within a ThemeProvider');
    }
    return context;
};

export const ThemeProvider = ({ children }) => {
    const [theme, setTheme] = useState(defaultTheme);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        loadTheme();
    }, []);

    useEffect(() => {
        if (theme.assets.faviconUrl) {
            updateFavicon(theme.assets.faviconUrl);
        }
    }, [theme.assets.faviconUrl]);

    const loadTheme = async () => {
        try {
            setLoading(true);
            setError(null);

            const apiTheme = await themeApi.getPublicTheme();
            const newTheme = createThemeFromApi(apiTheme);
            setTheme(newTheme);

            console.log('Тема успешно загружена:', apiTheme);
        } catch (err) {
            console.error('Ошибка при загрузке темы:', err);
            setError(err.message);
            setTheme(defaultTheme);
        } finally {
            setLoading(false);
        }
    };

    const updateFavicon = (faviconUrl) => {
        try {
            const existingLinks = document.querySelectorAll('link[rel*="icon"]');
            existingLinks.forEach(link => link.remove());

            const link = document.createElement('link');
            link.rel = 'icon';
            link.type = 'image/x-icon';
            link.href = faviconUrl;
            document.head.appendChild(link);
        } catch (err) {
            console.error('Ошибка при обновлении favicon:', err);
        }
    };

    const reloadTheme = () => {
        loadTheme();
    };

    const value = {
        theme,
        loading,
        error,
        reloadTheme,
        loadTheme
    };

    if (loading) {
        return (
            <StyledThemeProvider theme={defaultTheme}>
                <LoadingScreen
                    text="Загрузка темы..."
                    subtext="Настраиваем внешний вид магазина"
                />
            </StyledThemeProvider>
        );
    }

    return (
        <ThemeContext.Provider value={value}>
            <StyledThemeProvider theme={theme}>
                {children}
            </StyledThemeProvider>
        </ThemeContext.Provider>
    );
};

export default ThemeProvider;