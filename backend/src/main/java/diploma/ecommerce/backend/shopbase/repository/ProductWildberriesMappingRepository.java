package diploma.ecommerce.backend.shopbase.repository;

import diploma.ecommerce.backend.shopbase.model.ProductWildberriesMapping;
import diploma.ecommerce.backend.shopbase.model.ProductWildberriesMapping.SyncStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductWildberriesMappingRepository extends JpaRepository<ProductWildberriesMapping, Long> {

    Optional<ProductWildberriesMapping> findByTenantIdAndId(Long tenantId, Long id);

    Optional<ProductWildberriesMapping> findByTenantIdAndProductId(Long tenantId, Long productId);

    Optional<ProductWildberriesMapping> findByTenantIdAndWbNmId(Long tenantId, Long wbNmId);

    List<ProductWildberriesMapping> findByTenantId(Long tenantId);

    List<ProductWildberriesMapping> findByTenantIdAndActive(Long tenantId, boolean active);

    List<ProductWildberriesMapping> findByTenantIdAndSyncStatus(Long tenantId, SyncStatus syncStatus);

    Page<ProductWildberriesMapping> findByTenantId(Long tenantId, Pageable pageable);

    boolean existsByTenantIdAndProductId(Long tenantId, Long productId);

    boolean existsByTenantIdAndWbNmId(Long tenantId, Long wbNmId);

    @Query("SELECT m FROM ProductWildberriesMapping m WHERE m.tenant.id = :tenantId AND m.active = true AND m.syncStatus != 'ERROR'")
    List<ProductWildberriesMapping> findActiveMappingsForSync(Long tenantId);
}