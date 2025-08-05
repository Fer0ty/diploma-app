package diploma.ecommerce.backend.shopbase.repository;

import java.util.Optional;

import diploma.ecommerce.backend.shopbase.model.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, Long> {

    Optional<Tenant> findBySubdomain(String subdomain);

    boolean existsByName(String name);

    boolean existsBySubdomain(String subdomain);
}
