package diploma.ecommerce.backend.shopbase.service;

import java.util.List;

import diploma.ecommerce.backend.shopbase.model.OrderItem;

public interface OrderItemService {
    OrderItem getOrderItem(Long tenantId, Long id);

    List<OrderItem> getOrderItemsByOrderId(Long tenantId, Long orderId);

    OrderItem createOrderItem(Long tenantId, Long orderId, OrderItem orderItem);

    OrderItem updateOrderItem(Long tenantId, Long id, OrderItem orderItemDetails);

    void deleteOrderItem(Long tenantId, Long id);
}
