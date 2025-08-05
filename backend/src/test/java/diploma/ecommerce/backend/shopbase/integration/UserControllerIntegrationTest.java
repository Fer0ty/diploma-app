package diploma.ecommerce.backend.shopbase.integration;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import diploma.ecommerce.backend.shopbase.dto.request.UserCreateRequest;
import diploma.ecommerce.backend.shopbase.dto.request.UserUpdateRequest;
import diploma.ecommerce.backend.shopbase.model.Address;
import diploma.ecommerce.backend.shopbase.model.Order;
import diploma.ecommerce.backend.shopbase.model.OrderStatus;
import diploma.ecommerce.backend.shopbase.model.User;
import diploma.ecommerce.backend.shopbase.multitenancy.TenantContext;
import diploma.ecommerce.backend.shopbase.repository.AddressRepository;
import diploma.ecommerce.backend.shopbase.repository.OrderRepository;
import diploma.ecommerce.backend.shopbase.repository.OrderStatusRepository;
import diploma.ecommerce.backend.shopbase.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("UserController Integration Tests")
public class UserControllerIntegrationTest extends BaseIntegrationTest {

    private static final String USERS_API_URL = "/api/v1/users";

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrderStatusRepository orderStatusRepository;
    @Autowired
    private AddressRepository addressRepository;

    private User customer1Tenant1, customer2Tenant1, customer1Tenant2;
    private OrderStatus statusDelivered, statusCanceled, statusActiveOrder, statusCreated;


    @BeforeEach
    void setUpCustomerUsers() {
        TenantContext.clear();
        statusDelivered = orderStatusRepository.findByStatusName("Delivered")
                .orElseGet(() -> orderStatusRepository.saveAndFlush(new OrderStatus("Delivered")));
        statusCanceled = orderStatusRepository.findByStatusName("Canceled")
                .orElseGet(() -> orderStatusRepository.saveAndFlush(new OrderStatus("Canceled")));
        statusActiveOrder = orderStatusRepository.findByStatusName("Processing")
                .orElseGet(() -> orderStatusRepository.saveAndFlush(new OrderStatus("Processing")));
        statusCreated = orderStatusRepository.findByStatusName("Processing")
                .orElseGet(() -> orderStatusRepository.saveAndFlush(new OrderStatus("Created")));


        customer1Tenant1 = new User();
        customer1Tenant1.setTenant(tenant1);
        customer1Tenant1.setFirstName("Alice");
        customer1Tenant1.setLastName("Smith");
        customer1Tenant1.setEmail("alice.smith@tenant1.com");
        customer1Tenant1.setPhone("111-1111");
        customer1Tenant1.setActive(true);
        customer1Tenant1.setCreatedAt(LocalDateTime.now());
        customer1Tenant1 = userRepository.saveAndFlush(customer1Tenant1);

        customer2Tenant1 = new User();
        customer2Tenant1.setTenant(tenant1);
        customer2Tenant1.setFirstName("Bob");
        customer2Tenant1.setLastName("Johnson");
        customer2Tenant1.setEmail("bob.johnson@tenant1.com");
        customer2Tenant1.setActive(true);
        customer2Tenant1.setCreatedAt(LocalDateTime.now());
        customer2Tenant1 = userRepository.saveAndFlush(customer2Tenant1);

        customer1Tenant2 = new User();
        customer1Tenant2.setTenant(tenant2);
        customer1Tenant2.setFirstName("Carol");
        customer1Tenant2.setLastName("Williams");
        customer1Tenant2.setEmail("carol.williams@tenant2.com");
        customer1Tenant2.setActive(false);
        customer1Tenant2.setCreatedAt(LocalDateTime.now());
        customer1Tenant2 = userRepository.saveAndFlush(customer1Tenant2);
    }

    @AfterEach
    void clearTenantContextAfterUserTest() {
        TenantContext.clear();
    }

