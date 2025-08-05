package diploma.ecommerce.backend.shopbase.service;

import diploma.ecommerce.backend.shopbase.model.Theme;

public interface ThemeService {
    Theme getThemeById(Long id);

    Theme createTheme(Theme theme);

    Theme updateTheme(Long id, Theme themeDetails);

    Theme getDefaultTheme();
}