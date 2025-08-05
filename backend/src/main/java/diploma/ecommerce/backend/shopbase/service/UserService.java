package diploma.ecommerce.backend.shopbase.service;

import java.util.Optional;

import diploma.ecommerce.backend.shopbase.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {

    Page<User> getAllUsers(Long tenantId, Pageable pageable);

    User getUserById(Long tenantId, Long id);

    Optional<User> getUserByEmail(Long tenantId, String email);

    User createUser(Long tenantId, User user);

    User updateUser(Long tenantId, Long id, User userDetails);

    void deleteUser(Long tenantId, Long id);

    User activateUser(Long tenantId, Long id);

    User deactivateUser(Long tenantId, Long id);
}
