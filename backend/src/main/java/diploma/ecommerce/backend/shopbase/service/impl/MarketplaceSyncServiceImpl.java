package diploma.ecommerce.backend.shopbase.service.impl;

import diploma.ecommerce.backend.shopbase.exception.ResourceNotFoundException;
import diploma.ecommerce.backend.shopbase.integration.impl.OzonAdapter;
import diploma.ecommerce.backend.shopbase.integration.impl.WildberriesAdapter;
import diploma.ecommerce.backend.shopbase.model.Product;
import diploma.ecommerce.backend.shopbase.model.ProductOzonMapping;
import diploma.ecommerce.backend.shopbase.model.ProductWildberriesMapping;
import diploma.ecommerce.backend.shopbase.model.Tenant;
import diploma.ecommerce.backend.shopbase.repository.ProductOzonMappingRepository;
import diploma.ecommerce.backend.shopbase.repository.ProductWildberriesMappingRepository;
import diploma.ecommerce.backend.shopbase.repository.TenantRepository;
import diploma.ecommerce.backend.shopbase.service.CryptoService;
import diploma.ecommerce.backend.shopbase.service.MarketplaceSyncService;
import diploma.ecommerce.backend.shopbase.service.OzonApiService;
import diploma.ecommerce.backend.shopbase.service.WildberriesApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MarketplaceSyncServiceImpl implements MarketplaceSyncService {

    private final TenantRepository tenantRepository;
    private final ProductOzonMappingRepository ozonMappingRepository;
    private final ProductWildberriesMappingRepository wbMappingRepository;
    private final CryptoService cryptoService;
    private final OzonApiService ozonApiService;
    private final WildberriesApiService wildberriesApiService;

    @Override
    @Transactional
    public void syncProducts(Long tenantId, MarketplaceAdapter.MarketplaceType type) {
        log.info("Starting {} sync for tenant {}", type, tenantId);

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant", "id", tenantId));

        MarketplaceAdapter adapter = createAdapter(tenant, type);
        if (adapter == null) {
            log.warn("No {} configuration found for tenant {}", type, tenantId);
            return;
        }

        switch (type) {
            case OZON -> syncOzonProducts(tenant, adapter);
            case WILDBERRIES -> syncWildberriesProducts(tenant, adapter);
        }

        updateLastSyncTime(tenant, type);
        tenantRepository.save(tenant);
    }

    @Override
    @Transactional
    public void syncSingleProduct(Long tenantId, Long productId, MarketplaceAdapter.MarketplaceType type) {
        log.info("Syncing single product {} for tenant {} on {}", productId, tenantId, type);

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant", "id", tenantId));

        MarketplaceAdapter adapter = createAdapter(tenant, type);
        if (adapter == null) {
            log.warn("No {} configuration found for tenant {}", type, tenantId);
            return;
        }

        switch (type) {
            case OZON -> syncSingleOzonProduct(tenant, productId, adapter);
            case WILDBERRIES -> syncSingleWildberriesProduct(tenant, productId, adapter);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<?> getMappings(Long tenantId, MarketplaceAdapter.MarketplaceType type, Pageable pageable) {
        if (!tenantRepository.existsById(tenantId)) {
            throw new ResourceNotFoundException("Tenant", "id", tenantId);
        }

        return switch (type) {
            case OZON -> ozonMappingRepository.findByTenantId(tenantId, pageable);
            case WILDBERRIES -> wbMappingRepository.findByTenantId(tenantId, pageable);
        };
    }

    @Override
    @Transactional
    public void enableSync(Long tenantId, MarketplaceAdapter.MarketplaceType type, boolean enabled) {
        log.info("Setting {} sync to {} for tenant {}", type, enabled, tenantId);

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant", "id", tenantId));

        switch (type) {
            case OZON -> tenant.setOzonSyncEnabled(enabled);
            case WILDBERRIES -> tenant.setWildberriesSyncEnabled(enabled);
        }

        tenantRepository.save(tenant);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean testConnection(Long tenantId, MarketplaceAdapter.MarketplaceType type) {
        log.info("Testing {} connection for tenant {}", type, tenantId);

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant", "id", tenantId));

        MarketplaceAdapter adapter = createAdapter(tenant, type);
        return adapter != null && adapter.testConnection();
    }

    private MarketplaceAdapter createAdapter(Tenant tenant, MarketplaceAdapter.MarketplaceType type) {
        return switch (type) {
            case OZON -> {
                if (tenant.getOzonApiKey() == null || tenant.getOzonClientId() == null) {
                    yield null;
                }
                String apiKey = cryptoService.decrypt(tenant.getOzonApiKey());
                String clientId = cryptoService.decrypt(tenant.getOzonClientId());
                yield new OzonAdapter(ozonApiService, clientId, apiKey);
            }
            case WILDBERRIES -> {
                if (tenant.getWildberriesApiKey() == null) {
                    yield null;
                }
                String apiKey = cryptoService.decrypt(tenant.getWildberriesApiKey());
                yield new WildberriesAdapter(wildberriesApiService, apiKey);
            }
        };
    }

    private void syncOzonProducts(Tenant tenant, MarketplaceAdapter adapter) {
        List<ProductOzonMapping> mappings = ozonMappingRepository.findActiveMappingsForSync(tenant.getId());

        if (mappings.isEmpty()) {
            log.info("No active Ozon products mapped for tenant {}", tenant.getId());
            return;
        }

        // Обновляем статус
        mappings.forEach(m -> m.setSyncStatus(ProductOzonMapping.SyncStatus.SYNCING));
        ozonMappingRepository.saveAll(mappings);

        try {
            // Подготавливаем обновления
            List<MarketplaceAdapter.StockUpdate> stockUpdates = mappings.stream()
                    .map(m -> new MarketplaceAdapter.StockUpdate(
                            m.getOzonSku().toString(),
                            m.getOzonProductId(),
                            m.getProduct().getStockQuantity(),
                            m.getWarehouseId() != null ? m.getWarehouseId() : tenant.getOzonWarehouseId()
                    ))
                    .toList();

            List<MarketplaceAdapter.PriceUpdate> priceUpdates = mappings.stream()
                    .map(m -> new MarketplaceAdapter.PriceUpdate(
                            m.getOzonSku().toString(),
                            m.getOzonProductId(),
                            m.getProduct().getPrice().toString(),
                            null
                    ))
                    .toList();

            // Отправляем обновления
            adapter.updateStocks(stockUpdates);
            adapter.updatePrices(priceUpdates);

            // Обновляем статус
            LocalDateTime now = LocalDateTime.now();
            mappings.forEach(m -> {
                m.setSyncStatus(ProductOzonMapping.SyncStatus.SYNCED);
                m.setSyncError(null);
                m.setLastStockSync(now);
                m.setLastPriceSync(now);
            });

            log.info("Successfully synced {} Ozon products for tenant {}", mappings.size(), tenant.getId());

        } catch (Exception e) {
            log.error("Error syncing Ozon products: ", e);
            mappings.forEach(m -> {
                m.setSyncStatus(ProductOzonMapping.SyncStatus.ERROR);
                m.setSyncError(e.getMessage());
            });
        } finally {
            ozonMappingRepository.saveAll(mappings);
        }
    }

    private void syncWildberriesProducts(Tenant tenant, MarketplaceAdapter adapter) {
        List<ProductWildberriesMapping> mappings = wbMappingRepository.findActiveMappingsForSync(tenant.getId());

        if (mappings.isEmpty()) {
            log.info("No active Wildberries products mapped for tenant {}", tenant.getId());
            return;
        }

        // Обновляем статус
        mappings.forEach(m -> m.setSyncStatus(ProductWildberriesMapping.SyncStatus.SYNCING));
        wbMappingRepository.saveAll(mappings);

        try {
            // Подготавливаем обновления
            List<MarketplaceAdapter.StockUpdate> stockUpdates = mappings.stream()
                    .map(m -> new MarketplaceAdapter.StockUpdate(
                            m.getWbSku(),
                            m.getWbNmId(),
                            m.getProduct().getStockQuantity(),
                            tenant.getWildberriesWarehouseId() != null ?
                                    tenant.getWildberriesWarehouseId().longValue() : 1L
                    ))
                    .toList();

            List<MarketplaceAdapter.PriceUpdate> priceUpdates = mappings.stream()
                    .map(m -> new MarketplaceAdapter.PriceUpdate(
                            m.getWbSku(),
                            m.getWbNmId(),
                            m.getProduct().getPrice().toString(),
                            null
                    ))
                    .toList();

            // Отправляем обновления
            adapter.updateStocks(stockUpdates);
            adapter.updatePrices(priceUpdates);

            // Обновляем статус
            LocalDateTime now = LocalDateTime.now();
            mappings.forEach(m -> {
                m.setSyncStatus(ProductWildberriesMapping.SyncStatus.SYNCED);
                m.setSyncError(null);
                m.setLastStockSync(now);
                m.setLastPriceSync(now);
            });

            log.info("Successfully synced {} Wildberries products for tenant {}", mappings.size(), tenant.getId());

        } catch (Exception e) {
            log.error("Error syncing Wildberries products: ", e);
            mappings.forEach(m -> {
                m.setSyncStatus(ProductWildberriesMapping.SyncStatus.ERROR);
                m.setSyncError(e.getMessage());
            });
        } finally {
            wbMappingRepository.saveAll(mappings);
        }
    }

    private void syncSingleOzonProduct(Tenant tenant, Long productId, MarketplaceAdapter adapter) {
        ProductOzonMapping mapping = ozonMappingRepository.findByTenantIdAndProductId(tenant.getId(), productId)
                .orElseThrow(() -> new ResourceNotFoundException("ProductOzonMapping", "productId", productId));

        syncSingleOzonMapping(mapping, adapter, tenant);
    }

    private void syncSingleWildberriesProduct(Tenant tenant, Long productId, MarketplaceAdapter adapter) {
        ProductWildberriesMapping mapping = wbMappingRepository.findByTenantIdAndProductId(tenant.getId(), productId)
                .orElseThrow(() -> new ResourceNotFoundException("ProductWildberriesMapping", "productId", productId));

        syncSingleWildberriesMapping(mapping, adapter, tenant);
    }

    private void syncSingleOzonMapping(ProductOzonMapping mapping, MarketplaceAdapter adapter, Tenant tenant) {
        mapping.setSyncStatus(ProductOzonMapping.SyncStatus.SYNCING);
        ozonMappingRepository.save(mapping);

        try {
            Product product = mapping.getProduct();

            // Обновляем остатки
            adapter.updateStocks(List.of(new MarketplaceAdapter.StockUpdate(
                    mapping.getOzonSku().toString(),
                    mapping.getOzonProductId(),
                    product.getStockQuantity(),
                    mapping.getWarehouseId() != null ? mapping.getWarehouseId() : tenant.getOzonWarehouseId()
            )));

            // Обновляем цены
            adapter.updatePrices(List.of(new MarketplaceAdapter.PriceUpdate(
                    mapping.getOzonSku().toString(),
                    mapping.getOzonProductId(),
                    product.getPrice().toString(),
                    null
            )));

            LocalDateTime now = LocalDateTime.now();
            mapping.setSyncStatus(ProductOzonMapping.SyncStatus.SYNCED);
            mapping.setSyncError(null);
            mapping.setLastStockSync(now);
            mapping.setLastPriceSync(now);

        } catch (Exception e) {
            log.error("Error syncing single Ozon product: ", e);
            mapping.setSyncStatus(ProductOzonMapping.SyncStatus.ERROR);
            mapping.setSyncError(e.getMessage());
        } finally {
            ozonMappingRepository.save(mapping);
        }
    }

    private void syncSingleWildberriesMapping(ProductWildberriesMapping mapping, MarketplaceAdapter adapter, Tenant tenant) {
        mapping.setSyncStatus(ProductWildberriesMapping.SyncStatus.SYNCING);
        wbMappingRepository.save(mapping);

        try {
            Product product = mapping.getProduct();

            // Обновляем остатки
            adapter.updateStocks(List.of(new MarketplaceAdapter.StockUpdate(
                    mapping.getWbSku(),
                    mapping.getWbNmId(),
                    product.getStockQuantity(),
                    tenant.getWildberriesWarehouseId() != null ?
                            tenant.getWildberriesWarehouseId().longValue() : 1L
            )));

            // Обновляем цены
            adapter.updatePrices(List.of(new MarketplaceAdapter.PriceUpdate(
                    mapping.getWbSku(),
                    mapping.getWbNmId(),
                    product.getPrice().toString(),
                    null
            )));

            LocalDateTime now = LocalDateTime.now();
            mapping.setSyncStatus(ProductWildberriesMapping.SyncStatus.SYNCED);
            mapping.setSyncError(null);
            mapping.setLastStockSync(now);
            mapping.setLastPriceSync(now);

        } catch (Exception e) {
            log.error("Error syncing single Wildberries product: ", e);
            mapping.setSyncStatus(ProductWildberriesMapping.SyncStatus.ERROR);
            mapping.setSyncError(e.getMessage());
        } finally {
            wbMappingRepository.save(mapping);
        }
    }

    private void updateLastSyncTime(Tenant tenant, MarketplaceAdapter.MarketplaceType type) {
        LocalDateTime now = LocalDateTime.now();
        switch (type) {
            case OZON -> tenant.setOzonLastSyncAt(now);
            case WILDBERRIES -> tenant.setWildberriesLastSyncAt(now);
        }
    }
}