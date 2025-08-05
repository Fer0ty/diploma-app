package diploma.ecommerce.backend.shopbase.repository;

import java.util.List;
import java.util.Optional;

import diploma.ecommerce.backend.shopbase.model.TenantUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TenantUserRepository extends JpaRepository<TenantUser, Long> {

    Optional<TenantUser> findByTenantIdAndId(Long tenantId, Long id);

    Optional<TenantUser> findByTenantIdAndUsernameInTenant(Long tenantId, String usernameInTenant);

    List<TenantUser> findAllByTenantId(Long tenantId);

    Page<TenantUser> findAllByTenantId(Long tenantId, Pageable pageable);

    boolean existsByTenantIdAndId(Long tenantId, Long id);

    boolean existsByEmail(String email);

    Optional<TenantUser> findByUsernameInTenant(String usernameInTenant);

}
