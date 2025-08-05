package diploma.ecommerce.backend.shopbase.integration;

import diploma.ecommerce.backend.shopbase.dto.request.ThemeUpdateRequest;
import diploma.ecommerce.backend.shopbase.dto.response.ThemeResponse;
import diploma.ecommerce.backend.shopbase.model.Theme;
import diploma.ecommerce.backend.shopbase.multitenancy.TenantContext;
import diploma.ecommerce.backend.shopbase.repository.ThemeRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("ThemeController Integration Tests")
public class ThemeControllerIntegrationTest extends BaseIntegrationTest {

    private static final String THEME_API_URL = "/api/v1/theme";
    private static final String PUBLIC_THEME_API_URL = "/api/v1/public/theme";

    @Autowired
    private ThemeRepository themeRepository;

    private Theme themeTenant1;
    private Theme themeTenant2;

    @BeforeEach
    void setUpThemeTestData() {
        TenantContext.clear();

        // Создаем тему для первого тенанта
        themeTenant1 = new Theme();
        themeTenant1.setPrimaryColor("#3498db");
        themeTenant1.setSecondaryColor("#2ecc71");
        themeTenant1.setAccentColor("#e74c3c");
        themeTenant1.setTextColor("#333333");
        themeTenant1.setBackgroundColor("#ffffff");
        themeTenant1.setFontFamily("Roboto, sans-serif");
        themeTenant1.setLogoUrl("https://store1.example.com/logo.png");
        themeTenant1.setButtonRadius("4px");
        themeTenant1.setCardRadius("8px");
        themeTenant1.setSuccessColor("#27ae60");
        themeTenant1.setErrorColor("#c0392b");
        themeTenant1 = themeRepository.saveAndFlush(themeTenant1);

        // Связываем тему с первым тенантом
        tenant1.setTheme(themeTenant1);
        tenantRepository.saveAndFlush(tenant1);

        // Создаем тему для второго тенанта
        themeTenant2 = new Theme();
        themeTenant2.setPrimaryColor("#9b59b6");
        themeTenant2.setSecondaryColor("#f39c12");
        themeTenant2.setAccentColor("#1abc9c");
        themeTenant2.setTextColor("#2c3e50");
        themeTenant2.setBackgroundColor("#ecf0f1");
        themeTenant2.setFontFamily("Open Sans, sans-serif");
        themeTenant2.setLogoUrl("https://store2.example.com/logo.png");
        themeTenant2.setButtonRadius("6px");
        themeTenant2.setCardRadius("10px");
        themeTenant2.setSuccessColor("#2ecc71");
        themeTenant2.setErrorColor("#e74c3c");
        themeTenant2 = themeRepository.saveAndFlush(themeTenant2);

        // Связываем тему со вторым тенантом
        tenant2.setTheme(themeTenant2);
        tenantRepository.saveAndFlush(tenant2);
    }

    @AfterEach
    void clearTenantContextAfterThemeTest() {
        TenantContext.clear();
    }

    @Nested
    @DisplayName("GET /theme Tests")
    class GetCurrentThemeTests {

