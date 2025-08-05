package diploma.ecommerce.backend.shopbase.service;

import diploma.ecommerce.backend.shopbase.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface OrderService {
    Page<Order> getAllOrders(Long tenantId, Pageable pageable);

    Order getOrderById(Long tenantId, Long id);

    Order createOrder(Long tenantId, Order orderRequest);

    Order updateOrderStatus(Long tenantId, Long id, Long statusId, String comment);

    void deleteOrder(Long tenantId, Long id);

    Order cancelOrder(Long tenantId, Long id, String reason);

    Order processOrderPayment(Long tenantId, Long id, String paymentReference);
}

