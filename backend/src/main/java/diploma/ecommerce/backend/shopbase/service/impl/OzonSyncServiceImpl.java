package diploma.ecommerce.backend.shopbase.service.impl;

import diploma.ecommerce.backend.shopbase.dto.request.OzonPriceUpdateRequest;
import diploma.ecommerce.backend.shopbase.dto.request.OzonStockUpdateRequest;
import diploma.ecommerce.backend.shopbase.exception.ResourceNotFoundException;
import diploma.ecommerce.backend.shopbase.model.Product;
import diploma.ecommerce.backend.shopbase.model.ProductOzonMapping;
import diploma.ecommerce.backend.shopbase.model.ProductOzonMapping.SyncStatus;
import diploma.ecommerce.backend.shopbase.model.Tenant;
import diploma.ecommerce.backend.shopbase.repository.ProductOzonMappingRepository;
import diploma.ecommerce.backend.shopbase.repository.ProductRepository;
import diploma.ecommerce.backend.shopbase.repository.TenantRepository;
import diploma.ecommerce.backend.shopbase.service.CryptoService;
import diploma.ecommerce.backend.shopbase.service.OzonApiService;
import diploma.ecommerce.backend.shopbase.service.OzonSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OzonSyncServiceImpl implements OzonSyncService {

    private final OzonApiService ozonApiService;
    private final ProductRepository productRepository;
    private final ProductOzonMappingRepository mappingRepository;
    private final TenantRepository tenantRepository;
    private final CryptoService cryptoService;

    @Override
    @Transactional
    public void syncProductsForTenant(Long tenantId) {
        log.info("Starting Ozon sync for tenant {}", tenantId);

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant", "id", tenantId));

        if (!tenant.isOzonSyncEnabled()) {
            log.warn("Ozon sync is disabled for tenant {}", tenantId);
            return;
        }

        if (tenant.getOzonApiKey() == null || tenant.getOzonClientId() == null) {
            log.warn("Tenant {} has no Ozon credentials configured", tenantId);
            return;
        }

        String apiKey = cryptoService.decrypt(tenant.getOzonApiKey());
        String clientId = cryptoService.decrypt(tenant.getOzonClientId());
        Long warehouseId = tenant.getOzonWarehouseId();

        // Получаем все активные маппинги
        List<ProductOzonMapping> mappings = mappingRepository.findActiveMappingsForSync(tenantId);

        if (mappings.isEmpty()) {
            log.info("No active products mapped to Ozon for tenant {}", tenantId);
            return;
        }

        // Обновляем статус на SYNCING
        mappings.forEach(mapping -> mapping.setSyncStatus(SyncStatus.SYNCING));
        mappingRepository.saveAll(mappings);

        // Группируем обновления
        List<OzonStockUpdateRequest> stockUpdates = new ArrayList<>();
        List<OzonPriceUpdateRequest> priceUpdates = new ArrayList<>();

        for (ProductOzonMapping mapping : mappings) {
            Product product = mapping.getProduct();

            // Подготавливаем обновление остатков
            stockUpdates.add(OzonStockUpdateRequest.builder()
                    .product_id(mapping.getOzonProductId())
                    .stock(product.getStockQuantity().longValue())
                    .warehouse_id(warehouseId != null ? warehouseId : 1L)
                    .build());

            // Подготавливаем обновление цен
            priceUpdates.add(OzonPriceUpdateRequest.builder()
                    .product_id(mapping.getOzonProductId())
                    .price(product.getPrice().toString())
                    .old_price(product.getPrice().toString())
                    .premium_price(product.getPrice().toString())
                    .build());
        }

        // Отправляем обновления
        try {
            // Обновляем остатки
            if (!stockUpdates.isEmpty()) {
                ozonApiService.updateStocks(clientId, apiKey, stockUpdates);
                LocalDateTime now = LocalDateTime.now();
                mappings.forEach(mapping -> mapping.setLastStockSync(now));
            }

            // Обновляем цены
            if (!priceUpdates.isEmpty()) {
                ozonApiService.updatePrices(clientId, apiKey, priceUpdates);
                LocalDateTime now = LocalDateTime.now();
                mappings.forEach(mapping -> mapping.setLastPriceSync(now));
            }

            // Обновляем статус синхронизации
            mappings.forEach(mapping -> {
                mapping.setSyncStatus(SyncStatus.SYNCED);
                mapping.setSyncError(null);
            });

            // Обновляем время последней синхронизации для тенанта
            tenant.setOzonLastSyncAt(LocalDateTime.now());
            tenantRepository.save(tenant);

            log.info("Successfully synced {} products for tenant {}", mappings.size(), tenantId);

        } catch (Exception e) {
            log.error("Error syncing products for tenant {}: ", tenantId, e);

            // Обновляем статус с ошибкой
            mappings.forEach(mapping -> {
                mapping.setSyncStatus(SyncStatus.ERROR);
                mapping.setSyncError(e.getMessage());
            });
        } finally {
            mappingRepository.saveAll(mappings);
        }
    }

    @Override
    @Transactional
    public void syncSingleProduct(Long tenantId, Long productId) {
        log.info("Syncing single product {} for tenant {}", productId, tenantId);

        ProductOzonMapping mapping = mappingRepository.findByTenantIdAndProductId(tenantId, productId)
                .orElseThrow(() -> new ResourceNotFoundException("ProductOzonMapping", "productId", productId, tenantId));

        if (!mapping.getActive()) {
            log.warn("Mapping for product {} is inactive", productId);
            return;
        }

        Tenant tenant = mapping.getTenant();

        if (!tenant.isOzonSyncEnabled()) {
            log.warn("Ozon sync is disabled for tenant {}", tenantId);
            return;
        }

        String apiKey = cryptoService.decrypt(tenant.getOzonApiKey());
        String clientId = cryptoService.decrypt(tenant.getOzonClientId());
        Long warehouseId = mapping.getWarehouseId() != null ? mapping.getWarehouseId() : tenant.getOzonWarehouseId();

        Product product = mapping.getProduct();

        mapping.setSyncStatus(SyncStatus.SYNCING);
        mappingRepository.save(mapping);

        try {
            // Обновляем остатки
            List<OzonStockUpdateRequest> stockUpdate = List.of(
                    OzonStockUpdateRequest.builder()
                            .product_id(mapping.getOzonProductId())
                            .stock(product.getStockQuantity().longValue())
                            .warehouse_id(warehouseId != null ? warehouseId : 1L)
                            .build()
            );
            ozonApiService.updateStocks(clientId, apiKey, stockUpdate);
            mapping.setLastStockSync(LocalDateTime.now());

            // Обновляем цены
            List<OzonPriceUpdateRequest> priceUpdate = List.of(
                    OzonPriceUpdateRequest.builder()
                            .product_id(mapping.getOzonProductId())
                            .price(product.getPrice().toString())
                            .old_price(product.getPrice().toString())
                            .premium_price(product.getPrice().toString())
                            .build()
            );
            ozonApiService.updatePrices(clientId, apiKey, priceUpdate);
            mapping.setLastPriceSync(LocalDateTime.now());

            mapping.setSyncStatus(SyncStatus.SYNCED);
            mapping.setSyncError(null);

            log.info("Successfully synced product {} for tenant {}", productId, tenantId);

        } catch (Exception e) {
            log.error("Error syncing product {} for tenant {}: ", productId, tenantId, e);
            mapping.setSyncStatus(SyncStatus.ERROR);
            mapping.setSyncError(e.getMessage());
        } finally {
            mappingRepository.save(mapping);
        }
    }

    @Override
    @Transactional
    public ProductOzonMapping createMapping(Long tenantId, Long productId, Long ozonProductId, Long ozonSku) {
        log.info("Creating Ozon mapping for product {} in tenant {}", productId, tenantId);

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant", "id", tenantId));

        Product product = productRepository.findByTenantIdAndId(tenantId, productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId, tenantId));

        if (mappingRepository.existsByTenantIdAndProductId(tenantId, productId)) {
            throw new IllegalArgumentException("Product already mapped to Ozon");
        }

        if (mappingRepository.existsByTenantIdAndOzonProductId(tenantId, ozonProductId)) {
            throw new IllegalArgumentException("Ozon product already mapped to another product");
        }

        ProductOzonMapping mapping = new ProductOzonMapping();
        mapping.setTenant(tenant);
        mapping.setProduct(product);
        mapping.setOzonProductId(ozonProductId);
        mapping.setOzonSku(ozonSku);
        mapping.setWarehouseId(tenant.getOzonWarehouseId());
        mapping.setSyncStatus(SyncStatus.PENDING);
        mapping.setActive(true);

        ProductOzonMapping savedMapping = mappingRepository.save(mapping);

        // Запускаем первую синхронизацию
        syncSingleProduct(tenantId, productId);

        return savedMapping;
    }

    @Override
    @Transactional
    public ProductOzonMapping updateMapping(Long tenantId, Long mappingId, ProductOzonMapping mappingDetails) {
        log.info("Updating Ozon mapping {} for tenant {}", mappingId, tenantId);

        ProductOzonMapping existingMapping = mappingRepository.findByTenantIdAndId(tenantId, mappingId)
                .orElseThrow(() -> new ResourceNotFoundException("ProductOzonMapping", "id", mappingId, tenantId));

        if (mappingDetails.getOzonProductId() != null) {
            existingMapping.setOzonProductId(mappingDetails.getOzonProductId());
        }
        if (mappingDetails.getOzonSku() != null) {
            existingMapping.setOzonSku(mappingDetails.getOzonSku());
        }
        if (mappingDetails.getOzonFboSku() != null) {
            existingMapping.setOzonFboSku(mappingDetails.getOzonFboSku());
        }
        if (mappingDetails.getOzonFbsSku() != null) {
            existingMapping.setOzonFbsSku(mappingDetails.getOzonFbsSku());
        }
        if (mappingDetails.getWarehouseId() != null) {
            existingMapping.setWarehouseId(mappingDetails.getWarehouseId());
        }
        if (mappingDetails.getActive() != null) {
            existingMapping.setActive(mappingDetails.getActive());
        }

        return mappingRepository.save(existingMapping);
    }

    @Override
    @Transactional
    public void deleteMapping(Long tenantId, Long mappingId) {
        log.warn("Deleting Ozon mapping {} for tenant {}", mappingId, tenantId);

        ProductOzonMapping mapping = mappingRepository.findByTenantIdAndId(tenantId, mappingId)
                .orElseThrow(() -> new ResourceNotFoundException("ProductOzonMapping", "id", mappingId, tenantId));

        mappingRepository.delete(mapping);
        log.info("Ozon mapping {} deleted for tenant {}", mappingId, tenantId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductOzonMapping> getMappings(Long tenantId, Pageable pageable) {
        if (!tenantRepository.existsById(tenantId)) {
            throw new ResourceNotFoundException("Tenant", "id", tenantId);
        }
        return mappingRepository.findByTenantId(tenantId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductOzonMapping getMapping(Long tenantId, Long mappingId) {
        return mappingRepository.findByTenantIdAndId(tenantId, mappingId)
                .orElseThrow(() -> new ResourceNotFoundException("ProductOzonMapping", "id", mappingId, tenantId));
    }

    @Override
    @Transactional
    public void enableOzonSync(Long tenantId, boolean enabled) {
        log.info("Setting Ozon sync to {} for tenant {}", enabled, tenantId);

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant", "id", tenantId));

        tenant.setOzonSyncEnabled(enabled);
        tenantRepository.save(tenant);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean testOzonConnection(Long tenantId) {
        log.info("Testing Ozon connection for tenant {}", tenantId);

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant", "id", tenantId));

        if (tenant.getOzonApiKey() == null || tenant.getOzonClientId() == null) {
            log.warn("Tenant {} has no Ozon credentials configured", tenantId);
            return false;
        }

        String apiKey = cryptoService.decrypt(tenant.getOzonApiKey());
        String clientId = cryptoService.decrypt(tenant.getOzonClientId());

        return ozonApiService.testConnection(clientId, apiKey);
    }
}