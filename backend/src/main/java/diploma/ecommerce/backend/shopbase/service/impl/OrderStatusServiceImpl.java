package diploma.ecommerce.backend.shopbase.service.impl;

import java.util.List;
import java.util.Optional;

import diploma.ecommerce.backend.shopbase.exception.DataIntegrityViolationException;
import diploma.ecommerce.backend.shopbase.exception.ResourceNotFoundException;
import diploma.ecommerce.backend.shopbase.exception.StatusNameAlreadyExistsException;
import diploma.ecommerce.backend.shopbase.model.OrderStatus;
import diploma.ecommerce.backend.shopbase.repository.OrderRepository;
import diploma.ecommerce.backend.shopbase.repository.OrderStatusRepository;
import diploma.ecommerce.backend.shopbase.service.OrderStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderStatusServiceImpl implements OrderStatusService {


    private final OrderStatusRepository orderStatusRepository;
    private final OrderRepository orderRepository;

    @Override
    @Transactional
    public OrderStatus createOrderStatus(OrderStatus orderStatus) {
        if (orderStatus == null || !StringUtils.hasText(orderStatus.getStatusName())) {
            throw new IllegalArgumentException("Order status name cannot be null or empty");
        }
        if (orderStatus.getId() != null) {
            log.warn("Attempted to create OrderStatus with existing ID {}. ID will be ignored.", orderStatus.getId());
            orderStatus.setId(null);
        }

        log.info("Attempting to create OrderStatus with name: {}", orderStatus.getStatusName());

        if (orderStatusRepository.existsByStatusName(orderStatus.getStatusName())) {
            log.warn("OrderStatus creation failed: Name '{}' already exists.", orderStatus.getStatusName());
            throw new StatusNameAlreadyExistsException("Order status with name '" + orderStatus.getStatusName() + "' " +
                                                               "already exists.");
        }

        OrderStatus savedStatus = orderStatusRepository.save(orderStatus);
        log.info(
                "OrderStatus {} created successfully with name '{}'.",
                savedStatus.getId(),
                savedStatus.getStatusName()
        );
        return savedStatus;
    }

    @Override
    @Transactional(readOnly = true)
    public OrderStatus getOrderStatusById(Long id) {
        log.debug("Fetching OrderStatus by ID: {}", id);
        return orderStatusRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("OrderStatus not found with ID: {}", id);
                    return new ResourceNotFoundException("OrderStatus", "id", id);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<OrderStatus> findOrderStatusByName(String name) {
        log.debug("Fetching OrderStatus by name: {}", name);
        return orderStatusRepository.findByStatusName(name);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderStatus> getAllOrderStatuses() {
        log.debug("Fetching all OrderStatuses.");
        return orderStatusRepository.findAll();
    }

    @Override
    @Transactional
    public OrderStatus updateOrderStatus(Long id, OrderStatus orderStatusDetails) {
        if (orderStatusDetails == null || !StringUtils.hasText(orderStatusDetails.getStatusName())) {
            throw new IllegalArgumentException("New order status name cannot be null or empty");
        }
        String newName = orderStatusDetails.getStatusName();
        log.info("Attempting to update OrderStatus {} with new name: {}", id, newName);

        OrderStatus existingStatus = getOrderStatusById(id);

        if (newName.equals(existingStatus.getStatusName())) {
            log.info("OrderStatus {} name ('{}') hasn't changed. No update needed.", id, newName);
            return existingStatus;
        }

        if (orderStatusRepository.existsByStatusNameAndIdNot(newName, id)) {
            log.warn("OrderStatus update failed: New name '{}' already exists for another status.", newName);
            throw new StatusNameAlreadyExistsException("Order status with name '" + newName + "' already exists.");
        }

        existingStatus.setStatusName(newName);

        OrderStatus updatedStatus = orderStatusRepository.save(existingStatus);
        log.info("OrderStatus {} updated successfully with new name '{}'.", id, newName);
        return updatedStatus;
    }

    @Override
    @Transactional
    public void deleteOrderStatus(Long id) {
        log.warn("Attempting to delete OrderStatus with ID: {}", id);

        if (!orderStatusRepository.existsById(id)) {
            log.warn("OrderStatus deletion failed: Status with ID {} not found.", id);
            throw new ResourceNotFoundException("OrderStatus", "id", id);
        }

        if (orderRepository.existsByStatusId(id)) {
            log.error("OrderStatus deletion failed: Status with ID {} is currently in use by orders.", id);
            throw new DataIntegrityViolationException("Cannot delete OrderStatus with ID " + id + " because it is " +
                                                              "associated with existing orders.");
        }


        orderStatusRepository.deleteById(id);
        log.info("OrderStatus {} deleted successfully.", id);
    }

    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        return orderStatusRepository.existsById(id);
    }

}

