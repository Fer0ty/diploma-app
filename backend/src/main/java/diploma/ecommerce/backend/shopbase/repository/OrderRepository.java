package diploma.ecommerce.backend.shopbase.repository;

import java.util.List;
import java.util.Optional;

import diploma.ecommerce.backend.shopbase.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {
    Optional<Order> findByTenantIdAndId(Long tenantId, Long id);

    Page<Order> findAllByTenantId(Long tenantId, Pageable pageable);

    boolean existsByStatusId(Long statusId);

    boolean existsByTenantIdAndId(Long tenantId, Long id);

    boolean existsByTenantIdAndCustomerId(Long tenantId, Long customerId);

    boolean existsByTenantIdAndCustomerIdAndStatus_StatusNameNotIn(
            Long tenantId,
            Long customerId,
            List<String> statusNames
    );
}
