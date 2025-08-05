package diploma.ecommerce.backend.shopbase.integration;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import diploma.ecommerce.backend.shopbase.dto.request.OrderStatusCreateRequest;
import diploma.ecommerce.backend.shopbase.dto.request.OrderStatusUpdateRequest;
import diploma.ecommerce.backend.shopbase.model.Address;
import diploma.ecommerce.backend.shopbase.model.Order;
import diploma.ecommerce.backend.shopbase.model.OrderStatus;
import diploma.ecommerce.backend.shopbase.model.User;
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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItems;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("OrderStatusController Integration Tests")
public class OrderStatusControllerIntegrationTest extends BaseIntegrationTest {

    private static final String STATUSES_API_URL = "/api/v1/order-statuses";

    @Autowired
    private OrderStatusRepository orderStatusRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private UserRepository userRepository;

    private OrderStatus testStatus1;
    private OrderStatus testStatus2;

    @BeforeEach
    void setUp() {
        testStatus1 = new OrderStatus("TestStatus1");
        testStatus2 = new OrderStatus("TestStatus2");

        testStatus1 = orderStatusRepository.save(testStatus1);
        testStatus2 = orderStatusRepository.save(testStatus2);
    }

    @AfterEach
    void cleanUp() {
        orderStatusRepository.deleteAll();
    }

    @Nested
    @DisplayName("GET /order-statuses Tests")
    class GetAllOrderStatusesTests {

