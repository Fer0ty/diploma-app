package diploma.ecommerce.backend.shopbase.repository;

import java.util.List;
import java.util.Optional;

import diploma.ecommerce.backend.shopbase.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    Optional<User> findByTenantIdAndId(Long tenantId, Long id);

    Optional<User> findByTenantIdAndEmail(Long tenantId, String email);

    List<User> findAllByTenantId(Long tenantId);

    Page<User> findAllByTenantId(Long tenantId, Pageable pageable);

    boolean existsByTenantIdAndEmail(Long tenantId, String email);

    boolean existsByTenantIdAndId(Long tenantId, Long id);
}