        @Test
        @DisplayName("GET /theme - Tenant 1 - Should return current theme")
        void getCurrentTheme_forTenant1_shouldReturnCurrentTheme() throws Exception {
            mockMvc.perform(get(THEME_API_URL)
                            .headers(getAuthHeaders(jwtTenant1)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id", is(themeTenant1.getId().intValue())))
                    .andExpect(jsonPath("$.primaryColor", is("#3498db")))
                    .andExpect(jsonPath("$.secondaryColor", is("#2ecc71")))
                    .andExpect(jsonPath("$.accentColor", is("#e74c3c")))
                    .andExpect(jsonPath("$.textColor", is("#333333")))
                    .andExpect(jsonPath("$.backgroundColor", is("#ffffff")))
                    .andExpect(jsonPath("$.fontFamily", is("Roboto, sans-serif")))
                    .andExpect(jsonPath("$.logoUrl", is("https://store1.example.com/logo.png")))
                    .andExpect(jsonPath("$.buttonRadius", is("4px")))
                    .andExpect(jsonPath("$.cardRadius", is("8px")))
                    .andExpect(jsonPath("$.successColor", is("#27ae60")))
                    .andExpect(jsonPath("$.errorColor", is("#c0392b")));
        }

        @Test
        @DisplayName("GET /theme - Tenant 2 - Should return different theme")
        void getCurrentTheme_forTenant2_shouldReturnDifferentTheme() throws Exception {
            mockMvc.perform(get(THEME_API_URL)
                            .headers(getAuthHeaders(jwtTenant2)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(themeTenant2.getId().intValue())))
                    .andExpect(jsonPath("$.primaryColor", is("#9b59b6")))
                    .andExpect(jsonPath("$.secondaryColor", is("#f39c12")))
                    .andExpect(jsonPath("$.fontFamily", is("Open Sans, sans-serif")))
                    .andExpect(jsonPath("$.logoUrl", is("https://store2.example.com/logo.png")))
                    .andExpect(jsonPath("$.buttonRadius", is("6px")));
        }

        @Test
        @DisplayName("GET /theme - Tenant without theme - Should create and return default theme")
        void getCurrentTheme_tenantWithoutTheme_shouldCreateDefaultTheme() throws Exception {
            // Удаляем тему у первого тенанта
            tenant1.setTheme(null);
            tenantRepository.saveAndFlush(tenant1);

            MvcResult result = mockMvc.perform(get(THEME_API_URL)
                            .headers(getAuthHeaders(jwtTenant1)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id", notNullValue()))
                    .andExpect(jsonPath("$.primaryColor", is("#3498db"))) // Default value
                    .andExpect(jsonPath("$.secondaryColor", is("#2ecc71"))) // Default value
                    .andExpect(jsonPath("$.fontFamily", is("Roboto, sans-serif"))) // Default value
                    .andReturn();

            // Проверяем, что тема была создана в базе данных
            String responseContent = result.getResponse().getContentAsString();
            ThemeResponse themeResponse = objectMapper.readValue(responseContent, ThemeResponse.class);

            Optional<Theme> createdThemeOpt = themeRepository.findById(themeResponse.getId());
            assertTrue(createdThemeOpt.isPresent(), "Default theme should be created in database");

            // Проверяем, что тенант теперь связан с новой темой
            tenant1 = tenantRepository.findById(tenant1.getId()).orElseThrow();
            assertNotNull(tenant1.getTheme(), "Tenant should be linked to the new theme");
            assertEquals(themeResponse.getId(), tenant1.getTheme().getId());
        }

        @Test
        @DisplayName("GET /theme - Should require authentication")
        void getCurrentTheme_shouldRequireAuthentication() throws Exception {
            mockMvc.perform(get(THEME_API_URL))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("GET /theme - Should require ADMIN role")
        void getCurrentTheme_shouldRequireAdminRole() throws Exception {
            // Изменяем роль пользователя на не-админа
            userTenant1.setRole("ROLE_USER");
            tenantUserRepository.saveAndFlush(userTenant1);
            String userJwt = jwtUtil.generateToken(userTenant1);

            mockMvc.perform(get(THEME_API_URL)
                            .headers(getAuthHeaders(userJwt)))
                    .andExpect(status().isForbidden());

            // Восстанавливаем роль админа
            userTenant1.setRole("ROLE_ADMIN");
            tenantUserRepository.saveAndFlush(userTenant1);
        }
    }

    @Nested
    @DisplayName("PUT /theme Tests")
    class UpdateCurrentThemeTests {

        @Test
        @DisplayName("PUT /theme - Tenant 1 - Should update existing theme")
        void updateCurrentTheme_forTenant1_shouldUpdateExistingTheme() throws Exception {
            ThemeUpdateRequest updateRequest = new ThemeUpdateRequest();
            updateRequest.setPrimaryColor("#2980b9");
            updateRequest.setSecondaryColor("#27ae60");
            updateRequest.setButtonRadius("8px");
            updateRequest.setCardRadius("12px");
            updateRequest.setSuccessColor("#2ecc71");
            updateRequest.setErrorColor("#e74c3c");
            updateRequest.setLogoUrl("https://store1.example.com/new-logo.png");

            mockMvc.perform(put(THEME_API_URL)
                            .headers(getAuthHeaders(jwtTenant1))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id", is(themeTenant1.getId().intValue())))
                    .andExpect(jsonPath("$.primaryColor", is("#2980b9")))
                    .andExpect(jsonPath("$.secondaryColor", is("#27ae60")))
                    .andExpect(jsonPath("$.buttonRadius", is("8px")))
                    .andExpect(jsonPath("$.cardRadius", is("12px")))
                    .andExpect(jsonPath("$.successColor", is("#2ecc71")))
                    .andExpect(jsonPath("$.errorColor", is("#e74c3c")))
                    .andExpect(jsonPath("$.logoUrl", is("https://store1.example.com/new-logo.png")))
                    // Убеждаемся, что неизмененные поля остались прежними
                    .andExpect(jsonPath("$.textColor", is("#333333")))
                    .andExpect(jsonPath("$.backgroundColor", is("#ffffff")))
                    .andExpect(jsonPath("$.fontFamily", is("Roboto, sans-serif")));

            // Проверяем, что изменения сохранились в базе данных
            Theme updatedTheme = themeRepository.findById(themeTenant1.getId()).orElseThrow();
            assertEquals("#2980b9", updatedTheme.getPrimaryColor());
            assertEquals("#27ae60", updatedTheme.getSecondaryColor());
            assertEquals("8px", updatedTheme.getButtonRadius());
            assertEquals("https://store1.example.com/new-logo.png", updatedTheme.getLogoUrl());
            // Неизмененные поля должны остаться прежними
            assertEquals("#333333", updatedTheme.getTextColor());
            assertEquals("Roboto, sans-serif", updatedTheme.getFontFamily());
        }

        @Test
        @DisplayName("PUT /theme - Tenant without theme - Should create new theme")
        void updateCurrentTheme_tenantWithoutTheme_shouldCreateNewTheme() throws Exception {
            // Удаляем тему у первого тенанта
            tenant1.setTheme(null);
            tenantRepository.saveAndFlush(tenant1);

            ThemeUpdateRequest updateRequest = new ThemeUpdateRequest();
            updateRequest.setPrimaryColor("#8e44ad");
            updateRequest.setSecondaryColor("#d35400");
            updateRequest.setFontFamily("Montserrat, sans-serif");
            updateRequest.setButtonRadius("10px");

            MvcResult result = mockMvc.perform(put(THEME_API_URL)
                            .headers(getAuthHeaders(jwtTenant1))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id", notNullValue()))
                    .andExpect(jsonPath("$.primaryColor", is("#8e44ad")))
                    .andExpect(jsonPath("$.secondaryColor", is("#d35400")))
                    .andExpect(jsonPath("$.fontFamily", is("Montserrat, sans-serif")))
                    .andExpect(jsonPath("$.buttonRadius", is("10px")))
                    .andReturn();

            // Проверяем, что новая тема была создана
            String responseContent = result.getResponse().getContentAsString();
            ThemeResponse themeResponse = objectMapper.readValue(responseContent, ThemeResponse.class);

            Optional<Theme> createdThemeOpt = themeRepository.findById(themeResponse.getId());
            assertTrue(createdThemeOpt.isPresent(), "New theme should be created in database");

            Theme createdTheme = createdThemeOpt.get();
            assertEquals("#8e44ad", createdTheme.getPrimaryColor());
            assertEquals("Montserrat, sans-serif", createdTheme.getFontFamily());

            // Проверяем, что тенант связан с новой темой
            tenant1 = tenantRepository.findById(tenant1.getId()).orElseThrow();
            assertNotNull(tenant1.getTheme());
            assertEquals(themeResponse.getId(), tenant1.getTheme().getId());
        }

        @Test
        @DisplayName("PUT /theme - Should validate color format")
        void updateCurrentTheme_invalidColorFormat_shouldReturnBadRequest() throws Exception {
            ThemeUpdateRequest updateRequest = new ThemeUpdateRequest();
            updateRequest.setPrimaryColor("invalid-color-format");

            mockMvc.perform(put(THEME_API_URL)
                            .headers(getAuthHeaders(jwtTenant1))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.fieldErrors.primaryColor", containsString("Неверный формат цвета HEX")));
        }

        @Test
        @DisplayName("PUT /theme - Should validate URL length")
        void updateCurrentTheme_tooLongUrl_shouldReturnBadRequest() throws Exception {
            ThemeUpdateRequest updateRequest = new ThemeUpdateRequest();
            // Создаем URL длиннее 500 символов
            updateRequest.setLogoUrl("https://example.com/" + "a".repeat(500));

            mockMvc.perform(put(THEME_API_URL)
                            .headers(getAuthHeaders(jwtTenant1))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.fieldErrors.logoUrl", containsString("не может превышать 500 символов")));
        }

        @Test
        @DisplayName("PUT /theme - Tenant isolation - Should only update own theme")
        void updateCurrentTheme_tenantIsolation_shouldOnlyUpdateOwnTheme() throws Exception {
            String originalTenant2PrimaryColor = themeTenant2.getPrimaryColor();

            ThemeUpdateRequest updateRequest = new ThemeUpdateRequest();
            updateRequest.setPrimaryColor("#ff0000");

            // Обновляем тему для Tenant 1
            mockMvc.perform(put(THEME_API_URL)
                            .headers(getAuthHeaders(jwtTenant1))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.primaryColor", is("#ff0000")));

            // Проверяем, что тема Tenant 2 не изменилась
            Theme unchangedTheme = themeRepository.findById(themeTenant2.getId()).orElseThrow();
            assertEquals(originalTenant2PrimaryColor, unchangedTheme.getPrimaryColor());
        }

        @Test
        @DisplayName("PUT /theme - Should require authentication")
        void updateCurrentTheme_shouldRequireAuthentication() throws Exception {
            ThemeUpdateRequest updateRequest = new ThemeUpdateRequest();
            updateRequest.setPrimaryColor("#000000");

            mockMvc.perform(put(THEME_API_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("PUT /theme - Should require ADMIN role")
        void updateCurrentTheme_shouldRequireAdminRole() throws Exception {
            userTenant1.setRole("ROLE_USER");
            tenantUserRepository.saveAndFlush(userTenant1);
            String userJwt = jwtUtil.generateToken(userTenant1);

            ThemeUpdateRequest updateRequest = new ThemeUpdateRequest();
            updateRequest.setPrimaryColor("#000000");

            mockMvc.perform(put(THEME_API_URL)
                            .headers(getAuthHeaders(userJwt))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isForbidden());

            userTenant1.setRole("ROLE_ADMIN");
            tenantUserRepository.saveAndFlush(userTenant1);
        }
    }

    @Nested
    @DisplayName("GET /public/theme Tests")
    class GetPublicThemeTests {

        @Test
        @DisplayName("GET /public/theme - With tenant context from subdomain - Should return theme")
        void getPublicTheme_withTenantContext_shouldReturnTheme() throws Exception {
            mockMvc.perform(get(PUBLIC_THEME_API_URL)
                            .with(serverName("store1" + rootDomain))
                            .header("X-Tenant-Subdomain", "store1"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id", is(themeTenant1.getId().intValue())))
                    .andExpect(jsonPath("$.primaryColor", is("#3498db")))
                    .andExpect(jsonPath("$.fontFamily", is("Roboto, sans-serif")));
        }

        @Test
        @DisplayName("GET /public/theme - Different tenant context - Should return different theme")
        void getPublicTheme_differentTenantContext_shouldReturnDifferentTheme() throws Exception {
            mockMvc.perform(get(PUBLIC_THEME_API_URL)
                            .with(serverName("store2" + rootDomain))
                            .header("X-Tenant-Subdomain", "store2"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(themeTenant2.getId().intValue())))
                    .andExpect(jsonPath("$.primaryColor", is("#9b59b6")))
                    .andExpect(jsonPath("$.fontFamily", is("Open Sans, sans-serif")));
        }

        @Test
        @DisplayName("GET /public/theme - No tenant context - Should return 404")
        void getPublicTheme_noTenantContext_shouldReturn404() throws Exception {
            mockMvc.perform(get(PUBLIC_THEME_API_URL)
                            .with(serverName("unknown-domain.com")))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message", containsString("Tenant not found")));
        }

        @Test
        @DisplayName("GET /public/theme - Tenant without theme - Should create and return default theme")
        void getPublicTheme_tenantWithoutTheme_shouldCreateDefaultTheme() throws Exception {
            // Удаляем тему у первого тенанта
            tenant1.setTheme(null);
            tenantRepository.saveAndFlush(tenant1);

            MvcResult result = mockMvc.perform(get(PUBLIC_THEME_API_URL)
                            .with(serverName("store1" + rootDomain))
                            .header("X-Tenant-Subdomain", "store1"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id", notNullValue()))
                    .andExpect(jsonPath("$.primaryColor", is("#3498db"))) // Default value
                    .andExpect(jsonPath("$.fontFamily", is("Roboto, sans-serif"))) // Default value
                    .andReturn();

            // Проверяем, что дефолтная тема была создана
            String responseContent = result.getResponse().getContentAsString();
            ThemeResponse themeResponse = objectMapper.readValue(responseContent, ThemeResponse.class);

            Optional<Theme> createdThemeOpt = themeRepository.findById(themeResponse.getId());
            assertTrue(createdThemeOpt.isPresent(), "Default theme should be created");
        }

        @Test
        @DisplayName("GET /public/theme - Should not require authentication")
        void getPublicTheme_shouldNotRequireAuthentication() throws Exception {
            // Этот тест проверяет, что публичный эндпоинт не требует аутентификации
            mockMvc.perform(get(PUBLIC_THEME_API_URL)
                            .with(serverName("store1" + rootDomain))
                            .header("X-Tenant-Subdomain", "store1"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("Theme Full Flow Integration Tests")
    class ThemeFullFlowTests {

        @Test
        @DisplayName("Should handle complete theme management flow")
        void shouldHandleCompleteThemeManagementFlow() throws Exception {
            // 1. Получаем текущую тему
            MvcResult getResult = mockMvc.perform(get(THEME_API_URL)
                            .headers(getAuthHeaders(jwtTenant1)))
                    .andExpect(status().isOk())
                    .andReturn();

            ThemeResponse originalTheme = objectMapper.readValue(
                    getResult.getResponse().getContentAsString(), ThemeResponse.class);

            // 2. Обновляем тему
            ThemeUpdateRequest updateRequest = new ThemeUpdateRequest();
            updateRequest.setPrimaryColor("#ff5733");
            updateRequest.setSecondaryColor("#33c3ff");
            updateRequest.setButtonRadius("12px");
            updateRequest.setLogoUrl("https://newlogo.example.com/logo.png");

            mockMvc.perform(put(THEME_API_URL)
                            .headers(getAuthHeaders(jwtTenant1))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.primaryColor", is("#ff5733")))
                    .andExpect(jsonPath("$.secondaryColor", is("#33c3ff")))
                    .andExpect(jsonPath("$.buttonRadius", is("12px")))
                    .andExpect(jsonPath("$.logoUrl", is("https://newlogo.example.com/logo.png")));

            // 3. Проверяем, что тема доступна публично
            mockMvc.perform(get(PUBLIC_THEME_API_URL)
                            .with(serverName("store1" + rootDomain))
                            .header("X-Tenant-Subdomain", "store1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.primaryColor", is("#ff5733")))
                    .andExpect(jsonPath("$.logoUrl", is("https://newlogo.example.com/logo.png")));

            // 4. Проверяем, что другой тенант не видит изменений
            mockMvc.perform(get(THEME_API_URL)
                            .headers(getAuthHeaders(jwtTenant2)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.primaryColor", is(themeTenant2.getPrimaryColor())))
                    .andExpect(jsonPath("$.fontFamily", is("Open Sans, sans-serif")));
        }
    }
}