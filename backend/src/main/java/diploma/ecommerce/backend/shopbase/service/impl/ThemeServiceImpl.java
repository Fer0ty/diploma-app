package diploma.ecommerce.backend.shopbase.service.impl;

import diploma.ecommerce.backend.shopbase.exception.ResourceNotFoundException;
import diploma.ecommerce.backend.shopbase.model.Theme;
import diploma.ecommerce.backend.shopbase.repository.ThemeRepository;
import diploma.ecommerce.backend.shopbase.service.ThemeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ThemeServiceImpl implements ThemeService {

    private final ThemeRepository themeRepository;

    @Override
    @Transactional(readOnly = true)
    public Theme getThemeById(Long themeId) {
        log.debug("Fetching theme by ID: {}", themeId);
        return themeRepository.findById(themeId)
                .orElseThrow(() -> {
                    log.warn("Theme not found with ID: {}", themeId);
                    return new ResourceNotFoundException("Theme", "id", themeId);
                });
    }

    @Override
    @Transactional
    public Theme createTheme(Theme theme) {
        log.info("Creating a new theme");
        Theme savedTheme = themeRepository.save(theme);
        log.info("Theme created successfully with ID: {}", savedTheme.getId());
        return savedTheme;
    }

    @Override
    @Transactional
    public Theme updateTheme(Long themeId, Theme themeDetails) {
        log.info("Updating theme with ID: {}", themeId);

        Theme existingTheme = getThemeById(themeId);

        // Обновляем все непустые поля из запроса
        if (themeDetails.getPrimaryColor() != null) {
            existingTheme.setPrimaryColor(themeDetails.getPrimaryColor());
        }
        if (themeDetails.getSecondaryColor() != null) {
            existingTheme.setSecondaryColor(themeDetails.getSecondaryColor());
        }
        if (themeDetails.getAccentColor() != null) {
            existingTheme.setAccentColor(themeDetails.getAccentColor());
        }
        if (themeDetails.getTextColor() != null) {
            existingTheme.setTextColor(themeDetails.getTextColor());
        }
        if (themeDetails.getBackgroundColor() != null) {
            existingTheme.setBackgroundColor(themeDetails.getBackgroundColor());
        }
        if (themeDetails.getFontFamily() != null) {
            existingTheme.setFontFamily(themeDetails.getFontFamily());
        }
        if (themeDetails.getHeadingFontFamily() != null) {
            existingTheme.setHeadingFontFamily(themeDetails.getHeadingFontFamily());
        }
        if (themeDetails.getBodyFontFamily() != null) {
            existingTheme.setBodyFontFamily(themeDetails.getBodyFontFamily());
        }
        if (themeDetails.getLogoUrl() != null) {
            existingTheme.setLogoUrl(themeDetails.getLogoUrl());
        }
        if (themeDetails.getHeaderImageUrl() != null) {
            existingTheme.setHeaderImageUrl(themeDetails.getHeaderImageUrl());
        }
        if (themeDetails.getFooterLogoUrl() != null) {
            existingTheme.setFooterLogoUrl(themeDetails.getFooterLogoUrl());
        }
        if (themeDetails.getFaviconUrl() != null) {
            existingTheme.setFaviconUrl(themeDetails.getFaviconUrl());
        }
        if (themeDetails.getButtonRadius() != null) {
            existingTheme.setButtonRadius(themeDetails.getButtonRadius());
        }
        if (themeDetails.getCardRadius() != null) {
            existingTheme.setCardRadius(themeDetails.getCardRadius());
        }
        if (themeDetails.getInputRadius() != null) {
            existingTheme.setInputRadius(themeDetails.getInputRadius());
        }
        if (themeDetails.getButtonTextColor() != null) {
            existingTheme.setButtonTextColor(themeDetails.getButtonTextColor());
        }
        if (themeDetails.getFooterBackgroundColor() != null) {
            existingTheme.setFooterBackgroundColor(themeDetails.getFooterBackgroundColor());
        }
        if (themeDetails.getFooterTextColor() != null) {
            existingTheme.setFooterTextColor(themeDetails.getFooterTextColor());
        }
        if (themeDetails.getSuccessColor() != null) {
            existingTheme.setSuccessColor(themeDetails.getSuccessColor());
        }
        if (themeDetails.getErrorColor() != null) {
            existingTheme.setErrorColor(themeDetails.getErrorColor());
        }
        if (themeDetails.getWarningColor() != null) {
            existingTheme.setWarningColor(themeDetails.getWarningColor());
        }
        if (themeDetails.getInfoColor() != null) {
            existingTheme.setInfoColor(themeDetails.getInfoColor());
        }
        if (themeDetails.getHoverColor() != null) {
            existingTheme.setHoverColor(themeDetails.getHoverColor());
        }
        if (themeDetails.getActiveColor() != null) {
            existingTheme.setActiveColor(themeDetails.getActiveColor());
        }
        if (themeDetails.getBoxShadow() != null) {
            existingTheme.setBoxShadow(themeDetails.getBoxShadow());
        }
        if (themeDetails.getCardShadow() != null) {
            existingTheme.setCardShadow(themeDetails.getCardShadow());
        }

        Theme updatedTheme = themeRepository.save(existingTheme);
        log.info("Theme with ID {} updated successfully", themeId);

        return updatedTheme;
    }

    @Override
    public Theme getDefaultTheme() {
        Theme defaultTheme = new Theme();
        defaultTheme.setPrimaryColor("#3498db");
        defaultTheme.setSecondaryColor("#2ecc71");
        defaultTheme.setAccentColor("#e74c3c");
        defaultTheme.setTextColor("#333333");
        defaultTheme.setBackgroundColor("#ffffff");
        defaultTheme.setFontFamily("Roboto, sans-serif");
        defaultTheme.setButtonRadius("4px");
        defaultTheme.setCardRadius("8px");
        defaultTheme.setButtonTextColor("#ffffff");
        defaultTheme.setFooterBackgroundColor("#2c3e50");
        defaultTheme.setFooterTextColor("#ecf0f1");
        defaultTheme.setSuccessColor("#27ae60");
        defaultTheme.setErrorColor("#c0392b");
        defaultTheme.setWarningColor("#f39c12");
        defaultTheme.setInfoColor("#3498db");

        return defaultTheme;
    }
}