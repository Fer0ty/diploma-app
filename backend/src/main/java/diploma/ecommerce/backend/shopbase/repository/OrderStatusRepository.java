package diploma.ecommerce.backend.shopbase.repository;

import java.util.Optional;

import diploma.ecommerce.backend.shopbase.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderStatusRepository extends JpaRepository<OrderStatus, Long> {

    Optional<OrderStatus> findByStatusName(String statusName);

    boolean existsByStatusName(String statusName);

    boolean existsByStatusNameAndIdNot(String statusName, Long id);
}
