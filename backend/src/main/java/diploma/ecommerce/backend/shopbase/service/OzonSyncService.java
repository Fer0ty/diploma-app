package diploma.ecommerce.backend.shopbase.service;

import diploma.ecommerce.backend.shopbase.model.ProductOzonMapping;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OzonSyncService {

    void syncProductsForTenant(Long tenantId);

    void syncSingleProduct(Long tenantId, Long productId);

    ProductOzonMapping createMapping(Long tenantId, Long productId, Long ozonProductId, Long ozonSku);

    ProductOzonMapping updateMapping(Long tenantId, Long mappingId, ProductOzonMapping mappingDetails);

    void deleteMapping(Long tenantId, Long mappingId);

    Page<ProductOzonMapping> getMappings(Long tenantId, Pageable pageable);

    ProductOzonMapping getMapping(Long tenantId, Long mappingId);

    void enableOzonSync(Long tenantId, boolean enabled);

    boolean testOzonConnection(Long tenantId);
}