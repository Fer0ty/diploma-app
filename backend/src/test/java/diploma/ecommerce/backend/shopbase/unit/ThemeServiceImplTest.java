package diploma.ecommerce.backend.shopbase.unit;

import diploma.ecommerce.backend.shopbase.exception.ResourceNotFoundException;
import diploma.ecommerce.backend.shopbase.model.Theme;
import diploma.ecommerce.backend.shopbase.repository.ThemeRepository;
import diploma.ecommerce.backend.shopbase.service.impl.ThemeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ThemeServiceImplTest {

    private static final Long THEME_ID = 1L;

    @Mock
    private ThemeRepository themeRepository;

    @InjectMocks
    private ThemeServiceImpl themeService;

    @Captor
    private ArgumentCaptor<Theme> themeCaptor;

    private Theme theme;
    private Theme themeDetails;

    @BeforeEach
    void setUp() {
        theme = new Theme();
        theme.setId(THEME_ID);
        theme.setPrimaryColor("#3498db");
        theme.setSecondaryColor("#2ecc71");
        theme.setAccentColor("#e74c3c");
        theme.setTextColor("#333333");
        theme.setBackgroundColor("#ffffff");
        theme.setFontFamily("Roboto, sans-serif");
        theme.setLogoUrl("https://example.com/logo.png");
        theme.setHeaderImageUrl("https://example.com/header.jpg");

        themeDetails = new Theme();
        themeDetails.setPrimaryColor("#2980b9");
        themeDetails.setSecondaryColor("#27ae60");
        themeDetails.setButtonRadius("8px");
        themeDetails.setCardRadius("12px");
        themeDetails.setSuccessColor("#2ecc71");
        themeDetails.setErrorColor("#e74c3c");
    }

    @Nested
    @DisplayName("getThemeById Tests")
    class GetThemeByIdTests {

        @Test
        @DisplayName("Should return theme when theme exists")
        void getThemeById_ThemeExists_ReturnsTheme() {
            when(themeRepository.findById(THEME_ID)).thenReturn(Optional.of(theme));

            Theme result = themeService.getThemeById(THEME_ID);

            assertNotNull(result);
            assertEquals(THEME_ID, result.getId());
            assertEquals("#3498db", result.getPrimaryColor());
            verify(themeRepository).findById(THEME_ID);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when theme does not exist")
        void getThemeById_ThemeDoesNotExist_ThrowsResourceNotFoundException() {
            when(themeRepository.findById(THEME_ID)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> themeService.getThemeById(THEME_ID));
            verify(themeRepository).findById(THEME_ID);
        }
    }

    @Nested
    @DisplayName("createTheme Tests")
    class CreateThemeTests {

        @Test
        @DisplayName("Should create and return theme")
        void createTheme_ValidTheme_ReturnsCreatedTheme() {
            when(themeRepository.save(any(Theme.class))).thenAnswer(invocation -> {
                Theme savedTheme = invocation.getArgument(0);
                savedTheme.setId(THEME_ID);
                return savedTheme;
            });

            Theme result = themeService.createTheme(themeDetails);

            assertNotNull(result);
            assertEquals(THEME_ID, result.getId());
            assertEquals("#2980b9", result.getPrimaryColor());
            assertEquals("#27ae60", result.getSecondaryColor());

            verify(themeRepository).save(themeCaptor.capture());
            Theme savedTheme = themeCaptor.getValue();
            assertEquals("#2980b9", savedTheme.getPrimaryColor());
            assertEquals("8px", savedTheme.getButtonRadius());
        }
    }

    @Nested
    @DisplayName("updateTheme Tests")
    class UpdateThemeTests {

        @Test
        @DisplayName("Should update and return theme when theme exists")
        void updateTheme_ThemeExists_ReturnsUpdatedTheme() {
            when(themeRepository.findById(THEME_ID)).thenReturn(Optional.of(theme));
            when(themeRepository.save(any(Theme.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Theme result = themeService.updateTheme(THEME_ID, themeDetails);

            assertNotNull(result);
            assertEquals(THEME_ID, result.getId());
            assertEquals("#2980b9", result.getPrimaryColor());
            assertEquals("#27ae60", result.getSecondaryColor());
            assertEquals("8px", result.getButtonRadius());
            assertEquals("12px", result.getCardRadius());
            assertEquals("#2ecc71", result.getSuccessColor());
            assertEquals("#e74c3c", result.getErrorColor());

            // Original values that weren't overwritten should be preserved
            assertEquals("Roboto, sans-serif", result.getFontFamily());
            assertEquals("https://example.com/logo.png", result.getLogoUrl());

            verify(themeRepository).findById(THEME_ID);
            verify(themeRepository).save(themeCaptor.capture());
            Theme savedTheme = themeCaptor.getValue();
            assertEquals("#2980b9", savedTheme.getPrimaryColor());
            assertEquals(THEME_ID, savedTheme.getId());
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when theme does not exist")
        void updateTheme_ThemeDoesNotExist_ThrowsResourceNotFoundException() {
            when(themeRepository.findById(THEME_ID)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> themeService.updateTheme(THEME_ID, themeDetails));

            verify(themeRepository).findById(THEME_ID);
            verify(themeRepository, never()).save(any(Theme.class));
        }
    }

    @Nested
    @DisplayName("getDefaultTheme Tests")
    class GetDefaultThemeTests {

        @Test
        @DisplayName("Should return default theme with all default values")
        void getDefaultTheme_ReturnsDefaultTheme() {
            Theme result = themeService.getDefaultTheme();

            assertNotNull(result);
            assertEquals("#3498db", result.getPrimaryColor());
            assertEquals("#2ecc71", result.getSecondaryColor());
            assertEquals("#e74c3c", result.getAccentColor());
            assertEquals("#333333", result.getTextColor());
            assertEquals("#ffffff", result.getBackgroundColor());
            assertEquals("Roboto, sans-serif", result.getFontFamily());
            assertEquals("4px", result.getButtonRadius());
            assertEquals("8px", result.getCardRadius());
            assertEquals("#ffffff", result.getButtonTextColor());
            assertEquals("#27ae60", result.getSuccessColor());
            assertEquals("#c0392b", result.getErrorColor());
            assertEquals("#f39c12", result.getWarningColor());
            assertEquals("#3498db", result.getInfoColor());
        }
    }

    @Nested
    @DisplayName("updateTheme Comprehensive Field Tests")
    class UpdateThemeComprehensiveTests {

        @Test
        @DisplayName("Should update all font family fields")
        void updateTheme_FontFamilyFields_UpdatesCorrectly() {
            themeDetails.setHeadingFontFamily("Arial, sans-serif");
            themeDetails.setBodyFontFamily("Georgia, serif");

            when(themeRepository.findById(THEME_ID)).thenReturn(Optional.of(theme));
            when(themeRepository.save(any(Theme.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Theme result = themeService.updateTheme(THEME_ID, themeDetails);

            assertEquals("Arial, sans-serif", result.getHeadingFontFamily());
            assertEquals("Georgia, serif", result.getBodyFontFamily());

            verify(themeRepository).save(themeCaptor.capture());
            Theme savedTheme = themeCaptor.getValue();
            assertEquals("Arial, sans-serif", savedTheme.getHeadingFontFamily());
            assertEquals("Georgia, serif", savedTheme.getBodyFontFamily());
        }

        @Test
        @DisplayName("Should update image URL fields")
        void updateTheme_ImageUrlFields_UpdatesCorrectly() {
            themeDetails.setFooterLogoUrl("https://example.com/footer-logo.png");
            themeDetails.setFaviconUrl("https://example.com/favicon.ico");

            when(themeRepository.findById(THEME_ID)).thenReturn(Optional.of(theme));
            when(themeRepository.save(any(Theme.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Theme result = themeService.updateTheme(THEME_ID, themeDetails);

            assertEquals("https://example.com/footer-logo.png", result.getFooterLogoUrl());
            assertEquals("https://example.com/favicon.ico", result.getFaviconUrl());
        }

        @Test
        @DisplayName("Should update radius fields")
        void updateTheme_RadiusFields_UpdatesCorrectly() {
            themeDetails.setInputRadius("6px");

            when(themeRepository.findById(THEME_ID)).thenReturn(Optional.of(theme));
            when(themeRepository.save(any(Theme.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Theme result = themeService.updateTheme(THEME_ID, themeDetails);

            assertEquals("6px", result.getInputRadius());
            assertEquals("8px", result.getButtonRadius()); // From themeDetails
            assertEquals("12px", result.getCardRadius()); // From themeDetails
        }

        @Test
        @DisplayName("Should update footer color fields")
        void updateTheme_FooterColorFields_UpdatesCorrectly() {
            themeDetails.setFooterBackgroundColor("#1a1a1a");
            themeDetails.setFooterTextColor("#cccccc");

            when(themeRepository.findById(THEME_ID)).thenReturn(Optional.of(theme));
            when(themeRepository.save(any(Theme.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Theme result = themeService.updateTheme(THEME_ID, themeDetails);

            assertEquals("#1a1a1a", result.getFooterBackgroundColor());
            assertEquals("#cccccc", result.getFooterTextColor());
        }

        @Test
        @DisplayName("Should update additional status color fields")
        void updateTheme_AdditionalColorFields_UpdatesCorrectly() {
            themeDetails.setWarningColor("#ff9800");
            themeDetails.setInfoColor("#2196f3");
            themeDetails.setHoverColor("#1976d2");
            themeDetails.setActiveColor("#0d47a1");

            when(themeRepository.findById(THEME_ID)).thenReturn(Optional.of(theme));
            when(themeRepository.save(any(Theme.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Theme result = themeService.updateTheme(THEME_ID, themeDetails);

            assertEquals("#ff9800", result.getWarningColor());
            assertEquals("#2196f3", result.getInfoColor());
            assertEquals("#1976d2", result.getHoverColor());
            assertEquals("#0d47a1", result.getActiveColor());
        }

        @Test
        @DisplayName("Should update shadow fields")
        void updateTheme_ShadowFields_UpdatesCorrectly() {
            themeDetails.setBoxShadow("0 4px 8px rgba(0,0,0,0.2)");
            themeDetails.setCardShadow("0 2px 4px rgba(0,0,0,0.1)");

            when(themeRepository.findById(THEME_ID)).thenReturn(Optional.of(theme));
            when(themeRepository.save(any(Theme.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Theme result = themeService.updateTheme(THEME_ID, themeDetails);

            assertEquals("0 4px 8px rgba(0,0,0,0.2)", result.getBoxShadow());
            assertEquals("0 2px 4px rgba(0,0,0,0.1)", result.getCardShadow());
        }

        @Test
        @DisplayName("Should not update fields that are null in themeDetails")
        void updateTheme_NullFields_DoesNotUpdateExistingValues() {
            // Set up theme with all fields populated
            theme.setHeadingFontFamily("Original Heading Font");
            theme.setBodyFontFamily("Original Body Font");
            theme.setFooterLogoUrl("https://original.com/footer.png");
            theme.setFaviconUrl("https://original.com/favicon.ico");
            theme.setInputRadius("4px");
            theme.setFooterBackgroundColor("#000000");
            theme.setFooterTextColor("#ffffff");
            theme.setWarningColor("#original");
            theme.setInfoColor("#original");
            theme.setHoverColor("#original");
            theme.setActiveColor("#original");
            theme.setBoxShadow("original shadow");
            theme.setCardShadow("original card shadow");

            // themeDetails has only some fields set (others are null)
            themeDetails.setPrimaryColor("#new-primary");
            // All other fields in themeDetails are null

            when(themeRepository.findById(THEME_ID)).thenReturn(Optional.of(theme));
            when(themeRepository.save(any(Theme.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Theme result = themeService.updateTheme(THEME_ID, themeDetails);

            // Should update only the non-null field
            assertEquals("#new-primary", result.getPrimaryColor());

            // Should preserve original values for null fields
            assertEquals("Original Heading Font", result.getHeadingFontFamily());
            assertEquals("Original Body Font", result.getBodyFontFamily());
            assertEquals("https://original.com/footer.png", result.getFooterLogoUrl());
            assertEquals("https://original.com/favicon.ico", result.getFaviconUrl());
            assertEquals("4px", result.getInputRadius());
            assertEquals("#000000", result.getFooterBackgroundColor());
            assertEquals("#ffffff", result.getFooterTextColor());
            assertEquals("#original", result.getWarningColor());
            assertEquals("#original", result.getInfoColor());
            assertEquals("#original", result.getHoverColor());
            assertEquals("#original", result.getActiveColor());
            assertEquals("original shadow", result.getBoxShadow());
            assertEquals("original card shadow", result.getCardShadow());
        }

        @Test
        @DisplayName("Should update only button text color when provided")
        void updateTheme_ButtonTextColorOnly_UpdatesCorrectly() {
            themeDetails = new Theme(); // Reset to ensure only buttonTextColor is set
            themeDetails.setButtonTextColor("#000000");

            when(themeRepository.findById(THEME_ID)).thenReturn(Optional.of(theme));
            when(themeRepository.save(any(Theme.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Theme result = themeService.updateTheme(THEME_ID, themeDetails);

            assertEquals("#000000", result.getButtonTextColor());

            // Should preserve all other original values
            assertEquals("#3498db", result.getPrimaryColor());
            assertEquals("#2ecc71", result.getSecondaryColor());
            assertEquals("Roboto, sans-serif", result.getFontFamily());
        }
    }

    @Nested
    @DisplayName("getDefaultTheme Comprehensive Tests")
    class GetDefaultThemeComprehensiveTests {

        @Test
        @DisplayName("Should return default theme with all required fields")
        void getDefaultTheme_ReturnsAllRequiredFields() {
            Theme result = themeService.getDefaultTheme();

            assertNotNull(result);

            // Test all color fields
            assertEquals("#3498db", result.getPrimaryColor());
            assertEquals("#2ecc71", result.getSecondaryColor());
            assertEquals("#e74c3c", result.getAccentColor());
            assertEquals("#333333", result.getTextColor());
            assertEquals("#ffffff", result.getBackgroundColor());
            assertEquals("#ffffff", result.getButtonTextColor());
            assertEquals("#2c3e50", result.getFooterBackgroundColor());
            assertEquals("#ecf0f1", result.getFooterTextColor());
            assertEquals("#27ae60", result.getSuccessColor());
            assertEquals("#c0392b", result.getErrorColor());
            assertEquals("#f39c12", result.getWarningColor());
            assertEquals("#3498db", result.getInfoColor());

            // Test typography
            assertEquals("Roboto, sans-serif", result.getFontFamily());

            // Test UI elements
            assertEquals("4px", result.getButtonRadius());
            assertEquals("8px", result.getCardRadius());

            // Verify no ID is set (it's a new theme)
            assertNull(result.getId());
        }

        @Test
        @DisplayName("Should return new instance each time")
        void getDefaultTheme_ReturnsNewInstance() {
            Theme result1 = themeService.getDefaultTheme();
            Theme result2 = themeService.getDefaultTheme();

            assertNotSame(result1, result2);

            // But they should have the same values
            assertEquals(result1.getPrimaryColor(), result2.getPrimaryColor());
            assertEquals(result1.getSecondaryColor(), result2.getSecondaryColor());
        }
    }

    @Nested
    @DisplayName("Edge Cases and Error Handling")
    class ThemeEdgeCasesTests {

        @Test
        @DisplayName("Should handle empty string values in update")
        void updateTheme_EmptyStringValues_UpdatesCorrectly() {
            themeDetails.setPrimaryColor(""); // Empty string
            themeDetails.setSecondaryColor("#new-color"); // Valid color

            when(themeRepository.findById(THEME_ID)).thenReturn(Optional.of(theme));
            when(themeRepository.save(any(Theme.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Theme result = themeService.updateTheme(THEME_ID, themeDetails);

            assertEquals("", result.getPrimaryColor()); // Empty string should be set
            assertEquals("#new-color", result.getSecondaryColor());
        }

        @Test
        @DisplayName("Should handle theme with all null original values")
        void updateTheme_AllNullOriginalValues_UpdatesCorrectly() {
            Theme emptyTheme = new Theme();
            emptyTheme.setId(THEME_ID);
            // All fields are null initially

            when(themeRepository.findById(THEME_ID)).thenReturn(Optional.of(emptyTheme));
            when(themeRepository.save(any(Theme.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Theme result = themeService.updateTheme(THEME_ID, themeDetails);

            assertEquals("#2980b9", result.getPrimaryColor());
            assertEquals("#27ae60", result.getSecondaryColor());
            assertEquals("8px", result.getButtonRadius());
            assertEquals("12px", result.getCardRadius());
            assertEquals("#2ecc71", result.getSuccessColor());
            assertEquals("#e74c3c", result.getErrorColor());

            // Fields not in themeDetails should remain null
            assertNull(result.getFontFamily());
            assertNull(result.getLogoUrl());
        }

        @Test
        @DisplayName("Should handle repository save failure gracefully")
        void updateTheme_SaveFailure_PropagatesException() {
            when(themeRepository.findById(THEME_ID)).thenReturn(Optional.of(theme));
            when(themeRepository.save(any(Theme.class))).thenThrow(new RuntimeException("Database error"));

            assertThrows(RuntimeException.class, () -> themeService.updateTheme(THEME_ID, themeDetails));

            verify(themeRepository).findById(THEME_ID);
            verify(themeRepository).save(any(Theme.class));
        }

        @Test
        @DisplayName("Should handle createTheme with null fields")
        void createTheme_NullFields_SavesCorrectly() {
            Theme themeWithNulls = new Theme();
            themeWithNulls.setPrimaryColor("#test");
            // Other fields are null

            when(themeRepository.save(any(Theme.class))).thenAnswer(invocation -> {
                Theme savedTheme = invocation.getArgument(0);
                savedTheme.setId(THEME_ID);
                return savedTheme;
            });

            Theme result = themeService.createTheme(themeWithNulls);

            assertNotNull(result);
            assertEquals(THEME_ID, result.getId());
            assertEquals("#test", result.getPrimaryColor());
            assertNull(result.getSecondaryColor());

            verify(themeRepository).save(themeCaptor.capture());
            Theme savedTheme = themeCaptor.getValue();
            assertEquals("#test", savedTheme.getPrimaryColor());
            assertNull(savedTheme.getSecondaryColor());
        }

        @Test
        @DisplayName("Should handle createTheme repository failure")
        void createTheme_RepositoryFailure_PropagatesException() {
            when(themeRepository.save(any(Theme.class))).thenThrow(new RuntimeException("Database error"));

            assertThrows(RuntimeException.class, () -> themeService.createTheme(themeDetails));

            verify(themeRepository).save(any(Theme.class));
        }
    }

    @Nested
    @DisplayName("Logging and Method Verification Tests")
    class ThemeLoggingTests {

        @Test
        @DisplayName("getThemeById should call repository exactly once")
        void getThemeById_CallsRepositoryOnce() {
            when(themeRepository.findById(THEME_ID)).thenReturn(Optional.of(theme));

            themeService.getThemeById(THEME_ID);

            verify(themeRepository, times(1)).findById(THEME_ID);
            verifyNoMoreInteractions(themeRepository);
        }

        @Test
        @DisplayName("createTheme should call repository save exactly once")
        void createTheme_CallsRepositorySaveOnce() {
            when(themeRepository.save(any(Theme.class))).thenAnswer(invocation -> {
                Theme savedTheme = invocation.getArgument(0);
                savedTheme.setId(THEME_ID);
                return savedTheme;
            });

            themeService.createTheme(themeDetails);

            verify(themeRepository, times(1)).save(any(Theme.class));
            verifyNoMoreInteractions(themeRepository);
        }

        @Test
        @DisplayName("updateTheme should call repository methods in correct order")
        void updateTheme_CallsRepositoryMethodsInOrder() {
            when(themeRepository.findById(THEME_ID)).thenReturn(Optional.of(theme));
            when(themeRepository.save(any(Theme.class))).thenAnswer(invocation -> invocation.getArgument(0));

            themeService.updateTheme(THEME_ID, themeDetails);

            // Verify the order of calls
            var inOrder = inOrder(themeRepository);
            inOrder.verify(themeRepository).findById(THEME_ID);
            inOrder.verify(themeRepository).save(any(Theme.class));
            verifyNoMoreInteractions(themeRepository);
        }

        @Test
        @DisplayName("getDefaultTheme should not call repository")
        void getDefaultTheme_DoesNotCallRepository() {
            themeService.getDefaultTheme();

            verifyNoInteractions(themeRepository);
        }
    }
}