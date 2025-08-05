package diploma.ecommerce.backend.shopbase.repository;

import diploma.ecommerce.backend.shopbase.model.ProductOzonMapping;
import diploma.ecommerce.backend.shopbase.model.ProductOzonMapping.SyncStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductOzonMappingRepository extends JpaRepository<ProductOzonMapping, Long> {

    Optional<ProductOzonMapping> findByTenantIdAndId(Long tenantId, Long id);

    Optional<ProductOzonMapping> findByTenantIdAndProductId(Long tenantId, Long productId);

    Optional<ProductOzonMapping> findByTenantIdAndOzonProductId(Long tenantId, Long ozonProductId);

    List<ProductOzonMapping> findByTenantId(Long tenantId);

    List<ProductOzonMapping> findByTenantIdAndActive(Long tenantId, boolean active);

    List<ProductOzonMapping> findByTenantIdAndSyncStatus(Long tenantId, SyncStatus syncStatus);

    Page<ProductOzonMapping> findByTenantId(Long tenantId, Pageable pageable);

    boolean existsByTenantIdAndProductId(Long tenantId, Long productId);

    boolean existsByTenantIdAndOzonProductId(Long tenantId, Long ozonProductId);

    @Query("SELECT m FROM ProductOzonMapping m WHERE m.tenant.id = :tenantId AND m.active = true AND m.syncStatus != 'ERROR'")
    List<ProductOzonMapping> findActiveMappingsForSync(Long tenantId);
}