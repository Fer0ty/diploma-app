package diploma.ecommerce.backend.shopbase.controller;

import diploma.ecommerce.backend.shopbase.dto.mapper.ProductOzonMappingMapper;
import diploma.ecommerce.backend.shopbase.dto.request.ProductOzonMappingRequest;
import diploma.ecommerce.backend.shopbase.dto.response.ProductOzonMappingResponse;
import diploma.ecommerce.backend.shopbase.model.ProductOzonMapping;
import diploma.ecommerce.backend.shopbase.multitenancy.TenantContext;
import diploma.ecommerce.backend.shopbase.service.OzonSyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/ozon")
@RequiredArgsConstructor
@Tag(name = "Ozon Integration", description = "API для управления интеграцией с Ozon")
public class OzonIntegrationController {

    private final OzonSyncService ozonSyncService;
    private final ProductOzonMappingMapper mappingMapper;

    private Long getCurrentTenantIdOrFail() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            log.error("Tenant ID could not be resolved for the current request");
            throw new AccessDeniedException("Tenant context not established");
        }
        return tenantId;
    }

    @Operation(summary = "Создать связь товара с Ozon")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Связь успешно создана"),
            @ApiResponse(responseCode = "400", description = "Некорректный запрос"),
            @ApiResponse(responseCode = "404", description = "Товар не найден"),
            @ApiResponse(responseCode = "409", description = "Товар уже связан с Ozon")
    })
    @PostMapping("/products/{productId}/map")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductOzonMappingResponse> mapProduct(
            @Parameter(description = "ID товара") @PathVariable Long productId,
            @Parameter(description = "Данные для связи") @RequestBody @Valid ProductOzonMappingRequest request
    ) {
        Long tenantId = getCurrentTenantIdOrFail();

        ProductOzonMapping mapping = ozonSyncService.createMapping(
                tenantId,
                productId,
                request.getOzonProductId(),
                request.getOzonSku()
        );

        // Установка дополнительных полей
        if (request.getOzonFboSku() != null) {
            mapping.setOzonFboSku(request.getOzonFboSku());
        }
        if (request.getOzonFbsSku() != null) {
            mapping.setOzonFbsSku(request.getOzonFbsSku());
        }
        if (request.getWarehouseId() != null) {
            mapping.setWarehouseId(request.getWarehouseId());
        }

        mapping = ozonSyncService.updateMapping(tenantId, mapping.getId(), mapping);

        return ResponseEntity.ok(mappingMapper.toResponse(mapping));
    }

    @Operation(summary = "Получить все связи товаров с Ozon")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список связей получен"),
            @ApiResponse(responseCode = "401", description = "Не авторизован")
    })
    @GetMapping("/mappings")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<ProductOzonMappingResponse>> getMappings(
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Long tenantId = getCurrentTenantIdOrFail();

        Page<ProductOzonMapping> mappings = ozonSyncService.getMappings(tenantId, pageable);

        return ResponseEntity.ok(mappings.map(mappingMapper::toResponse));
    }

    @Operation(summary = "Получить связь по ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Связь найдена"),
            @ApiResponse(responseCode = "404", description = "Связь не найдена")
    })
    @GetMapping("/mappings/{mappingId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductOzonMappingResponse> getMapping(
            @Parameter(description = "ID связи") @PathVariable Long mappingId
    ) {
        Long tenantId = getCurrentTenantIdOrFail();

        ProductOzonMapping mapping = ozonSyncService.getMapping(tenantId, mappingId);

        return ResponseEntity.ok(mappingMapper.toResponse(mapping));
    }

    @Operation(summary = "Удалить связь товара с Ozon")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Связь удалена"),
            @ApiResponse(responseCode = "404", description = "Связь не найдена")
    })
    @DeleteMapping("/mappings/{mappingId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteMapping(
            @Parameter(description = "ID связи") @PathVariable Long mappingId
    ) {
        Long tenantId = getCurrentTenantIdOrFail();

        ozonSyncService.deleteMapping(tenantId, mappingId);

        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Запустить синхронизацию всех товаров с Ozon")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Синхронизация запущена"),
            @ApiResponse(responseCode = "400", description = "Синхронизация отключена или не настроена")
    })
    @PostMapping("/sync")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> triggerSync() {
        Long tenantId = getCurrentTenantIdOrFail();

        ozonSyncService.syncProductsForTenant(tenantId);

        return ResponseEntity.ok(Map.of(
                "status", "Sync completed",
                "message", "Products synchronized with Ozon"
        ));
    }

    @Operation(summary = "Синхронизировать один товар с Ozon")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Товар синхронизирован"),
            @ApiResponse(responseCode = "404", description = "Товар или связь не найдены")
    })
    @PostMapping("/products/{productId}/sync")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> syncProduct(
            @Parameter(description = "ID товара") @PathVariable Long productId
    ) {
        Long tenantId = getCurrentTenantIdOrFail();

        ozonSyncService.syncSingleProduct(tenantId, productId);

        return ResponseEntity.ok(Map.of(
                "status", "Sync completed",
                "message", "Product synchronized with Ozon"
        ));
    }

    @Operation(summary = "Включить/выключить синхронизацию с Ozon")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Настройка изменена"),
            @ApiResponse(responseCode = "401", description = "Не авторизован")
    })
    @PutMapping("/sync/enabled")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Boolean>> setSyncEnabled(
            @Parameter(description = "Включить синхронизацию") @RequestParam boolean enabled
    ) {
        Long tenantId = getCurrentTenantIdOrFail();

        ozonSyncService.enableOzonSync(tenantId, enabled);

        return ResponseEntity.ok(Map.of("enabled", enabled));
    }

    @Operation(summary = "Проверить подключение к Ozon")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Результат проверки"),
            @ApiResponse(responseCode = "401", description = "Не авторизован")
    })
    @GetMapping("/test-connection")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Boolean>> testConnection() {
        Long tenantId = getCurrentTenantIdOrFail();

        boolean connected = ozonSyncService.testOzonConnection(tenantId);

        return ResponseEntity.ok(Map.of("connected", connected));
    }
}