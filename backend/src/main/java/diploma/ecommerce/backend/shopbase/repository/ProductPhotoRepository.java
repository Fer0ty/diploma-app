package diploma.ecommerce.backend.shopbase.repository;

import java.util.List;
import java.util.Optional;

import diploma.ecommerce.backend.shopbase.model.ProductPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductPhotoRepository extends JpaRepository<ProductPhoto, Long> {

    Optional<ProductPhoto> findByTenantIdAndId(Long tenantId, Long id);

    List<ProductPhoto> findByTenantIdAndProductIdOrderByDisplayOrderAsc(Long tenantId, Long productId);

    Optional<ProductPhoto> findByTenantIdAndProductIdAndMainTrue(Long tenantId, Long productId);

    List<ProductPhoto> findAllByTenantId(Long tenantId);

    Optional<ProductPhoto> findByTenantIdAndProductIdAndId(Long tenantId, Long productId, Long photoId);

    List<ProductPhoto> findByTenantIdAndProductIdAndIdNotOrderByDisplayOrderAsc(
            Long tenantId,
            Long productId,
            Long photoIdToExclude
    );

    boolean existsByTenantIdAndProductId(Long tenantId, Long productId);

    boolean existsByTenantIdAndId(Long tenantId, Long id);
}
