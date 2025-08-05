package diploma.ecommerce.backend.shopbase.service.impl;

import diploma.ecommerce.backend.shopbase.exception.ResourceNotFoundException;
import diploma.ecommerce.backend.shopbase.model.Product;
import diploma.ecommerce.backend.shopbase.model.ProductWildberriesMapping;
import diploma.ecommerce.backend.shopbase.model.Tenant;
import diploma.ecommerce.backend.shopbase.repository.ProductRepository;
import diploma.ecommerce.backend.shopbase.repository.ProductWildberriesMappingRepository;
import diploma.ecommerce.backend.shopbase.repository.TenantRepository;
import diploma.ecommerce.backend.shopbase.service.WildberriesSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class WildberriesSyncServiceImpl implements WildberriesSyncService {

    private final ProductWildberriesMappingRepository mappingRepository;
    private final ProductRepository productRepository;
    private final TenantRepository tenantRepository;

    @Override
    @Transactional
    public ProductWildberriesMapping createMapping(Long tenantId, Long productId, Long wbNmId, String wbSku) {
        log.info("Creating Wildberries mapping for product {} in tenant {}", productId, tenantId);

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant", "id", tenantId));

        Product product = productRepository.findByTenantIdAndId(tenantId, productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId, tenantId));

        if (mappingRepository.existsByTenantIdAndProductId(tenantId, productId)) {
            throw new IllegalArgumentException("Product already mapped to Wildberries");
        }

        if (mappingRepository.existsByTenantIdAndWbNmId(tenantId, wbNmId)) {
            throw new IllegalArgumentException("Wildberries nmId already mapped to another product");
        }

        ProductWildberriesMapping mapping = new ProductWildberriesMapping();
        mapping.setTenant(tenant);
        mapping.setProduct(product);
        mapping.setWbNmId(wbNmId);
        mapping.setWbSku(wbSku);
        mapping.setSyncStatus(ProductWildberriesMapping.SyncStatus.PENDING);
        mapping.setActive(true);

        return mappingRepository.save(mapping);
    }

    @Override
    @Transactional
    public ProductWildberriesMapping updateMapping(Long tenantId, Long mappingId, ProductWildberriesMapping mappingDetails) {
        log.info("Updating Wildberries mapping {} for tenant {}", mappingId, tenantId);

        ProductWildberriesMapping existingMapping = mappingRepository.findByTenantIdAndId(tenantId, mappingId)
                .orElseThrow(() -> new ResourceNotFoundException("ProductWildberriesMapping", "id", mappingId, tenantId));

        if (mappingDetails.getWbNmId() != null) {
            existingMapping.setWbNmId(mappingDetails.getWbNmId());
        }
        if (mappingDetails.getWbSku() != null) {
            existingMapping.setWbSku(mappingDetails.getWbSku());
        }
        if (mappingDetails.getWbBarcode() != null) {
            existingMapping.setWbBarcode(mappingDetails.getWbBarcode());
        }
        if (mappingDetails.getWbImId() != null) {
            existingMapping.setWbImId(mappingDetails.getWbImId());
        }
        if (mappingDetails.getActive() != null) {
            existingMapping.setActive(mappingDetails.getActive());
        }

        return mappingRepository.save(existingMapping);
    }

    @Override
    @Transactional
    public void deleteMapping(Long tenantId, Long mappingId) {
        log.warn("Deleting Wildberries mapping {} for tenant {}", mappingId, tenantId);

        ProductWildberriesMapping mapping = mappingRepository.findByTenantIdAndId(tenantId, mappingId)
                .orElseThrow(() -> new ResourceNotFoundException("ProductWildberriesMapping", "id", mappingId, tenantId));

        mappingRepository.delete(mapping);
        log.info("Wildberries mapping {} deleted for tenant {}", mappingId, tenantId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductWildberriesMapping> getMappings(Long tenantId, Pageable pageable) {
        if (!tenantRepository.existsById(tenantId)) {
            throw new ResourceNotFoundException("Tenant", "id", tenantId);
        }
        return mappingRepository.findByTenantId(tenantId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductWildberriesMapping getMapping(Long tenantId, Long mappingId) {
        return mappingRepository.findByTenantIdAndId(tenantId, mappingId)
                .orElseThrow(() -> new ResourceNotFoundException("ProductWildberriesMapping", "id", mappingId, tenantId));
    }
}