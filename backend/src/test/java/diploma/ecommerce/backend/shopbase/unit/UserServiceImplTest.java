package diploma.ecommerce.backend.shopbase.unit;

import java.util.List;
import java.util.Optional;

import diploma.ecommerce.backend.shopbase.exception.DataIntegrityViolationException;
import diploma.ecommerce.backend.shopbase.exception.EmailAlreadyExistsException;
import diploma.ecommerce.backend.shopbase.exception.ResourceNotFoundException;
import diploma.ecommerce.backend.shopbase.model.Tenant;
import diploma.ecommerce.backend.shopbase.model.User;
import diploma.ecommerce.backend.shopbase.repository.OrderRepository;
import diploma.ecommerce.backend.shopbase.repository.TenantRepository;
import diploma.ecommerce.backend.shopbase.repository.UserRepository;
import diploma.ecommerce.backend.shopbase.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    private static final Long TENANT_ID = 1L;
    private static final Long USER_ID = 300L;
    private static final String USER_EMAIL = "test@example.com";
    private static final String NEW_USER_EMAIL = "new@example.com";
    @Captor
    ArgumentCaptor<User> userCaptor;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TenantRepository tenantRepository;
    @Mock
    private OrderRepository orderRepository;
    @InjectMocks
    @Qualifier("customerUserService")
    private UserServiceImpl userService;
    private Tenant tenant;
    private User user;
    private User userDetails;

    @BeforeEach
    void setUp() {
        tenant = new Tenant();
        tenant.setId(TENANT_ID);
        user = new User();
        user.setId(USER_ID);
        user.setTenant(tenant);
        user.setEmail(USER_EMAIL);
        user.setActive(true);
        user.setFirstName("Old");
        userDetails = new User();
        userDetails.setEmail(NEW_USER_EMAIL);
        userDetails.setFirstName("New");
        userDetails.setLastName("User");
        userDetails.setActive(true);
    }

    @Nested
    @DisplayName("Get User List/ById/ByEmail Tests")
    @MockitoSettings(strictness = Strictness.LENIENT)
    class GetUserTests {
        Pageable pageable = PageRequest.of(0, 10);

        @Test
        void getAllUsers_TenantExists_ReturnsPage() {/*...*/
            when(tenantRepository.existsById(TENANT_ID)).thenReturn(true);
            when(userRepository.findAllByTenantId(TENANT_ID, pageable)).thenReturn(new PageImpl<>(List.of(user)));
            Page<User> result = userService.getAllUsers(TENANT_ID, pageable);
            assertEquals(1, result.getTotalElements());
            verify(userRepository).findAllByTenantId(TENANT_ID, pageable);
        }

        @Test
        void getAllUsers_TenantNotFound_ThrowsException() {/*...*/
            when(tenantRepository.existsById(TENANT_ID)).thenReturn(false);
            assertThrows(ResourceNotFoundException.class, () -> userService.getAllUsers(TENANT_ID, pageable));
            verify(userRepository, never()).findAllByTenantId(anyLong(), any());
        }

        @Test
        void getUserById_Found_ReturnsUser() {/*...*/
            when(userRepository.findByTenantIdAndId(TENANT_ID, USER_ID)).thenReturn(Optional.of(user));
            User result = userService.getUserById(TENANT_ID, USER_ID);
            assertNotNull(result);
            assertEquals(USER_ID, result.getId());
            verify(userRepository).findByTenantIdAndId(TENANT_ID, USER_ID);
        }

        @Test
        void getUserById_NotFound_ThrowsException() {/*...*/
            when(userRepository.findByTenantIdAndId(TENANT_ID, USER_ID)).thenReturn(Optional.empty());
            assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(TENANT_ID, USER_ID));
        }

        @Test
        void getUserByEmail_Found_ReturnsOptionalUser() {/*...*/
            when(tenantRepository.existsById(TENANT_ID)).thenReturn(true);
            when(userRepository.findByTenantIdAndEmail(TENANT_ID, USER_EMAIL)).thenReturn(Optional.of(user));
            Optional<User> result = userService.getUserByEmail(TENANT_ID, USER_EMAIL);
            assertTrue(result.isPresent());
            assertEquals(user, result.get());
            verify(userRepository).findByTenantIdAndEmail(TENANT_ID, USER_EMAIL);
        }

        @Test
        void getUserByEmail_NotFound_ReturnsEmpty() {/*...*/
            when(tenantRepository.existsById(TENANT_ID)).thenReturn(true);
            when(userRepository.findByTenantIdAndEmail(TENANT_ID, USER_EMAIL)).thenReturn(Optional.empty());
            Optional<User> result = userService.getUserByEmail(TENANT_ID, USER_EMAIL);
            assertTrue(result.isEmpty());
        }

        @Test
        void getUserByEmail_TenantNotFound_ReturnsEmpty() {/*...*/
            when(tenantRepository.existsById(TENANT_ID)).thenReturn(false);
            Optional<User> result = userService.getUserByEmail(TENANT_ID, USER_EMAIL);
            assertTrue(result.isEmpty());
            verify(userRepository, never()).findByTenantIdAndEmail(anyLong(), anyString());
        }
    }

    @Nested
    @DisplayName("createUser Tests")
    class CreateUserTests {
        @Test
        void createUser_ValidNewEmail_ReturnsCreatedUser() {
            when(tenantRepository.findById(TENANT_ID)).thenReturn(Optional.of(tenant));
            when(userRepository.save(any(User.class))).thenAnswer(inv -> {
                User u = inv.getArgument(0);
                u.setId(USER_ID + 1);
                return u;
            });

            User result = userService.createUser(TENANT_ID, userDetails);

            assertNotNull(result);
            assertNotNull(result.getId());
            assertEquals(tenant, result.getTenant());
            verify(userRepository).save(userCaptor.capture());
            assertEquals(tenant, userCaptor.getValue().getTenant());
            verify(tenantRepository).findById(TENANT_ID);
        }

        @Test
        void createUser_TenantNotFound_ThrowsResourceNotFoundException() {
            when(tenantRepository.findById(TENANT_ID)).thenReturn(Optional.empty());
            assertThrows(ResourceNotFoundException.class, () -> userService.createUser(TENANT_ID, userDetails));
            verify(userRepository, never()).save(any(User.class));
        }
    }

    @Nested
    @DisplayName("updateUser Tests")
    class UpdateUserTests {
        @Test
        void updateUser_ValidNoEmailChange_ReturnsUpdatedUser() {
            userDetails.setEmail(USER_EMAIL);
            when(userRepository.findByTenantIdAndId(TENANT_ID, USER_ID)).thenReturn(Optional.of(user));
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            User result = userService.updateUser(TENANT_ID, USER_ID, userDetails);

            assertNotNull(result);
            assertEquals(USER_ID, result.getId());
            assertEquals(userDetails.getFirstName(), result.getFirstName());
            verify(userRepository, never()).existsByTenantIdAndEmail(anyLong(), anyString());
            verify(userRepository).save(userCaptor.capture());
            assertEquals(USER_ID, userCaptor.getValue().getId());
            assertEquals(userDetails.getFirstName(), userCaptor.getValue().getFirstName());
        }

        @Test
        void updateUser_ValidEmailChange_ReturnsUpdatedUser() {
            when(userRepository.findByTenantIdAndId(TENANT_ID, USER_ID)).thenReturn(Optional.of(user));
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            User result = userService.updateUser(TENANT_ID, USER_ID, userDetails);

            assertNotNull(result);
            assertEquals(NEW_USER_EMAIL, result.getEmail());
            verify(userRepository).save(userCaptor.capture());
            assertEquals(NEW_USER_EMAIL, userCaptor.getValue().getEmail());
        }

        @Test
        void updateUser_UserNotFound_ThrowsResourceNotFoundException() {
            when(userRepository.findByTenantIdAndId(TENANT_ID, USER_ID)).thenReturn(Optional.empty());
            assertThrows(
                    ResourceNotFoundException.class,
                    () -> userService.updateUser(TENANT_ID, USER_ID, userDetails)
            );
            verify(userRepository, never()).save(any(User.class));
        }
    }

    @Nested
    @DisplayName("deleteUser Tests")
    class DeleteUserTests {
        @Test
        void deleteUser_NoOrders_DeletesUser() {
            when(userRepository.findByTenantIdAndId(TENANT_ID, USER_ID)).thenReturn(Optional.of(user));
            when(orderRepository.existsByTenantIdAndCustomerId(TENANT_ID, USER_ID)).thenReturn(false);
            doNothing().when(userRepository).delete(any(User.class));

            userService.deleteUser(TENANT_ID, USER_ID);

            verify(userRepository).findByTenantIdAndId(TENANT_ID, USER_ID);
            verify(orderRepository).existsByTenantIdAndCustomerId(TENANT_ID, USER_ID);
            verify(userRepository).delete(user);
        }

        @Test
        void deleteUser_HasOrders_ThrowsDataIntegrityViolationException() {
            when(userRepository.findByTenantIdAndId(TENANT_ID, USER_ID)).thenReturn(Optional.of(user));
            when(orderRepository.existsByTenantIdAndCustomerId(TENANT_ID, USER_ID)).thenReturn(true);

            assertThrows(DataIntegrityViolationException.class, () -> userService.deleteUser(TENANT_ID, USER_ID));

            verify(userRepository).findByTenantIdAndId(TENANT_ID, USER_ID);
            verify(orderRepository).existsByTenantIdAndCustomerId(TENANT_ID, USER_ID);
            verify(userRepository, never()).delete(any(User.class));
        }

        @Test
        void deleteUser_UserNotFound_ThrowsResourceNotFoundException() {/*...*/
            when(userRepository.findByTenantIdAndId(TENANT_ID, USER_ID)).thenReturn(Optional.empty());
            assertThrows(ResourceNotFoundException.class, () -> userService.deleteUser(TENANT_ID, USER_ID));
            verify(orderRepository, never()).existsByTenantIdAndCustomerId(anyLong(), anyLong());
            verify(userRepository, never()).delete(any(User.class));
        }
    }

    @Nested
    @DisplayName("activate/deactivateUser Tests")
    class ActivationTests {
        @Test
        void activateUser_Inactive_ActivatesAndSaves() {
            user.setActive(false);
            when(userRepository.findByTenantIdAndId(TENANT_ID, USER_ID)).thenReturn(Optional.of(user));
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            User result = userService.activateUser(TENANT_ID, USER_ID);

            assertTrue(result.isActive());
            verify(userRepository).save(userCaptor.capture());
            assertTrue(userCaptor.getValue().isActive());
        }

        @Test
        void activateUser_AlreadyActive_ReturnsUserNoSave() {
            user.setActive(true);
            when(userRepository.findByTenantIdAndId(TENANT_ID, USER_ID)).thenReturn(Optional.of(user));

            User result = userService.activateUser(TENANT_ID, USER_ID);

            assertTrue(result.isActive());
            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        void deactivateUser_Active_DeactivatesAndSaves() {
            user.setActive(true);
            when(userRepository.findByTenantIdAndId(TENANT_ID, USER_ID)).thenReturn(Optional.of(user));
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            User result = userService.deactivateUser(TENANT_ID, USER_ID);

            assertFalse(result.isActive());
            verify(userRepository).save(userCaptor.capture());
            assertFalse(userCaptor.getValue().isActive());
        }

        @Test
        void deactivateUser_AlreadyInactive_ReturnsUserNoSave() {
            user.setActive(false);
            when(userRepository.findByTenantIdAndId(TENANT_ID, USER_ID)).thenReturn(Optional.of(user));

            User result = userService.deactivateUser(TENANT_ID, USER_ID);

            assertFalse(result.isActive());
            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        void activateDeactivate_UserNotFound_ThrowsException() {
            when(userRepository.findByTenantIdAndId(TENANT_ID, USER_ID)).thenReturn(Optional.empty());
            assertThrows(ResourceNotFoundException.class, () -> userService.activateUser(TENANT_ID, USER_ID));
            assertThrows(ResourceNotFoundException.class, () -> userService.deactivateUser(TENANT_ID, USER_ID));
            verify(userRepository, never()).save(any(User.class));
        }
    }
}