        @Test
        @DisplayName("GET /order-statuses - Should return all order statuses")
        void getAllOrderStatuses_shouldReturnAllStatuses() throws Exception {
            mockMvc.perform(get(STATUSES_API_URL)
                                    .headers(getAuthHeaders(jwtTenant1)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))))
                    .andExpect(jsonPath(
                            "$[*].statusName",
                            hasItems(testStatus1.getStatusName(), testStatus2.getStatusName())
                    ));
        }

        @Test
        @DisplayName("GET /order-statuses - Should require authentication")
        void getAllOrderStatuses_shouldRequireAuthentication() throws Exception {
            mockMvc.perform(get(STATUSES_API_URL))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /order-statuses/{id} Tests")
    class GetOrderStatusByIdTests {

        @Test
        @DisplayName("GET /order-statuses/{id} - Should return order status by ID")
        void getOrderStatusById_whenExists_shouldReturnStatus() throws Exception {
            mockMvc.perform(get(STATUSES_API_URL + "/" + testStatus1.getId())
                                    .headers(getAuthHeaders(jwtTenant1)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id", is(testStatus1.getId().intValue())))
                    .andExpect(jsonPath("$.statusName", is(testStatus1.getStatusName())));
        }

        @Test
        @DisplayName("GET /order-statuses/{id} - Should return 404 for non-existent status")
        void getOrderStatusById_whenNonExistent_shouldReturn404() throws Exception {
            mockMvc.perform(get(STATUSES_API_URL + "/99999")
                                    .headers(getAuthHeaders(jwtTenant1)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("GET /order-statuses/{id} - Should require authentication")
        void getOrderStatusById_shouldRequireAuthentication() throws Exception {
            mockMvc.perform(get(STATUSES_API_URL + "/" + testStatus1.getId()))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("POST /order-statuses Tests")
    class CreateOrderStatusTests {

        @Test
        @DisplayName("POST /order-statuses - Should create new order status")
        void createOrderStatus_withValidData_shouldCreateSuccessfully() throws Exception {
            OrderStatusCreateRequest request = new OrderStatusCreateRequest();
            request.setStatusName("NewTestStatus");

            mockMvc.perform(post(STATUSES_API_URL)
                                    .headers(getAuthHeaders(jwtTenant1))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id", notNullValue()))
                    .andExpect(jsonPath("$.statusName", is("NewTestStatus")));

            assertTrue(orderStatusRepository.findByStatusName("NewTestStatus").isPresent());
        }

        @Test
        @DisplayName("POST /order-statuses - Should reject creating status with existing name")
        void createOrderStatus_withExistingName_shouldReturnConflict() throws Exception {
            OrderStatusCreateRequest request = new OrderStatusCreateRequest();
            request.setStatusName(testStatus1.getStatusName());

            mockMvc.perform(post(STATUSES_API_URL)
                                    .headers(getAuthHeaders(jwtTenant1))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message", containsString("already exists")));
        }

        @Test
        @DisplayName("POST /order-statuses - Should reject creating status with invalid data")
        void createOrderStatus_withInvalidData_shouldReturnBadRequest() throws Exception {
            OrderStatusCreateRequest request = new OrderStatusCreateRequest();
            request.setStatusName("");

            mockMvc.perform(post(STATUSES_API_URL)
                                    .headers(getAuthHeaders(jwtTenant1))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            request.setStatusName("x".repeat(51));

            mockMvc.perform(post(STATUSES_API_URL)
                                    .headers(getAuthHeaders(jwtTenant1))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("POST /order-statuses - Should require authentication")
        void createOrderStatus_shouldRequireAuthentication() throws Exception {
            OrderStatusCreateRequest request = new OrderStatusCreateRequest();
            request.setStatusName("AuthRequiredTest");

            mockMvc.perform(post(STATUSES_API_URL)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("PUT /order-statuses/{id} Tests")
    class UpdateOrderStatusTests {

        @Test
        @DisplayName("PUT /order-statuses/{id} - Should update order status")
        void updateOrderStatus_withValidData_shouldUpdateSuccessfully() throws Exception {
            OrderStatusUpdateRequest request = new OrderStatusUpdateRequest();
            request.setStatusName("UpdatedTestStatus");

            mockMvc.perform(put(STATUSES_API_URL + "/" + testStatus1.getId())
                                    .headers(getAuthHeaders(jwtTenant1))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id", is(testStatus1.getId().intValue())))
                    .andExpect(jsonPath("$.statusName", is("UpdatedTestStatus")));

            Optional<OrderStatus> updatedStatus = orderStatusRepository.findById(testStatus1.getId());
            assertTrue(updatedStatus.isPresent());
            assertEquals("UpdatedTestStatus", updatedStatus.get().getStatusName());
        }

        @Test
        @DisplayName("PUT /order-statuses/{id} - Should return 404 for non-existent status")
        void updateOrderStatus_nonExistent_shouldReturn404() throws Exception {
            OrderStatusUpdateRequest request = new OrderStatusUpdateRequest();
            request.setStatusName("NonExistentUpdateTest");

            mockMvc.perform(put(STATUSES_API_URL + "/99999")
                                    .headers(getAuthHeaders(jwtTenant1))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("PUT /order-statuses/{id} - Should reject updating to existing name")
        void updateOrderStatus_toExistingName_shouldReturnConflict() throws Exception {
            OrderStatusUpdateRequest request = new OrderStatusUpdateRequest();
            request.setStatusName(testStatus2.getStatusName());

            mockMvc.perform(put(STATUSES_API_URL + "/" + testStatus1.getId())
                                    .headers(getAuthHeaders(jwtTenant1))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message", containsString("already exists")));
        }

        @Test
        @DisplayName("PUT /order-statuses/{id} - Should reject updating with invalid data")
        void updateOrderStatus_withInvalidData_shouldReturnBadRequest() throws Exception {
            OrderStatusUpdateRequest request = new OrderStatusUpdateRequest();
            request.setStatusName("");

            mockMvc.perform(put(STATUSES_API_URL + "/" + testStatus1.getId())
                                    .headers(getAuthHeaders(jwtTenant1))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("PUT /order-statuses/{id} - Should require authentication")
        void updateOrderStatus_shouldRequireAuthentication() throws Exception {
            OrderStatusUpdateRequest request = new OrderStatusUpdateRequest();
            request.setStatusName("AuthRequiredUpdateTest");

            mockMvc.perform(put(STATUSES_API_URL + "/" + testStatus1.getId())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("DELETE /order-statuses/{id} Tests")
    class DeleteOrderStatusTests {

        @Test
        @DisplayName("DELETE /order-statuses/{id} - Should delete unused order status")
        @Transactional
        void deleteOrderStatus_whenUnused_shouldDeleteSuccessfully() throws Exception {
            OrderStatus tempStatus = new OrderStatus("TemporaryStatusForDelete");
            tempStatus = orderStatusRepository.save(tempStatus);

            mockMvc.perform(delete(STATUSES_API_URL + "/" + tempStatus.getId())
                                    .headers(getAuthHeaders(jwtTenant1)))
                    .andExpect(status().isNoContent());

            assertFalse(orderStatusRepository.existsById(tempStatus.getId()));
        }

        @Test
        @DisplayName("DELETE /order-statuses/{id} - Should reject deleting status used in orders")
        void deleteOrderStatus_whenUsed_shouldReturnConflict() throws Exception {
            Order order = new Order();
            order.setStatus(testStatus1);
            order.setTenant(tenant1);
            order.setTotalAmount(new BigDecimal("100.00"));

            User customer = new User();
            customer.setTenant(tenant1);
            customer.setFirstName("Test");
            customer.setLastName("Customer");
            customer.setEmail("test@example.com");
            customer.setActive(true);
            customer.setCreatedAt(LocalDateTime.now());
            userRepository.save(customer);
            order.setCustomer(customer);

            Address address = new Address();
            address.setTenant(tenant1);
            address.setCountry("Country");
            address.setCity("City");
            address.setStreet("Street");
            address.setHouseNumber("1");
            addressRepository.save(address);
            order.setAddress(address);

            order = orderRepository.save(order);

            try {
                mockMvc.perform(delete(STATUSES_API_URL + "/" + testStatus1.getId())
                                        .headers(getAuthHeaders(jwtTenant1)))
                        .andExpect(status().isConflict())
                        .andExpect(jsonPath("$.message", containsString("associated with existing orders")));
            } finally {
                orderRepository.delete(order);
            }
        }


        @Test
        @DisplayName("DELETE /order-statuses/{id} - Should return 404 for non-existent status")
        void deleteOrderStatus_whenNonExistent_shouldReturn404() throws Exception {
            mockMvc.perform(delete(STATUSES_API_URL + "/99999")
                                    .headers(getAuthHeaders(jwtTenant1)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("DELETE /order-statuses/{id} - Should require authentication")
        void deleteOrderStatus_shouldRequireAuthentication() throws Exception {
            mockMvc.perform(delete(STATUSES_API_URL + "/" + testStatus1.getId()))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("Order Status Management Flow Tests")
    class OrderStatusManagementFlowTests {

        @Test
        @DisplayName("Should handle complete order status lifecycle: create -> retrieve -> update -> delete")
        @Transactional
        void shouldHandleCompleteOrderStatusLifecycle() throws Exception {
            OrderStatusCreateRequest createRequest = new OrderStatusCreateRequest();
            createRequest.setStatusName("LifecycleTestStatus");

            MvcResult createResult = mockMvc.perform(post(STATUSES_API_URL)
                                                             .headers(getAuthHeaders(jwtTenant1))
                                                             .contentType(MediaType.APPLICATION_JSON)
                                                             .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.statusName", is("LifecycleTestStatus")))
                    .andReturn();

            String responseContent = createResult.getResponse().getContentAsString();
            Long createdStatusId = objectMapper.readTree(responseContent).get("id").asLong();

            mockMvc.perform(get(STATUSES_API_URL + "/" + createdStatusId)
                                    .headers(getAuthHeaders(jwtTenant1)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(createdStatusId.intValue())))
                    .andExpect(jsonPath("$.statusName", is("LifecycleTestStatus")));

            OrderStatusUpdateRequest updateRequest = new OrderStatusUpdateRequest();
            updateRequest.setStatusName("UpdatedLifecycleTestStatus");

            mockMvc.perform(put(STATUSES_API_URL + "/" + createdStatusId)
                                    .headers(getAuthHeaders(jwtTenant1))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(createdStatusId.intValue())))
                    .andExpect(jsonPath("$.statusName", is("UpdatedLifecycleTestStatus")));

            Optional<OrderStatus> updatedStatus = orderStatusRepository.findById(createdStatusId);
            assertTrue(updatedStatus.isPresent());
            assertEquals("UpdatedLifecycleTestStatus", updatedStatus.get().getStatusName());

            mockMvc.perform(delete(STATUSES_API_URL + "/" + createdStatusId)
                                    .headers(getAuthHeaders(jwtTenant1)))
                    .andExpect(status().isNoContent());

            assertFalse(orderStatusRepository.existsById(createdStatusId));
        }
    }
}
