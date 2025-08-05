package diploma.ecommerce.backend.shopbase.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import diploma.ecommerce.backend.shopbase.exception.BadRequestException;
import diploma.ecommerce.backend.shopbase.exception.InsufficientStockException;
import diploma.ecommerce.backend.shopbase.exception.ResourceNotFoundException;
import diploma.ecommerce.backend.shopbase.model.Address;
import diploma.ecommerce.backend.shopbase.model.Order;
import diploma.ecommerce.backend.shopbase.model.OrderItem;
import diploma.ecommerce.backend.shopbase.model.OrderStatus;
import diploma.ecommerce.backend.shopbase.model.Product;
import diploma.ecommerce.backend.shopbase.model.Tenant;
import diploma.ecommerce.backend.shopbase.model.User;
import diploma.ecommerce.backend.shopbase.repository.AddressRepository;
import diploma.ecommerce.backend.shopbase.repository.OrderRepository;
import diploma.ecommerce.backend.shopbase.repository.OrderStatusRepository;
import diploma.ecommerce.backend.shopbase.repository.ProductRepository;
import diploma.ecommerce.backend.shopbase.repository.TenantRepository;
import diploma.ecommerce.backend.shopbase.repository.UserRepository;
import diploma.ecommerce.backend.shopbase.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final ProductRepository productRepository;
    private final OrderStatusRepository orderStatusRepository;
    private final TenantRepository tenantRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<Order> getAllOrders(Long tenantId, Pageable pageable) {
        if (!tenantRepository.existsById(tenantId)) {
            throw new ResourceNotFoundException("Tenant", "id", tenantId);
        }
        return orderRepository.findAllByTenantId(tenantId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Order getOrderById(Long tenantId, Long id) {
        Order order = orderRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id, tenantId));
        log.debug(
                "Fetched order {} for tenant {}. Items count: {}",
                id,
                tenantId,
                order.getOrderItems().size()
        );
        return order;
    }

    @Override
    @Transactional
    public Order createOrder(Long tenantId, Order orderRequest) {
        log.info("Attempting to create order for tenant {}", tenantId);
        if (orderRequest.getCustomer() == null || orderRequest.getCustomer().getId() == null) {
            throw new IllegalArgumentException("Customer ID must be provided");
        }
        if (orderRequest.getAddress() == null || orderRequest.getAddress().getId() == null) {
            throw new IllegalArgumentException("Address ID must be provided");
        }
        if (CollectionUtils.isEmpty(orderRequest.getOrderItems())) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }
        Long customerId = orderRequest.getCustomer().getId();
        Long addressId = orderRequest.getAddress().getId();

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant", "id", tenantId));
        User customer = userRepository.findByTenantIdAndId(tenantId, customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", customerId, tenantId));
        Address address = addressRepository.findByTenantIdAndId(tenantId, addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", addressId, tenantId));
        OrderStatus createdStatus = orderStatusRepository.findByStatusName("Created")
                .orElseThrow(() -> new ResourceNotFoundException("OrderStatus", "name", "Created"));

        Order newOrder = new Order();
        newOrder.setTenant(tenant);
        newOrder.setCustomer(customer);
        newOrder.setAddress(address);
        newOrder.setStatus(createdStatus);
        newOrder.setTotalAmount(BigDecimal.ZERO);
        newOrder.setOrderItems(new ArrayList<>());

        BigDecimal calculatedTotalAmount = BigDecimal.ZERO;

        for (OrderItem itemRequest : orderRequest.getOrderItems()) {
            if (itemRequest.getProduct() == null || itemRequest.getProduct().getId() == null) {
                throw new IllegalArgumentException("Product ID missing in one of the order items");
            }
            if (itemRequest.getQuantity() == null || itemRequest.getQuantity() <= 0) {
                throw new IllegalArgumentException("Invalid quantity for product ID "
                                                           + itemRequest.getProduct().getId()
                );
            }
            Long productId = itemRequest.getProduct().getId();
            int requestedQuantity = itemRequest.getQuantity();

            Product product = productRepository.findByTenantIdAndId(tenantId, productId)
                    .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId, tenantId));

            if (product.getStockQuantity() < requestedQuantity) {
                log.warn(
                        "Insufficient stock for product {} (tenant {}). Requested: {}, Available: {}",
                        productId,
                        tenantId,
                        requestedQuantity,
                        product.getStockQuantity()
                );
                throw new InsufficientStockException(productId, requestedQuantity, product.getStockQuantity());
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setTenant(tenant);
            orderItem.setProduct(product);
            orderItem.setQuantity(requestedQuantity);
            orderItem.setUnitPrice(product.getPrice());
            orderItem.setTotalPrice(product.getPrice().multiply(BigDecimal.valueOf(requestedQuantity)));

            newOrder.addOrderItem(orderItem);

            product.setStockQuantity(product.getStockQuantity() - requestedQuantity);
            productRepository.save(product);

            calculatedTotalAmount = calculatedTotalAmount.add(orderItem.getTotalPrice());
            log.debug(
                    "Processed item: product={}, quantity={}, price={}, totalItemPrice={}",
                    productId,
                    requestedQuantity,
                    orderItem.getUnitPrice(),
                    orderItem.getTotalPrice()
            );
        }

        newOrder.setTotalAmount(calculatedTotalAmount);
        Order savedOrder = orderRepository.save(newOrder);
        log.info("Order {} created successfully for tenant {}", savedOrder.getId(), tenantId);

        return savedOrder;
    }

    @Override
    @Transactional
    public Order updateOrderStatus(Long tenantId, Long id, Long statusId, String comment) {
        log.info("Attempting to update status for order {} (tenant {}) to statusId {}", id, tenantId, statusId);
        Order order = orderRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id, tenantId));

        OrderStatus newStatus = orderStatusRepository.findById(statusId)
                .orElseThrow(() -> new ResourceNotFoundException("OrderStatus", "id", statusId));

        OrderStatus oldStatus = order.getStatus();

        order.setStatus(newStatus);
        if (comment != null && !comment.isBlank()) {
            String existingComment = order.getComment() == null ? "" : order.getComment() + "\n";
            order.setComment(existingComment + "Status changed to " + newStatus.getStatusName() + ": " + comment);
        }

        if (("Canceled".equals(newStatus.getStatusName()) || "Returned".equals(newStatus.getStatusName())) &&
                !"Canceled".equals(oldStatus.getStatusName()) &&
                !"Returned".equals(oldStatus.getStatusName())) {
            log.info("Order {} is being canceled or returned, restoring inventory", order.getId());
            restoreInventoryForOrder(order);
        }

        Order updatedOrder = orderRepository.save(order);
        log.info("Order {} (tenant {}) status updated successfully to {}", id, tenantId, newStatus.getStatusName());
        return updatedOrder;
    }

    @Override
    @Transactional
    public void deleteOrder(Long tenantId, Long id) {
        log.warn("Attempting to delete order {} for tenant {}", id, tenantId);
        Order order = orderRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id, tenantId));

        String statusName = order.getStatus().getStatusName();

        if (!"Canceled".equals(statusName) && !"Returned".equals(statusName)) {
            log.error(
                    "Attempted to delete order {} (tenant {}) with status {}",
                    id,
                    tenantId,
                    statusName
            );
            throw new BadRequestException("Cannot delete order with status: " + statusName);
        }

        orderRepository.delete(order);
        log.warn("Order {} for tenant {} deleted.", id, tenantId);
    }

    @Override
    @Transactional
    public Order cancelOrder(Long tenantId, Long id, String reason) {
        Order order = getOrderById(tenantId, id);

        OrderStatus canceledStatus = orderStatusRepository.findByStatusName("Canceled")
                .orElseThrow(() -> new ResourceNotFoundException("OrderStatus", "name", "Canceled"));

        String currentStatus = order.getStatus().getStatusName();
        if ("Delivered".equals(currentStatus) || "Completed".equals(currentStatus)) {
            throw new BadRequestException("Cannot cancel order that is already " + currentStatus);
        }

        return updateOrderStatus(tenantId, id, canceledStatus.getId(), reason);
    }

    @Override
    @Transactional
    public Order processOrderPayment(Long tenantId, Long id, String paymentReference) {
        Order order = getOrderById(tenantId, id);

        if (!"Created".equals(order.getStatus().getStatusName())) {
            throw new BadRequestException("Payment can only be processed for orders in 'Created' status");
        }

        OrderStatus paidStatus = orderStatusRepository.findByStatusName("Paid")
                .orElseThrow(() -> new ResourceNotFoundException("OrderStatus", "name", "Paid"));

        String comment = "Payment processed successfully. Reference: " + paymentReference;
        return updateOrderStatus(tenantId, id, paidStatus.getId(), comment);
    }


    private void restoreInventoryForOrder(Order order) {
        if (order == null || CollectionUtils.isEmpty(order.getOrderItems())) {
            return;
        }

        log.info(
                "Restoring inventory for order {} (tenant {})",
                order.getId(),
                order.getTenant().getId()
        );

        List<Product> productsToUpdate = new ArrayList<>();

        for (OrderItem item : order.getOrderItems()) {
            Product product = item.getProduct();
            if (product != null) {
                int quantityToRestore = item.getQuantity();
                product.setStockQuantity(product.getStockQuantity() + quantityToRestore);
                productsToUpdate.add(product);
                log.debug("Restored {} units to inventory for product {}", quantityToRestore, product.getId());
            }
        }
        if (!productsToUpdate.isEmpty()) {
            productRepository.saveAll(productsToUpdate);
        }
    }


}
