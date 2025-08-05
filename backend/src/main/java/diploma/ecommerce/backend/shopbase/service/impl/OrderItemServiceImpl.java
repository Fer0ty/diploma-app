package diploma.ecommerce.backend.shopbase.service.impl;

import java.math.BigDecimal;
import java.util.List;

import diploma.ecommerce.backend.shopbase.exception.ResourceNotFoundException;
import diploma.ecommerce.backend.shopbase.model.Order;
import diploma.ecommerce.backend.shopbase.model.OrderItem;
import diploma.ecommerce.backend.shopbase.model.Product;
import diploma.ecommerce.backend.shopbase.model.Tenant;
import diploma.ecommerce.backend.shopbase.repository.OrderItemRepository;
import diploma.ecommerce.backend.shopbase.repository.OrderRepository;
import diploma.ecommerce.backend.shopbase.repository.ProductRepository;
import diploma.ecommerce.backend.shopbase.repository.TenantRepository;
import diploma.ecommerce.backend.shopbase.service.OrderItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderItemServiceImpl implements OrderItemService {

    private final OrderItemRepository orderItemRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final TenantRepository tenantRepository;

    private static Product getProduct(OrderItem orderItemDetails, OrderItem existingOrderItem) {
        Product product = existingOrderItem.getProduct();

        int quantityDifference = orderItemDetails.getQuantity() - existingOrderItem.getQuantity();

        if (quantityDifference > 0 && product.getStockQuantity() < quantityDifference) {
            throw new IllegalArgumentException("Not enough product in stock. Available: " +
                                                       product.getStockQuantity() + ", additional needed: " + quantityDifference);
        }

        product.setStockQuantity(product.getStockQuantity() - quantityDifference);
        return product;
    }

    @Override
    @Transactional(readOnly = true)
    public OrderItem getOrderItem(Long tenantId, Long id) {
        return orderItemRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new ResourceNotFoundException("OrderItem", "id", id, tenantId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderItem> getOrderItemsByOrderId(Long tenantId, Long orderId) {
        if (!orderRepository.existsByTenantIdAndId(tenantId, orderId)) {
            throw new ResourceNotFoundException("Order", "id", orderId, tenantId);
        }
        return orderItemRepository.findByTenantIdAndOrderId(tenantId, orderId);
    }

    @Override
    @Transactional
    public OrderItem createOrderItem(Long tenantId, Long orderId, OrderItem orderItem) {
        if (orderItem.getProduct() == null || orderItem.getProduct().getId() == null) {
            throw new IllegalArgumentException("Product ID must be provided in OrderItem");
        }
        if (orderItem.getQuantity() == null || orderItem.getQuantity() <= 0) {
            throw new IllegalArgumentException("Valid quantity must be provided in OrderItem");
        }
        Long productId = orderItem.getProduct().getId();

        if (orderItemRepository.findByTenantIdAndOrderIdAndProductId(tenantId, orderId, productId).isPresent()) {
            throw new IllegalArgumentException("Product with ID " + productId
                                                       + " already exists in order with ID " + orderId);
        }

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant", "id", tenantId));
        Order order = orderRepository.findByTenantIdAndId(tenantId, orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId, tenantId));
        Product product = productRepository.findByTenantIdAndId(tenantId, productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId, tenantId));

        if (product.getStockQuantity() < orderItem.getQuantity()) {
            throw new IllegalArgumentException("Not enough product in stock. Available: " +
                                                       product.getStockQuantity() +
                                                       ", requested: " + orderItem.getQuantity());
        }

        product.setStockQuantity(product.getStockQuantity() - orderItem.getQuantity());
        productRepository.save(product);

        orderItem.setTenant(tenant);
        orderItem.setOrder(order);
        orderItem.setProduct(product);

        orderItem.setUnitPrice(product.getPrice());
        orderItem.setTotalPrice(
                product.getPrice().multiply(
                        BigDecimal.valueOf(orderItem.getQuantity())
                )
        );

        orderItem.setId(null);

        OrderItem savedOrderItem = orderItemRepository.save(orderItem);

        updateOrderTotalAmount(order);

        return savedOrderItem;
    }

    @Override
    @Transactional
    public OrderItem updateOrderItem(Long tenantId, Long id, OrderItem orderItemDetails) {
        if (orderItemDetails.getQuantity() == null || orderItemDetails.getQuantity() <= 0) {
            throw new IllegalArgumentException("Valid quantity must be provided");
        }

        OrderItem existingOrderItem = orderItemRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new ResourceNotFoundException("OrderItem", "id", id, tenantId));

        Product product = getProduct(orderItemDetails, existingOrderItem);
        productRepository.save(product);

        existingOrderItem.setQuantity(orderItemDetails.getQuantity());
        existingOrderItem.setTotalPrice(
                existingOrderItem.getUnitPrice().multiply(
                        BigDecimal.valueOf(existingOrderItem.getQuantity())
                )
        );

        OrderItem updatedOrderItem = orderItemRepository.save(existingOrderItem);

        updateOrderTotalAmount(existingOrderItem.getOrder());

        return updatedOrderItem;
    }

    @Override
    @Transactional
    public void deleteOrderItem(Long tenantId, Long id) {
        OrderItem orderItem = orderItemRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new ResourceNotFoundException("OrderItem", "id", id, tenantId));

        Product product = orderItem.getProduct();
        product.setStockQuantity(product.getStockQuantity() + orderItem.getQuantity());
        productRepository.save(product);

        Order order = orderItem.getOrder();

        orderItemRepository.delete(orderItem);

        updateOrderTotalAmount(order);
    }

    private void updateOrderTotalAmount(Order order) {
        List<OrderItem> items = orderItemRepository.findByTenantIdAndOrderId(
                order.getTenant().getId(), order.getId());

        BigDecimal totalAmount = items.stream()
                .map(OrderItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        order.setTotalAmount(totalAmount);
        orderRepository.save(order);
    }
}
