package diploma.ecommerce.backend.shopbase.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MarketplaceSyncService {

    void syncProducts(Long tenantId, MarketplaceAdapter.MarketplaceType type);

    void syncSingleProduct(Long tenantId, Long productId, MarketplaceAdapter.MarketplaceType type);

    Page<?> getMappings(Long tenantId, MarketplaceAdapter.MarketplaceType type, Pageable pageable);

    void enableSync(Long tenantId, MarketplaceAdapter.MarketplaceType type, boolean enabled);

    boolean testConnection(Long tenantId, MarketplaceAdapter.MarketplaceType type);
}