package diploma.ecommerce.backend.shopbase.controller;

import diploma.ecommerce.backend.shopbase.dto.request.TenantUpdateRequest;
import diploma.ecommerce.backend.shopbase.dto.response.TenantResponse;
import diploma.ecommerce.backend.shopbase.multitenancy.TenantContext;
import diploma.ecommerce.backend.shopbase.service.TenantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@RequestMapping("/api/v1/store")
@RequiredArgsConstructor
@Tag(name = "Store Management", description = "API для управления информацией о текущем магазине")
@SecurityRequirement(name = "bearerAuth")
public class StoreController {

    private final TenantService tenantService;

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
            summary = "Получить информацию о текущем магазине",
            description = "Возвращает информацию о магазине текущего пользователя"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Успешное получение информации о магазине",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TenantResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Неавторизован"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен"),
            @ApiResponse(responseCode = "404", description = "Магазин не найден")
    })
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TenantResponse> getCurrentStore() {
        Long tenantId = getCurrentTenantIdOrFail();
        TenantResponse response = tenantService.getTenantResponse(tenantId);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Обновить информацию о текущем магазине",
            description = "Обновляет информацию о магазине текущего пользователя, включая контакты и API ключи"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Успешное обновление информации о магазине",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TenantResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Неверный запрос"),
            @ApiResponse(responseCode = "401", description = "Неавторизован"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен"),
            @ApiResponse(responseCode = "404", description = "Магазин не найден"),
            @ApiResponse(responseCode = "409", description = "Конфликт (название уже существует)")
    })
    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TenantResponse> updateCurrentStore(
            @Parameter(description = "Данные для обновления магазина", required = true)
            @Valid @RequestBody TenantUpdateRequest request
    ) {
        Long tenantId = getCurrentTenantIdOrFail();
        tenantService.updateTenantFromRequest(tenantId, request);
        TenantResponse response = tenantService.getTenantResponse(tenantId);
        return ResponseEntity.ok(response);
    }
}