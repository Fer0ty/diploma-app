package diploma.ecommerce.backend.shopbase.repository;

import java.util.List;
import java.util.Optional;

import diploma.ecommerce.backend.shopbase.model.Address;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {

    Optional<Address> findByTenantIdAndId(Long tenantId, Long id);

    List<Address> findAllByTenantId(Long tenantId);

    Page<Address> findAllByTenantId(Long tenantId, Pageable pageable);

    boolean existsByTenantIdAndId(Long tenantId, Long id);
}
