package diploma.ecommerce.backend.shopbase.service;

import diploma.ecommerce.backend.shopbase.model.ProductWildberriesMapping;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface WildberriesSyncService {

    ProductWildberriesMapping createMapping(Long tenantId, Long productId, Long wbNmId, String wbSku);

    ProductWildberriesMapping updateMapping(Long tenantId, Long mappingId, ProductWildberriesMapping mappingDetails);

    void deleteMapping(Long tenantId, Long mappingId);

    Page<ProductWildberriesMapping> getMappings(Long tenantId, Pageable pageable);

    ProductWildberriesMapping getMapping(Long tenantId, Long mappingId);
}