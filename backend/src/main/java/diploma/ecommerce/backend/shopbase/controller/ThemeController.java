package diploma.ecommerce.backend.shopbase.controller;

import diploma.ecommerce.backend.shopbase.dto.mapper.ThemeMapper;
import diploma.ecommerce.backend.shopbase.dto.request.ThemeUpdateRequest;
import diploma.ecommerce.backend.shopbase.dto.response.ThemeResponse;
import diploma.ecommerce.backend.shopbase.exception.ResourceNotFoundException;
import diploma.ecommerce.backend.shopbase.model.Tenant;
import diploma.ecommerce.backend.shopbase.model.Theme;
import diploma.ecommerce.backend.shopbase.multitenancy.TenantContext;
import diploma.ecommerce.backend.shopbase.service.TenantService;
import diploma.ecommerce.backend.shopbase.service.ThemeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Theme Management", description = "API для управления темой магазина")
public class ThemeController {

    private final TenantService tenantService;
    private final ThemeService themeService;
    private final ThemeMapper themeMapper;

    private Long getCurrentTenantIdOrFail() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            log.error("Tenant ID could not be resolved for the current request. Access denied or configuration issue.");
            throw new AccessDeniedException("Tenant context not established.");
        }
        log.trace("Using Tenant ID: {}", tenantId);
        return tenantId;
    }

    @Operation(
            summary = "Получить настройки темы текущего магазина",
            description = "Возвращает настройки темы магазина текущего авторизованного пользователя"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Успешное получение настроек темы",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ThemeResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Неавторизован"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен"),
            @ApiResponse(responseCode = "404", description = "Магазин не найден")
    })
    @GetMapping("/theme")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ThemeResponse> getCurrentTheme() {
        Long tenantId = getCurrentTenantIdOrFail();
        Tenant tenant = tenantService.getTenantById(tenantId);

        if (tenant.getTheme() == null) {
            log.warn("No theme found for tenant ID: {}. Creating default theme.", tenantId);
            // Создаем тему по умолчанию, если её нет
            Theme defaultTheme = themeService.getDefaultTheme();
            defaultTheme = themeService.createTheme(defaultTheme);
            tenant.setTheme(defaultTheme);
            tenant = tenantService.updateTenant(tenantId, tenant);
        }

        ThemeResponse response = themeMapper.toThemeResponse(tenant.getTheme());
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Обновить настройки темы текущего магазина",
            description = "Обновляет настройки темы магазина текущего авторизованного пользователя"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Успешное обновление настроек темы",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ThemeResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Неверный запрос"),
            @ApiResponse(responseCode = "401", description = "Неавторизован"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен"),
            @ApiResponse(responseCode = "404", description = "Магазин не найден")
    })
    @PutMapping("/theme")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ThemeResponse> updateCurrentTheme(
            @Parameter(description = "Новые настройки темы", required = true)
            @Valid @RequestBody ThemeUpdateRequest request
    ) {
        Long tenantId = getCurrentTenantIdOrFail();
        Tenant tenant = tenantService.getTenantById(tenantId);

        Theme themeToUpdate;
        if (tenant.getTheme() == null) {
            // Если темы нет, создаем новую из запроса
            themeToUpdate = themeMapper.createThemeFromRequest(request);
            themeToUpdate = themeService.createTheme(themeToUpdate);
            tenant.setTheme(themeToUpdate);
            tenantService.updateTenant(tenantId, tenant);
        } else {
            // Обновляем существующую тему
            themeToUpdate = tenant.getTheme();
            themeMapper.updateThemeFromRequest(request, themeToUpdate);
            themeToUpdate = themeService.updateTheme(themeToUpdate.getId(), themeToUpdate);
        }

        ThemeResponse response = themeMapper.toThemeResponse(themeToUpdate);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Получить публичные настройки темы текущего магазина",
            description = "Возвращает настройки темы для текущего магазина. Контекст тенанта определяется автоматически из поддомена."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Успешное получение настроек темы",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ThemeResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "404", description = "Магазин не найден или контекст тенанта не определен")
    })
    @GetMapping("/public/theme")
    public ResponseEntity<ThemeResponse> getPublicThemeFromContext(HttpServletRequest request) {
        Long tenantId = TenantContext.getTenantId();

        if (tenantId == null) {
            log.warn("Tenant context not established for theme request from {}", request.getRemoteAddr());
            log.debug("Headers: Host={}, X-Tenant-Subdomain={}, X-Tenant-Host={}",
                    request.getHeader("Host"),
                    request.getHeader("X-Tenant-Subdomain"),
                    request.getHeader("X-Tenant-Host"));
            throw new ResourceNotFoundException("Tenant", "id", null);
        }

        log.debug("Getting theme for automatically detected tenant ID: {}", tenantId);
        Tenant tenant = tenantService.getTenantById(tenantId);

        if (tenant.getTheme() == null) {
            log.warn("No theme found for tenant ID: {}. Creating default theme.", tenantId);
            // Создаем тему по умолчанию, если её нет
            Theme defaultTheme = themeService.getDefaultTheme();
            defaultTheme = themeService.createTheme(defaultTheme);
            tenant.setTheme(defaultTheme);
            tenant = tenantService.updateTenant(tenantId, tenant);
        }

        ThemeResponse response = themeMapper.toThemeResponse(tenant.getTheme());
        return ResponseEntity.ok(response);
    }
}