    @Nested
    @DisplayName("GET /users Tests")
    class GetUsersTests {
        @Test
        @DisplayName("GET /users - Tenant 1 - Should return all users for current tenant (paginated)")
        void getAllUsers_forTenant1_shouldReturnTenant1Users() throws Exception {
            mockMvc.perform(get(USERS_API_URL)
                                    .headers(getAuthHeaders(jwtTenant1))
                                    .param("page", "0")
                                    .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content", hasSize(2)))
                    .andExpect(jsonPath(
                            "$.content[*].email",
                            containsInAnyOrder("alice.smith@tenant1.com", "bob.johnson@tenant1.com")
                    ))
                    .andExpect(jsonPath("$.totalElements", is(2)));
        }

        @Test
        @DisplayName("GET /users - Tenant 2 - Should return all users for current tenant")
        void getAllUsers_forTenant2_shouldReturnTenant2Users() throws Exception {
            mockMvc.perform(get(USERS_API_URL)
                                    .headers(getAuthHeaders(jwtTenant2))
                                    .param("size", "5"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].email", is("carol.williams@tenant2.com")));
        }

        @Test
        @DisplayName("GET /users - Should require authentication")
        void getAllUsers_shouldRequireAuthentication() throws Exception {
            mockMvc.perform(get(USERS_API_URL))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /users/{userId} Tests")
    class GetUserByIdTests {
        @Test
        @DisplayName("GET /{userId} - Tenant 1 - Should return user by ID for current tenant")
        void getUserById_forTenant1_whenOwnUser_shouldReturnUser() throws Exception {
            mockMvc.perform(get(USERS_API_URL + "/" + customer1Tenant1.getId())
                                    .headers(getAuthHeaders(jwtTenant1)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id", is(customer1Tenant1.getId().intValue())))
                    .andExpect(jsonPath("$.email", is(customer1Tenant1.getEmail())));
        }

        @Test
        @DisplayName("GET /{userId} - Tenant 1 - Should NOT return user of another tenant")
        void getUserById_forTenant1_whenAnothersUser_shouldReturnNotFound() throws Exception {
            mockMvc.perform(get(USERS_API_URL + "/" + customer1Tenant2.getId())
                                    .headers(getAuthHeaders(jwtTenant1)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("POST /users Tests")
    class CreateUserTests {
        @Test
        @DisplayName("POST /users - Tenant 1 - Should create new user successfully")
        void createUser_forTenant1_withValidData_shouldCreateUser() throws Exception {
            UserCreateRequest request = new UserCreateRequest();
            request.setFirstName("New");
            request.setLastName("Customer");
            request.setEmail("new.customer@tenant1.com");
            request.setPhone("555-0101");
            request.setActive(true);

            mockMvc.perform(post(USERS_API_URL)
                                    .headers(getAuthHeaders(jwtTenant1))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id", notNullValue()))
                    .andExpect(jsonPath("$.email", is("new.customer@tenant1.com")))
                    .andExpect(jsonPath("$.active", is(true)))
                    .andExpect(header().exists(HttpHeaders.LOCATION));

            assertTrue(userRepository.findByTenantIdAndEmail(tenant1.getId(), "new.customer@tenant1.com").isPresent());
        }

        @Test
        @DisplayName("POST /users - Tenant 1 - Should reject if email already exists for this tenant")
        void createUser_forTenant1_whenEmailExistsInTenant_shouldReturnConflict() throws Exception {
            UserCreateRequest request = new UserCreateRequest();
            request.setFirstName("Duplicate");
            request.setLastName("Email");
            request.setEmail(customer1Tenant1.getEmail());

            mockMvc.perform(post(USERS_API_URL)
                                    .headers(getAuthHeaders(jwtTenant1))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath(
                            "$.message",
                            containsString("Email '" + customer1Tenant1.getEmail() + "' is already registered for " +
                                                   "this store.")
                    ));
        }

        @Test
        @DisplayName("POST /users - Tenant 1 - Should allow same email if for DIFFERENT tenant")
        void createUser_forTenant1_allowsSameEmailAsOtherTenant() throws Exception {
            UserCreateRequest request = new UserCreateRequest();
            request.setFirstName("Another Carol");
            request.setLastName("Williams");
            request.setEmail(customer1Tenant2.getEmail());
            request.setPhone("555-0102");
            request.setActive(true);

            mockMvc.perform(post(USERS_API_URL)
                                    .headers(getAuthHeaders(jwtTenant1))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.email", is(customer1Tenant2.getEmail())));
        }
    }

    @Nested
    @DisplayName("PUT /users/{userId} Tests")
    class UpdateUserTests {
        @Test
        @DisplayName("PUT /{userId} - Tenant 1 - Should update user successfully")
        void updateUser_forTenant1_whenOwnUser_shouldUpdateSuccessfully() throws Exception {
            UserUpdateRequest request = new UserUpdateRequest();
            request.setFirstName("Alicia");
            request.setPhone("111-2222");
            request.setActive(true);

            mockMvc.perform(put(USERS_API_URL + "/" + customer1Tenant1.getId())
                                    .headers(getAuthHeaders(jwtTenant1))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id", is(customer1Tenant1.getId().intValue())))
                    .andExpect(jsonPath("$.firstName", is("Alicia")))
                    .andExpect(jsonPath("$.phone", is("111-2222")));

            User updatedUser = userRepository.findById(customer1Tenant1.getId()).orElseThrow();
            assertEquals("Alicia", updatedUser.getFirstName());
            assertEquals("111-2222", updatedUser.getPhone());
        }

        @Test
        @DisplayName("PUT /{userId} - Tenant 1 - Should reject if updated email already exists for another user in " +
                "THIS tenant")
        void updateUser_forTenant1_whenUpdatedEmailExistsInTenant_shouldReturnConflict() throws Exception {
            UserUpdateRequest request = new UserUpdateRequest();
            request.setEmail(customer2Tenant1.getEmail());

            mockMvc.perform(put(USERS_API_URL + "/" + customer1Tenant1.getId())
                                    .headers(getAuthHeaders(jwtTenant1))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath(
                            "$.message",
                            containsString("Email '" + customer2Tenant1.getEmail() + "' is already registered for " +
                                                   "this store.")
                    ));
        }
    }

    @Nested
    @DisplayName("DELETE /users/{userId} Tests")
    class DeleteUserTests {
        @Test
        @DisplayName("DELETE /{userId} - Tenant 1 - Should delete user without orders")
        void deleteUser_forTenant1_whenNoOrders_shouldDeleteSuccessfully() throws Exception {
            Long userIdToDelete = customer2Tenant1.getId();

            mockMvc.perform(delete(USERS_API_URL + "/" + userIdToDelete)
                                    .headers(getAuthHeaders(jwtTenant1)))
                    .andExpect(status().isNoContent());

            assertFalse(userRepository.existsById(userIdToDelete));
        }

        @Test
        @DisplayName("DELETE /{userId} - Tenant 1 - Should reject deletion if user has orders")
        void deleteUser_forTenant1_whenHasOrders_shouldReturnConflict() throws Exception {
            Address tempAddress = addressRepository.save(new Address(
                    null,
                    tenant1,
                    "Temp",
                    "Temp",
                    "Temp",
                    "1",
                    "1",
                    null,
                    null
            ));
            Order order = new Order();
            order.setTenant(tenant1);
            order.setCustomer(customer1Tenant1);
            order.setAddress(tempAddress);
            order.setStatus(statusCreated);
            order.setTotalAmount(BigDecimal.ONE);
            order.setCreatedAt(LocalDateTime.now());
            order.setUpdatedAt(LocalDateTime.now());
            orderRepository.saveAndFlush(order);


            mockMvc.perform(delete(USERS_API_URL + "/" + customer1Tenant1.getId())
                                    .headers(getAuthHeaders(jwtTenant1)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath(
                            "$.message",
                            containsString("Cannot delete user with ID " + customer1Tenant1.getId() + " because they " +
                                                   "have associated orders.")
                    ));

            assertTrue(userRepository.existsById(customer1Tenant1.getId()));
        }
    }

    @Nested
    @DisplayName("User Activation/Deactivation Tests")
    class UserActivationTests {
        @Test
        @DisplayName("POST /{userId}/activate - Tenant 1 - Should activate an inactive user")
        void activateUser_forTenant1_whenInactive_shouldActivate() throws Exception {
            User inactiveUserTenant1 = new User();
            inactiveUserTenant1.setTenant(tenant1);
            inactiveUserTenant1.setFirstName("Inactive");
            inactiveUserTenant1.setLastName("UserT1");
            inactiveUserTenant1.setEmail("inactive.usert1@tenant1.com");
            inactiveUserTenant1.setActive(false);
            inactiveUserTenant1.setCreatedAt(LocalDateTime.now());
            inactiveUserTenant1 = userRepository.saveAndFlush(inactiveUserTenant1);
            Long inactiveUserId = inactiveUserTenant1.getId();

            mockMvc.perform(post(USERS_API_URL + "/" + inactiveUserId + "/activate")
                                    .headers(getAuthHeaders(jwtTenant1)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(inactiveUserId.intValue())))
                    .andExpect(jsonPath("$.active", is(true)));

            Optional<User> userOpt = userRepository.findById(inactiveUserId);
            assertTrue(userOpt.isPresent() && userOpt.get().isActive());
        }

        @Test
        @DisplayName("POST /{userId}/deactivate - Tenant 1 - Should deactivate an active user without active orders")
        void deactivateUser_forTenant1_whenActiveAndNoActiveOrders_shouldDeactivate() throws Exception {
            Long userIdToDeactivate = customer2Tenant1.getId();

            mockMvc.perform(post(USERS_API_URL + "/" + userIdToDeactivate + "/deactivate")
                                    .headers(getAuthHeaders(jwtTenant1)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(userIdToDeactivate.intValue())))
                    .andExpect(jsonPath("$.active", is(false)));

            Optional<User> userOpt = userRepository.findById(userIdToDeactivate);
            assertTrue(userOpt.isPresent() && !userOpt.get().isActive());
        }

        @Test
        @DisplayName("POST /{userId}/deactivate - Tenant 1 - Should reject deactivation if user has active orders")
        void deactivateUser_forTenant1_whenHasActiveOrders_shouldReturnBadRequest() throws Exception {
            Address tempAddress = addressRepository.save(new Address(
                    null,
                    tenant1,
                    "Temp",
                    "Temp",
                    "Temp",
                    "1",
                    "1",
                    null,
                    null
            ));
            Order activeOrder = new Order();
            activeOrder.setTenant(tenant1);
            activeOrder.setCustomer(customer1Tenant1);
            activeOrder.setAddress(tempAddress);
            activeOrder.setStatus(statusActiveOrder);
            activeOrder.setTotalAmount(BigDecimal.TEN);
            activeOrder.setCreatedAt(LocalDateTime.now());
            activeOrder.setUpdatedAt(LocalDateTime.now());
            orderRepository.saveAndFlush(activeOrder);

            mockMvc.perform(post(USERS_API_URL + "/" + customer1Tenant1.getId() + "/deactivate")
                                    .headers(getAuthHeaders(jwtTenant1)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", is("Cannot deactivate user with active orders.")));

            Optional<User> userOpt = userRepository.findById(customer1Tenant1.getId());
            assertTrue(userOpt.isPresent() && userOpt.get().isActive());
        }
    }
}
