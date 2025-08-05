package diploma.ecommerce.backend.shopbase.repository;

import java.util.List;
import java.util.Optional;

import diploma.ecommerce.backend.shopbase.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    Optional<OrderItem> findByTenantIdAndId(Long tenantId, Long id);

    List<OrderItem> findByTenantIdAndOrderId(Long tenantId, Long orderId);

    List<OrderItem> findByTenantIdAndProductId(Long tenantId, Long productId);

    Optional<OrderItem> findByTenantIdAndOrderIdAndProductId(Long tenantId, Long orderId, Long productId);

    List<OrderItem> findAllByTenantId(Long tenantId);

    boolean existsByTenantIdAndId(Long tenantId, Long id);
}
