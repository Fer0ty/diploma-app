package diploma.ecommerce.backend.shopbase.listener;

import diploma.ecommerce.backend.shopbase.model.Product;
import diploma.ecommerce.backend.shopbase.multitenancy.TenantContext;
import diploma.ecommerce.backend.shopbase.service.UnifiedMarketplaceSyncService;
import diploma.ecommerce.backend.shopbase.service.marketplace.MarketplaceAdapter.MarketplaceType;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PostUpdate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductChangeListener {

    private final UnifiedMarketplaceSyncService syncService;

    @PostUpdate
    @PostPersist
    @Async
    public void onProductChanged(Product product) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return;
        }

        log.info("Product {} changed, triggering marketplace sync", product.getId());

        // Синхронизируем со всеми маркетплейсами где есть маппинг
        try {
            syncService.syncSingleProduct(tenantId, product.getId(), MarketplaceType.OZON);
        } catch (Exception e) {
            log.debug("No Ozon mapping for product {}", product.getId());
        }

        try {
            syncService.syncSingleProduct(tenantId, product.getId(), MarketplaceType.WILDBERRIES);
        } catch (Exception e) {
            log.debug("No Wildberries mapping for product {}", product.getId());
        }
    }
}