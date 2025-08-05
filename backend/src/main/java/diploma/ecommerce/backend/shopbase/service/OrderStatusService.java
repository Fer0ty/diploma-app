package diploma.ecommerce.backend.shopbase.service;

import java.util.List;
import java.util.Optional;

import diploma.ecommerce.backend.shopbase.model.OrderStatus;

public interface OrderStatusService {

    OrderStatus createOrderStatus(OrderStatus orderStatus);

    OrderStatus getOrderStatusById(Long id);

    Optional<OrderStatus> findOrderStatusByName(String name);

    List<OrderStatus> getAllOrderStatuses();

    OrderStatus updateOrderStatus(Long id, OrderStatus orderStatusDetails);

    void deleteOrderStatus(Long id);
}
