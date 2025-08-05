package diploma.ecommerce.backend.shopbase.service;

import diploma.ecommerce.backend.shopbase.service.marketplace.MarketplaceAdapter.MarketplaceType;

public interface UnifiedMarketplaceSyncService {

    void syncProducts(Long tenantId, MarketplaceType marketplaceType);

    void syncSingleProduct(Long tenantId, Long productId, MarketplaceType marketplaceType);

    void syncAllMarketplaces(Long tenantId);
}