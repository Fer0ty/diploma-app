package diploma.ecommerce.backend.shopbase.controller;

import diploma.ecommerce.backend.shopbase.dto.mapper.ProductWildberriesMappingMapper;
import diploma.ecommerce.backend.shopbase.dto.request.ProductWildberriesMappingRequest;
import diploma.ecommerce.backend.shopbase.dto.response.ProductWildberriesMappingResponse;
import diploma.ecommerce.backend.shopbase.model.ProductWildberriesMapping;
import diploma.ecommerce.backend.shopbase.multitenancy.TenantContext;
import diploma.ecommerce.backend.shopbase.service.MarketplaceSyncService;
import diploma.ecommerce.backend.shopbase.service.WildberriesSyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/wildberries")
@RequiredArgsConstructor
@Tag(name = "Wildberries Integration", description = "API для управления интеграцией с Wildberries")
public class WildberriesIntegrationController {

    private final WildberriesSyncService wildberriesSyncService;
    private final MarketplaceSyncService marketplaceSyncService;
    private final ProductWildberriesMappingMapper mappingMapper;

    private Long getCurrentTenantIdOrFail() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            log.error("Tenant ID could not be resolved for the current request");
            throw new AccessDeniedException("Tenant context not established");
        }
        return tenantId;
    }

    @Operation(summary = "Создать связь товара с Wildberries")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Связь успешно создана"),
            @ApiResponse(responseCode = "400", description = "Некорректный запрос"),
            @ApiResponse(responseCode = "404", description = "Товар не найден"),
            @ApiResponse(responseCode = "409", description = "Товар уже связан с Wildberries")
    })
    @PostMapping("/products/{productId}/map")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductWildberriesMappingResponse> mapProduct(
            @Parameter(description = "ID товара") @PathVariable Long productId,
            @Parameter(description = "Данные для связи") @RequestBody @Valid ProductWildberriesMappingRequest request
    ) {
        Long tenantId = getCurrentTenantIdOrFail();

        ProductWildberriesMapping mapping = wildberriesSyncService.createMapping(
                tenantId,
                productId,
                request.getWbNmId(),
                request.getWbSku()
        );

        // Установка дополнительных полей
        if (request.getWbBarcode() != null) {
            mapping.setWbBarcode(request.getWbBarcode());
        }
        if (request.getWbImId() != null) {
            mapping.setWbImId(request.getWbImId());
        }

        mapping = wildberriesSyncService.updateMapping(tenantId, mapping.getId(), mapping);

        // Запускаем первую синхронизацию
        marketplaceSyncService.syncSingleProduct(tenantId, productId, MarketplaceAdapter.MarketplaceType.WILDBERRIES);

        return ResponseEntity.ok(mappingMapper.toResponse(mapping));
    }

    @Operation(summary = "Получить все связи товаров с Wildberries")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список связей получен"),
            @ApiResponse(responseCode = "401", description = "Не авторизован")
    })
    @GetMapping("/mappings")
    @PreAuthorize("hasRole('ADMIN')")
    pub