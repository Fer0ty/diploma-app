package diploma.ecommerce.backend.shopbase.service.impl;

import diploma.ecommerce.backend.shopbase.model.Product;
import diploma.ecommerce.backend.shopbase.model.ProductOzonMapping;
import diploma.ecommerce.backend.shopbase.model.ProductWildberriesMapping;
import diploma.ecommerce.backend.shopbase.model.Tenant;
import diploma.ecommerce.backend.shopbase.repository.ProductOzonMappingRepository;
import diploma.ecommerce.backend.shopbase.repository.ProductWildberriesMappingRepository;
import diploma.ecommerce.backend.shopbase.repository.TenantRepository;
import diploma.ecommerce.backend.shopbase.service.CryptoService;
import diploma.ecommerce.backend.shopbase.service.OzonApiService;
import diploma.ecommerce.backend.shopbase.service.UnifiedMarketplaceSyncService;
import diploma.ecommerce.backend.shopbase.service.WildberriesApiService;
import diploma.ecommerce.backend.shopbase.service.marketplace.MarketplaceAdapter;
import diploma.ecommerce.backend.shopbase.service.marketplace.MarketplaceAdapter.MarketplaceType;
import diploma.ecommerce.backend.shopbase.service.marketplace.MarketplaceAdapter.PriceUpdate;
import diploma.ecommerce.backend.shopbase.service.marketplace.MarketplaceAdapter.StockUpdate;
import diploma.ecommerce.backend.shopbase.service.marketplace.impl.OzonMarketplaceAdapter;
import diploma.ecommerce.backend.shopbase.service.marketplace.impl.WildberriesMarketplaceAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UnifiedMarketplaceSyncServiceImpl implements UnifiedMarketplaceSyncService {

    private final TenantRepository tenantRepository;
    private final ProductOzonMappingRepository ozonMappingRepository;
    private final ProductWildberriesMappingRepository wbMappingRepository;
    private final OzonApiService ozonApiService;
    private final WildberriesApiService wildberriesApiService;
    private final CryptoService cryptoService;

    @Override
    @Transactional
    public void syncProducts(Long tenantId, MarketplaceType marketplaceType) {
        log.info("Starting {} sync for tenant {}", marketplaceType, tenantId);

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));

        MarketplaceAdapter adapter = createAdapter(tenant, marketplaceType);

        if (adapter == null) {
            log.warn("No adapter configured for {} in tenant {}", marketplaceType, tenantId);
            return;
        }

        switch (marketplaceType) {
            case OZON -> syncOzonProducts(tenant, adapter);
            case WILDBERRIES -> syncWildberriesProducts(tenant, adapter);
        }
    }

    @Override
    @Transactional
    public void syncSingleProduct(Long tenantId, Long productId, MarketplaceType marketplaceType) {
        log.info("Syncing single product {} for {} in tenant {}", productId, marketplaceType, tenantId);

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));

        MarketplaceAdapter adapter = createAdapter(tenant, marketplaceType);

        if (adapter == null) {
            log.warn("No adapter configured for {} in tenant {}", marketplaceType, tenantId);
            return;
        }

        switch (marketplaceType) {
            case OZON -> {
                ProductOzonMapping mapping = ozonMappingRepository
                        .findByTenantIdAndProductId(tenantId, productId)
                        .orElseThrow(() -> new RuntimeException("Ozon mapping not found"));
                syncSingleOzonProduct(mapping, adapter);
            }
            case WILDBERRIES -> {
                ProductWildberriesMapping mapping = wbMappingRepository
                        .findByTenantIdAndProductId(tenantId, productId)
                        .orElseThrow(() -> new RuntimeException("Wildberries mapping not found"));
                syncSingleWildberriesProduct(mapping, adapter);
            }
        }
    }

    @Override
    @Transactional
    public void syncAllMarketplaces(Long tenantId) {
        log.info("Syncing all marketplaces for tenant {}", tenantId);

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));

        if (tenant.isOzonSyncEnabled()) {
            try {
                syncProducts(tenantId, MarketplaceType.OZON);
            } catch (Exception e) {
                log.error("Error syncing Ozon for tenant {}: ", tenantId, e);
            }
        }

        if (tenant.isWildberriesSyncEnabled()) {
            try {
                syncProducts(tenantId, MarketplaceType.WILDBERRIES);
            } catch (Exception e) {
                log.error("Error syncing Wildberries for tenant {}: ", tenantId, e);
            }
        }
    }

    private MarketplaceAdapter createAdapter(Tenant tenant, MarketplaceType type) {
        return switch (type) {
            case OZON -> {
                if (tenant.getOzonApiKey() != null && tenant.getOzonClientId() != null) {
                    String apiKey = cryptoService.decrypt(tenant.getOzonApiKey());
                    String clientId = cryptoService.decrypt(tenant.getOzonClientId());
                    yield new OzonMarketplaceAdapter(ozonApiService, clientId, apiKey);
                }
                yield null;
            }
            case WILDBERRIES -> {
                if (tenant.getWildberriesApiKey() != null) {
                    String apiKey = cryptoService.decrypt(tenant.getWildberriesApiKey());
                    yield new WildberriesMarketplaceAdapter(wildberriesApiService, apiKey);
                }
                yield null;
            }
        };
    }

    private void syncOzonProducts(Tenant tenant, MarketplaceAdapter adapter) {
        List<ProductOzonMapping> mappings = ozonMappingRepository
                .findActiveMappingsForSync(tenant.getId());

        if (mappings.isEmpty()) {
            log.info("No active Ozon mappings for tenant {}", tenant.getId());
            return;
        }

        List<StockUpdate> stockUpdates = new ArrayList<>();
        List<PriceUpdate> priceUpdates = new ArrayList<>();

        for (ProductOzonMapping mapping : mappings) {
            Product product = mapping.getProduct();

            stockUpdates.add(new StockUpdate(
                    mapping.getOzonProductId().toString(),
                    product.getStockQuantity(),
                    mapping.getWarehouseId() != null ? mapping.getWarehouseId() : tenant.getOzonWarehouseId()
            ));

            priceUpdates.add(new PriceUpdate(
                    mapping.getOzonProductId().toString(),
                    product.getPrice().toString(),
                    product.getPrice().toString()
            ));
        }

        try {
            adapter.updateStocks(stockUpdates);
            adapter.updatePrices(priceUpdates);

            // Обновляем статус
            mappings.forEach(m -> {
                m.setSyncStatus(ProductOzonMapping.SyncStatus.SYNCED);
                m.setSyncError(null);
            });
            ozonMappingRepository.saveAll(mappings);

        } catch (Exception e) {
            log.error("Error syncing Ozon products: ", e);
            mappings.forEach(m -> {
                m.setSyncStatus(ProductOzonMapping.SyncStatus.ERROR);
                m.setSyncError(e.getMessage());
            });
            ozonMappingRepository.saveAll(mappings);
        }
    }

    private void syncWildberriesProducts(Tenant tenant, MarketplaceAdapter adapter) {
        List<ProductWildberriesMapping> mappings = wbMappingRepository
                .findActiveMappingsForSync(tenant.getId());

        if (mappings.isEmpty()) {
            log.info("No active Wildberries mappings for tenant {}", tenant.getId());
            return;
        }

        List<StockUpdate> stockUpdates = new ArrayList<>();
        List<PriceUpdate> priceUpdates = new ArrayList<>();

        for (ProductWildberriesMapping mapping : mappings) {
            Product product = mapping.getProduct();

            stockUpdates.add(new StockUpdate(
                    mapping.getWbNmId().toString(),
                    product.getStockQuantity(),
                    mapping.getWarehouseId() != null ? mapping.getWarehouseId() : tenant.getWildberriesWarehouseId()
            ));

            priceUpdates.add(new PriceUpdate(
                    mapping.getWbNmId().toString(),
                    product.getPrice().toString(),
                    product.getPrice().toString()
            ));
        }

        try {
            adapter.updateStocks(stockUpdates);
            adapter.updatePrices(priceUpdates);

            // Обновляем статус
            mappings.forEach(m -> {
                m.setSyncStatus(ProductWildberriesMapping.SyncStatus.SYNCED);
                m.setSyncError(null);
            });
            wbMappingRepository.saveAll(mappings);

        } catch (Exception e) {
            log.error("Error syncing Wildberries products: ", e);
            mappings.forEach(m -> {
                m.setSyncStatus(ProductWildberriesMapping.SyncStatus.ERROR);
                m.setSyncError(e.getMessage());
            });
            wbMappingRepository.saveAll(mappings);
        }
    }

    private void syncSingleOzonProduct(ProductOzonMapping mapping, MarketplaceAdapter adapter) {
        Product product = mapping.getProduct();

        try {
            adapter.updateStocks(List.of(new StockUpdate(
                    mapping.getOzonProductId().toString(),
                    product.getStockQuantity(),
                    mapping.getWarehouseId()
            )));

            adapter.updatePrices(List.of(new PriceUpdate(
                    mapping.getOzonProductId().toString(),
                    product.getPrice().toString(),
                    product.getPrice().toString()
            )));

            mapping.setSyncStatus(ProductOzonMapping.SyncStatus.SYNCED);
            mapping.setSyncError(null);
            ozonMappingRepository.save(mapping);

        } catch (Exception e) {
            log.error("Error syncing single Ozon product: ", e);
            mapping.setSyncStatus(ProductOzonMapping.SyncStatus.ERROR);
            mapping.setSyncError(e.getMessage());
            ozonMappingRepository.save(mapping);
        }
    }

    private void syncSingleWildberriesProduct(ProductWildberriesMapping mapping, MarketplaceAdapter adapter) {
        Product product = mapping.getProduct();

        try {
            adapter.updateStocks(List.of(new StockUpdate(
                    mapping.getWbNmId().toString(),
                    product.getStockQuantity(),
                    mapping.getWarehouseId()
            )));

            adapter.updatePrices(List.of(new PriceUpdate(
                    mapping.getWbNmId().toString(),
                    product.getPrice().toString(),
                    product.getPrice().toString()
            )));

            mapping.setSyncStatus(ProductWildberriesMapping.SyncStatus.SYNCED);
            mapping.setSyncError(null);
            wbMappingRepository.save(mapping);

        } catch (Exception e) {
            log.error("Error syncing single Wildberries product: ", e);
            mapping.setSyncStatus(ProductWildberriesMapping.SyncStatus.ERROR);
            mapping.setSyncError(e.getMessage());
            wbMappingRepository.save(mapping);
        }
    }
}