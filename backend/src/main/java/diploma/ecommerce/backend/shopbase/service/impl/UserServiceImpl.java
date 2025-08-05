package diploma.ecommerce.backend.shopbase.service.impl;

import diploma.ecommerce.backend.shopbase.exception.DataIntegrityViolationException;
import diploma.ecommerce.backend.shopbase.exception.ResourceNotFoundException;
import diploma.ecommerce.backend.shopbase.model.Tenant;
import diploma.ecommerce.backend.shopbase.model.User;
import diploma.ecommerce.backend.shopbase.repository.OrderRepository;
import diploma.ecommerce.backend.shopbase.repository.TenantRepository;
import diploma.ecommerce.backend.shopbase.repository.UserRepository;
import diploma.ecommerce.backend.shopbase.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service("customerUserService")
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final OrderRepository orderRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<User> getAllUsers(Long tenantId, Pageable pageable) {
        if (!tenantRepository.existsById(tenantId)) {
            throw new ResourceNotFoundException("Tenant", "id", tenantId);
        }
        return userRepository.findAllByTenantId(tenantId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserById(Long tenantId, Long id) {
        return userRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id, tenantId));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> getUserByEmail(Long tenantId, String email) {
        if (!tenantRepository.existsById(tenantId)) {
            log.warn("Attempted to find user by email for non-existent tenant {}", tenantId);
            return Optional.empty();
        }
        return userRepository.findByTenantIdAndEmail(tenantId, email);
    }

    @Override
    @Transactional
    public User createUser(Long tenantId, User user) {
        log.info("Creating user with email {} for tenant {}", user.getEmail(), tenantId);
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant", "id", tenantId));

        user.setTenant(tenant);
        user.setId(null);

        User savedUser = userRepository.save(user);
        log.info(
                "User {} created successfully with email {} for tenant {}",
                savedUser.getId(),
                savedUser.getEmail(),
                tenantId
        );
        return savedUser;
    }

    @Override
    @Transactional
    public User updateUser(Long tenantId, Long id, User userDetails) {
        log.info("Updating user {} for tenant {}", id, tenantId);
        User existingUser = userRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id, tenantId));

        String newEmail = userDetails.getEmail();
        if (newEmail != null && !newEmail.equalsIgnoreCase(existingUser.getEmail())) {
            log.debug("Email change detected for user {}. Old: {}, New: {}", id, existingUser.getEmail(), newEmail);
            existingUser.setEmail(newEmail);
        }

        existingUser.setFirstName(userDetails.getFirstName());
        existingUser.setLastName(userDetails.getLastName());
        existingUser.setPatronymic(userDetails.getPatronymic());
        existingUser.setPhone(userDetails.getPhone());

        User updatedUser = userRepository.save(existingUser);
        log.info("User {} updated successfully for tenant {}", id, tenantId);
        return updatedUser;
    }

    @Override
    @Transactional
    public void deleteUser(Long tenantId, Long id) {
        log.warn("Attempting to delete user {} for tenant {}", id, tenantId);

        User userToDelete = userRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id, tenantId));

        boolean hasOrders = orderRepository.existsByTenantIdAndCustomerId(tenantId, id);

        if (hasOrders) {
            log.error("Cannot delete user {} (tenant {}): user has existing orders.", id, tenantId);
            throw new DataIntegrityViolationException(
                    "Cannot delete user with ID " + id + " because they have associated orders. Consider deactivating" +
                            " the user instead."
            );
        }

        userRepository.delete(userToDelete);

        log.info("User {} deleted successfully for tenant {}", id, tenantId);
    }

    @Override
    @Transactional
    public User activateUser(Long tenantId, Long id) {
        User user = getUserById(tenantId, id);
        if (!user.isActive()) {
            user.setActive(true);
            log.info("Activating user {} for tenant {}", id, tenantId);
            return userRepository.save(user);
        }
        log.debug("User {} for tenant {} is already active.", id, tenantId);
        return user;
    }

    @Override
    @Transactional
    public User deactivateUser(Long tenantId, Long id) {
        User user = getUserById(tenantId, id);

        if (orderRepository.existsByTenantIdAndCustomerIdAndStatus_StatusNameNotIn(
                tenantId, id, List.of
                        ("Delivered", "Canceled", "Returned")
        )) {
            throw new IllegalArgumentException("Cannot deactivate user with active orders.");
        }

        if (user.isActive()) {
            user.setActive(false);
            log.warn("Deactivating user {} for tenant {}", id, tenantId);
            return userRepository.save(user);
        }
        log.debug("User {} for tenant {} is already inactive.", id, tenantId);
        return user;
    }
}
