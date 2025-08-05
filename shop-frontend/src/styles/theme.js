// Базовая тема по умолчанию (fallback)
export const defaultTheme = {
    colors: {
        primary: '#3498db',
        secondary: '#2ecc71',
        accent: '#e74c3c',
        text: '#333333',
        background: '#ffffff',
        buttonText: '#ffffff',
        footer: {
            background: '#2c3e50',
            text: '#ecf0f1'
        },
        status: {
            success: '#27ae60',
            error: '#c0392b',
            warning: '#f39c12',
            info: '#3498db'
        },
        interactive: {
            hover: '#34495e',
            active: '#2980b9'
        }
    },
    typography: {
        fontFamily: 'Roboto, sans-serif',
        headingFontFamily: 'Montserrat, sans-serif',
        bodyFontFamily: 'Open Sans, sans-serif',
        fontSize: {
            tiny: '0.75rem',
            small: '0.875rem',
            base: '1rem',
            large: '1.25rem',
            heading: '1.5rem',
            display: '2rem'
        },
        fontWeight: {
            light: 300,
            normal: 400,
            medium: 500,
            bold: 700
        }
    },
    spacing: {
        xs: '0.25rem',
        sm: '0.5rem',
        md: '1rem',
        lg: '1.5rem',
        xl: '2rem',
        xxl: '3rem'
    },
    borderRadius: {
        small: '2px',
        button: '4px',
        card: '8px',
        input: '4px',
        image: '8px'
    },
    shadows: {
        small: '0 1px 2px rgba(0,0,0,0.1)',
        card: '0 2px 4px rgba(0,0,0,0.1)',
        medium: '0 4px 6px rgba(0,0,0,0.1)',
        large: '0 4px 8px rgba(0,0,0,0.1)'
    },
    assets: {
        logoUrl: null,
        headerImageUrl: null,
        footerLogoUrl: null,
        faviconUrl: null
    }
};

// Функция для создания темы из API ответа
export const createThemeFromApi = (apiTheme) => {
    if (!apiTheme) return defaultTheme;

    return {
        colors: {
            primary: apiTheme.primaryColor || defaultTheme.colors.primary,
            secondary: apiTheme.secondaryColor || defaultTheme.colors.secondary,
            accent: apiTheme.accentColor || defaultTheme.colors.accent,
            text: apiTheme.textColor || defaultTheme.colors.text,
            background: apiTheme.backgroundColor || defaultTheme.colors.background,
            buttonText: apiTheme.buttonTextColor || defaultTheme.colors.buttonText,
            footer: {
                background: apiTheme.footerBackgroundColor || defaultTheme.colors.footer.background,
                text: apiTheme.footerTextColor || defaultTheme.colors.footer.text
            },
            status: {
                success: apiTheme.successColor || defaultTheme.colors.status.success,
                error: apiTheme.errorColor || defaultTheme.colors.status.error,
                warning: apiTheme.warningColor || defaultTheme.colors.status.warning,
                info: apiTheme.infoColor || defaultTheme.colors.status.info
            },
            interactive: {
                hover: apiTheme.hoverColor || defaultTheme.colors.interactive.hover,
                active: apiTheme.activeColor || defaultTheme.colors.interactive.active
            }
        },
        typography: {
            fontFamily: apiTheme.fontFamily || defaultTheme.typography.fontFamily,
            headingFontFamily: apiTheme.headingFontFamily || defaultTheme.typography.headingFontFamily,
            bodyFontFamily: apiTheme.bodyFontFamily || defaultTheme.typography.bodyFontFamily,
            fontSize: defaultTheme.typography.fontSize,
            fontWeight: defaultTheme.typography.fontWeight
        },
        spacing: defaultTheme.spacing,
        borderRadius: {
            small: defaultTheme.borderRadius.small,
            button: apiTheme.buttonRadius || defaultTheme.borderRadius.button,
            card: apiTheme.cardRadius || defaultTheme.borderRadius.card,
            input: apiTheme.inputRadius || defaultTheme.borderRadius.input,
            image: defaultTheme.borderRadius.image
        },
        shadows: {
            small: defaultTheme.shadows.small,
            card: apiTheme.cardShadow || defaultTheme.shadows.card,
            medium: apiTheme.boxShadow || defaultTheme.shadows.medium,
            large: defaultTheme.shadows.large
        },
        assets: {
            logoUrl: apiTheme.logoUrl,
            headerImageUrl: apiTheme.headerImageUrl,
            footerLogoUrl: apiTheme.footerLogoUrl,
            faviconUrl: apiTheme.faviconUrl
        }
    };
};

export default defaultTheme;