package diploma.ecommerce.backend.shopbase.repository;

import java.util.Optional;

import diploma.ecommerce.backend.shopbase.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    Page<Product> findAllByTenantId(Long tenantId, Pageable pageable);

    Page<Product> findByTenantIdAndActive(Long tenantId, boolean active, Pageable pageable);

    Optional<Product> findByTenantIdAndId(Long tenantId, Long id);

    Optional<Product> findByTenantIdAndName(Long tenantId, String name);

    boolean existsByTenantIdAndId(Long tenantId, Long id);

    void deleteByTenantIdAndId(Long tenantId, Long id);
}
