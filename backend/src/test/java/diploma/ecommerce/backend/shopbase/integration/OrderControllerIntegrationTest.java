package diploma.ecommerce.backend.shopbase.integration;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.core.type.TypeReference;
import diploma.ecommerce.backend.shopbase.dto.request.OrderCreateRequest;
import diploma.ecommerce.backend.shopbase.dto.request.OrderItemCreateRequest;
import diploma.ecommerce.backend.shopbase.dto.request.OrderUpdateRequest;
import diploma.ecommerce.backend.shopbase.model.Address;
import diploma.ecommerce.backend.shopbase.model.Order;
import diploma.ecommerce.backend.shopbase.model.OrderItem;
import diploma.ecommerce.backend.shopbase.model.OrderStatus;
import diploma.ecommerce.backend.shopbase.model.Product;
import diploma.ecommerce.backend.shopbase.model.User;
import diploma.ecommerce.backend.shopbase.multitenancy.TenantContext;
import diploma.ecommerce.backend.shopbase.repository.AddressRepository;
import diploma.ecommerce.backend.shopbase.repository.OrderItemRepository;
import diploma.ecommerce.backend.shopbase.repository.OrderRepository;
import diploma.ecommerce.backend.shopbase.repository.OrderStatusRepository;
import diploma.ecommerce.backend.shopbase.repository.ProductRepository;
import diploma.ecommerce.backend.shopbase.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("OrderController Integration Tests")
public class OrderControllerIntegrationTest extends BaseIntegrationTest {

    private static final String ORDERS_API_URL = "/api/v1/orders";
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private AddressRepository addressRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrderItemRepository orderItemRepository;
    @Autowired
    private OrderStatusRepository orderStatusRepository;
    private Product product1Tenant1, product2Tenant1, product1Tenant2;
    private User customer1Tenant1, customer1Tenant2;
    private Address address1Tenant1, address1Tenant2;
    private OrderStatus statusCreated, statusPaid, statusProcessing, statusCanceled;
    private Order order1Tenant1, order1Tenant2;

    @BeforeEach
    void setUpOrderTestData() {
        TenantContext.clear();

        statusCreated = orderStatusRepository.findByStatusName("Created")
                .orElseGet(() -> {
                    OrderStatus status = new OrderStatus();
                    status.setStatusName("Created");
                    return orderStatusRepository.saveAndFlush(status);
                });
        statusPaid = orderStatusRepository.findByStatusName("Paid")
                .orElseGet(() -> {
                    OrderStatus status = new OrderStatus();
                    status.setStatusName("Paid");
                    return orderStatusRepository.saveAndFlush(status);
                });
        statusProcessing = orderStatusRepository.findByStatusName("Processing")
                .orElseGet(() -> {
                    OrderStatus status = new OrderStatus();
                    status.setStatusName("Processing");
                    return orderStatusRepository.saveAndFlush(status);
                });
        statusCanceled = orderStatusRepository.findByStatusName("Canceled")
                .orElseGet(() -> {
                    OrderStatus status = new OrderStatus();
                    status.setStatusName("Canceled");
                    return orderStatusRepository.saveAndFlush(status);
                });
        // Tenant 1
        product1Tenant1 = new Product();
        product1Tenant1.setTenant(tenant1);
        product1Tenant1.setName("Laptop X1");
        product1Tenant1.setPrice(new BigDecimal("1200.00"));
        product1Tenant1.setStockQuantity(50);
        product1Tenant1.setActive(true);
        product1Tenant1 = productRepository.saveAndFlush(product1Tenant1);

        product2Tenant1 = new Product();
        product2Tenant1.setTenant(tenant1);
        product2Tenant1.setName("Mouse G5");
        product2Tenant1.setPrice(new BigDecimal("75.00"));
        product2Tenant1.setStockQuantity(100);
        product2Tenant1.setActive(true);
        product2Tenant1 = productRepository.saveAndFlush(product2Tenant1);

        customer1Tenant1 = new User();
        customer1Tenant1.setTenant(tenant1);
        customer1Tenant1.setFirstName("John");
        customer1Tenant1.setLastName("Doe");
        customer1Tenant1.setEmail("john.doe@tenant1.com");
        customer1Tenant1.setActive(true);
        customer1Tenant1.setCreatedAt(LocalDateTime.now());
        customer1Tenant1 = userRepository.saveAndFlush(customer1Tenant1);

        address1Tenant1 = new Address();
        address1Tenant1.setTenant(tenant1);
        address1Tenant1.setCountry("CountryT1");
        address1Tenant1.setCity("CityT1");
        address1Tenant1.setStreet("StreetT1");
        address1Tenant1.setHouseNumber("1T1");
        address1Tenant1.setPostalCode("11111");
        address1Tenant1 = addressRepository.saveAndFlush(address1Tenant1);

        order1Tenant1 = new Order();
        order1Tenant1.setTenant(tenant1);
        order1Tenant1.setCustomer(customer1Tenant1);
        order1Tenant1.setAddress(address1Tenant1);
        order1Tenant1.setStatus(statusCreated);
        order1Tenant1.setTotalAmount(new BigDecimal("1275.00"));
        order1Tenant1.setCreatedAt(LocalDateTime.now());
        order1Tenant1.setUpdatedAt(LocalDateTime.now());
        order1Tenant1 = orderRepository.saveAndFlush(order1Tenant1);

        OrderItem item1Order1 = new OrderItem();
        item1Order1.setTenant(tenant1);
        item1Order1.setOrder(order1Tenant1);
        item1Order1.setProduct(product1Tenant1);
        item1Order1.setQuantity(1);
        item1Order1.setUnitPrice(product1Tenant1.getPrice());
        item1Order1.setTotalPrice(product1Tenant1.getPrice());
        orderItemRepository.saveAndFlush(item1Order1);

        OrderItem item2Order1 = new OrderItem();
        item2Order1.setTenant(tenant1);
        item2Order1.setOrder(order1Tenant1);
        item2Order1.setProduct(product2Tenant1);
        item2Order1.setQuantity(1);
        item2Order1.setUnitPrice(product2Tenant1.getPrice());
        item2Order1.setTotalPrice(product2Tenant1.getPrice());
        orderItemRepository.saveAndFlush(item2Order1);

        // Tenant 2
        product1Tenant2 = new Product();
        product1Tenant2.setTenant(tenant2);
        product1Tenant2.setName("Tablet T2");
        product1Tenant2.setPrice(new BigDecimal("450.00"));
        product1Tenant2.setStockQuantity(30);
        product1Tenant2.setActive(true);
        product1Tenant2 = productRepository.saveAndFlush(product1Tenant2);

        customer1Tenant2 = new User();
        customer1Tenant2.setTenant(tenant2);
        customer1Tenant2.setFirstName("Jane");
        customer1Tenant2.setLastName("Smith");
        customer1Tenant2.setEmail("jane.smith@tenant2.com");
        customer1Tenant2.setActive(true);
        customer1Tenant2.setCreatedAt(LocalDateTime.now());
        customer1Tenant2 = userRepository.saveAndFlush(customer1Tenant2);

        address1Tenant2 = new Address();
        address1Tenant2.setTenant(tenant2);
        address1Tenant2.setCountry("CountryT2");
        address1Tenant2.setCity("CityT2");
        address1Tenant2.setStreet("StreetT2");
        address1Tenant2.setHouseNumber("2T2");
        address1Tenant2.setPostalCode("22222");
        address1Tenant2 = addressRepository.saveAndFlush(address1Tenant2);

        order1Tenant2 = new Order();
        order1Tenant2.setTenant(tenant2);
        order1Tenant2.setCustomer(customer1Tenant2);
        order1Tenant2.setAddress(address1Tenant2);
        order1Tenant2.setStatus(statusCreated);
        order1Tenant2.setTotalAmount(new BigDecimal("450.00"));
        order1Tenant2.setCreatedAt(LocalDateTime.now());
        order1Tenant2.setUpdatedAt(LocalDateTime.now());
        order1Tenant2 = orderRepository.saveAndFlush(order1Tenant2);

        OrderItem item1Order2 = new OrderItem();
        item1Order2.setTenant(tenant2);
        item1Order2.setOrder(order1Tenant2);
        item1Order2.setProduct(product1Tenant2);
        item1Order2.setQuantity(1);
        item1Order2.setUnitPrice(product1Tenant2.getPrice());
        item1Order2.setTotalPrice(product1Tenant2.getPrice());
        orderItemRepository.saveAndFlush(item1Order2);
    }

    @AfterEach
    void clearTenantContextAfterOrderTest() {
        TenantContext.clear();
    }

    @Nested
    @DisplayName("GET /orders Tests")
    class GetOrdersTests {
        @Test
        @DisplayName("GET /orders - Tenant 1 - Should return all orders for current tenant")
        void getAllOrders_forTenant1_shouldReturnOnlyTenant1Orders() throws Exception {
            mockMvc.perform(get(ORDERS_API_URL)
                                    .param("sort", "createdAt")
                                    .param("direction", "desc")
                                    .headers(getAuthHeaders(jwtTenant1)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].id", is(order1Tenant1.getId().intValue())))
                    .andExpect(jsonPath("$.content[0].customerId", is(customer1Tenant1.getId().intValue())))
                    .andExpect(jsonPath("$.content[0].addressId", is(address1Tenant1.getId().intValue())))
                    .andExpect(jsonPath("$.content[0].statusName", is(statusCreated.getStatusName())));
        }

        @Test
        @DisplayName("GET /orders - Tenant 1 - Should NOT return orders of another tenant")
        void getAllOrders_forTenant1_shouldNotReturnTenant2Orders() throws Exception {
            MvcResult result = mockMvc.perform(get(ORDERS_API_URL)
                            .param("sort", "createdAt")
                            .param("direction", "desc")
                            .headers(getAuthHeaders(jwtTenant1)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andReturn();

            String responseContent = result.getResponse().getContentAsString();
            Map<String, Object> page = objectMapper.readValue(
                    responseContent, new TypeReference<>() {
                    }
            );
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> ordersList = (List<Map<String, Object>>) page.get("content");

            assertNotNull(ordersList);
            assertEquals(1, ordersList.size(), "Should only contain orders for tenant1");
            assertTrue(
                    ordersList.stream().noneMatch(order -> ((Number) order.get("id")).longValue() == order1Tenant2.getId()),
                    "Should not contain orders from tenant2"
            );
            assertEquals(order1Tenant1.getId().longValue(), ((Number) ordersList.getFirst().get("id")).longValue());
        }
    }

    @Nested
    @DisplayName("GET /orders/{id} Tests")
    class GetOrderByIdTests {
        @Test
        @DisplayName("GET /orders/{id} - Tenant 1 - Should return order by ID for current tenant")
        void getOrderById_forTenant1_whenOwnOrder_shouldReturnOrder() throws Exception {
            mockMvc.perform(get(ORDERS_API_URL + "/" + order1Tenant1.getId())
                                    .headers(getAuthHeaders(jwtTenant1)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id", is(order1Tenant1.getId().intValue())))
                    .andExpect(jsonPath("$.customerId", is(customer1Tenant1.getId().intValue())));
        }

        @Test
        @DisplayName("GET /orders/{id} - Tenant 1 - Should NOT return order of another tenant")
        void getOrderById_forTenant1_whenAnothersOrder_shouldReturnNotFound() throws Exception {
            mockMvc.perform(get(ORDERS_API_URL + "/" + order1Tenant2.getId())
                                    .headers(getAuthHeaders(jwtTenant1)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("GET /orders/{id} - Tenant 1 - Should return 404 for non-existent order")
        void getOrderById_forTenant1_whenOrderNonExistent_shouldReturn404() throws Exception {
            mockMvc.perform(get(ORDERS_API_URL + "/99999")
                                    .headers(getAuthHeaders(jwtTenant1)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("GET /orders/{id} - Should require authentication")
        void getOrderById_shouldRequireAuthentication() throws Exception {
            mockMvc.perform(get(ORDERS_API_URL + "/" + order1Tenant1.getId()))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("POST /orders Tests")
    class CreateOrderTests {
        @Test
        @DisplayName("POST /orders - Tenant 1 - Should create order successfully")
        void createOrder_forTenant1_shouldCreateSuccessfully() throws Exception {
            OrderCreateRequest request = new OrderCreateRequest();
            request.setCustomerId(customer1Tenant1.getId());
            request.setAddressId(address1Tenant1.getId());

            List<OrderItemCreateRequest> items = new ArrayList<>();
            OrderItemCreateRequest itemRequest = new OrderItemCreateRequest();
            itemRequest.setProductId(product1Tenant1.getId());
            itemRequest.setQuantity(2);
            items.add(itemRequest);
            request.setOrderItems(items);

            int initialStock = product1Tenant1.getStockQuantity();

            mockMvc.perform(post(ORDERS_API_URL)
                                    .headers(getAuthHeaders(jwtTenant1))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id", notNullValue()))
                    .andExpect(jsonPath("$.customerId", is(customer1Tenant1.getId().intValue())))
                    .andExpect(jsonPath("$.addressId", is(address1Tenant1.getId().intValue())))
                    .andExpect(jsonPath("$.statusName", is(statusCreated.getStatusName())))
                    .andExpect(jsonPath("$.totalAmount", is(product1Tenant1.getPrice().doubleValue() * 2)));

            Product updatedProduct = productRepository.findById(product1Tenant1.getId()).orElseThrow();
            assertEquals(initialStock - 2, updatedProduct.getStockQuantity());
        }

        @Test
        @DisplayName("POST /orders - Tenant 1 - Should reject if customer doesn't exist or belongs to another tenant")
        void createOrder_forTenant1_ifCustomerNonExistentOrOtherTenant_shouldReject() throws Exception {
            OrderCreateRequest request = new OrderCreateRequest();
            request.setAddressId(address1Tenant1.getId());
            request.setOrderItems(List.of(new OrderItemCreateRequest(product1Tenant1.getId(), 1)));

            request.setCustomerId(999L);
            mockMvc.perform(post(ORDERS_API_URL)
                                    .headers(getAuthHeaders(jwtTenant1))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath(
                            "$.message",
                            containsString("Customer not found with id :")
                    ))
                    .andExpect(jsonPath(
                            "$.message",
                            containsString("for Tenant ID:")
                    ));

            request.setCustomerId(customer1Tenant2.getId());
            mockMvc.perform(post(ORDERS_API_URL)
                                    .headers(getAuthHeaders(jwtTenant1))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath(
                            "$.message",
                            containsString("Customer not found with id :")
                    ))
                    .andExpect(jsonPath(
                            "$.message",
                            containsString("for Tenant ID:")
                    ));
        }

        @Test
        @DisplayName("POST /orders - Tenant 1 - Should reject if address doesn't exist or belongs to another tenant")
        void createOrder_forTenant1_ifAddressNonExistentOrOtherTenant_shouldReject() throws Exception {
            OrderCreateRequest request = new OrderCreateRequest();
            request.setCustomerId(customer1Tenant1.getId());
            request.setOrderItems(List.of(new OrderItemCreateRequest(product1Tenant1.getId(), 1)));

            request.setAddressId(999L);
            mockMvc.perform(post(ORDERS_API_URL)
                                    .headers(getAuthHeaders(jwtTenant1))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath(
                            "$.message",
                            containsString("Address not found with id :")
                    ))
                    .andExpect(jsonPath(
                            "$.message",
                            containsString("for Tenant ID:")
                    ));

            request.setAddressId(address1Tenant2.getId());
            mockMvc.perform(post(ORDERS_API_URL)
                                    .headers(getAuthHeaders(jwtTenant1))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath(
                            "$.message",
                            containsString("Address not found with id :")
                    ))
                    .andExpect(jsonPath(
                            "$.message",
                            containsString("for Tenant ID:")
                    ));
        }

        @Test
        @DisplayName("POST /orders - Tenant 1 - Should reject if product doesn't exist or belongs to another tenant")
        void createOrder_forTenant1_ifProductNonExistentOrOtherTenant_shouldReject() throws Exception {
            OrderCreateRequest request = new OrderCreateRequest();
            request.setCustomerId(customer1Tenant1.getId());
            request.setAddressId(address1Tenant1.getId());

            request.setOrderItems(List.of(new OrderItemCreateRequest(999L, 1)));
            mockMvc.perform(post(ORDERS_API_URL)
                                    .headers(getAuthHeaders(jwtTenant1))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath(
                            "$.message",
                            containsString("Product not found with id :")
                    )).andExpect(jsonPath(
                            "$.message",
                            containsString("for Tenant ID:")
                    ));

            request.setOrderItems(List.of(new OrderItemCreateRequest(
                    product1Tenant2.getId(),
                    1
            )));
            mockMvc.perform(post(ORDERS_API_URL)
                                    .headers(getAuthHeaders(jwtTenant1))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath(
                            "$.message",
                            containsString("Product not found with id :")
                    )).andExpect(jsonPath(
                            "$.message",
                            containsString("for Tenant ID:")
                    ));
        }


        @Test
        @DisplayName("POST /orders - Tenant 1 - Should reject if insufficient stock")
        void createOrder_forTenant1_ifInsufficientStock_shouldReject() throws Exception {
            OrderCreateRequest request = new OrderCreateRequest();
            request.setCustomerId(customer1Tenant1.getId());
            request.setAddressId(address1Tenant1.getId());
            List<OrderItemCreateRequest> items = List.of(new OrderItemCreateRequest(
                    product1Tenant1.getId(),
                    product1Tenant1.getStockQuantity() + 1
            ));
            request.setOrderItems(items);

            mockMvc.perform(post(ORDERS_API_URL)
                                    .headers(getAuthHeaders(jwtTenant1))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath(
                            "$.message",
                            containsString("Insufficient stock for Product ID:")
                    )).andExpect(jsonPath(
                            "$.message",
                            containsString(" Requested:")
                    )).andExpect(jsonPath(
                            "$.message",
                            containsString("Available:")
                    ));
        }
    }

    @Nested
    @DisplayName("PUT /orders/{id}/status Tests")
    class UpdateOrderStatusTests {
        @Test
        @DisplayName("PUT /orders/{id}/status - Tenant 1 - Should update order status")
        void updateOrderStatus_forTenant1_shouldUpdateSuccessfully() throws Exception {
            OrderUpdateRequest request = new OrderUpdateRequest();
            request.setStatusId(statusPaid.getId());
            request.setComment("Payment confirmed by admin");

            mockMvc.perform(put(ORDERS_API_URL + "/" + order1Tenant1.getId() + "/status")
                                    .headers(getAuthHeaders(jwtTenant1))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id", is(order1Tenant1.getId().intValue())))
                    .andExpect(jsonPath("$.statusId", is(statusPaid.getId().intValue())))
                    .andExpect(jsonPath("$.statusName", is(statusPaid.getStatusName())))
                    .andExpect(jsonPath("$.comment", is("Status changed to Paid: Payment confirmed by admin")));

            Optional<Order> updatedOrderOpt = orderRepository.findById(order1Tenant1.getId());
            assertTrue(updatedOrderOpt.isPresent());
            assertEquals(statusPaid.getId(), updatedOrderOpt.get().getStatus().getId());
            assertEquals("Status changed to Paid: Payment confirmed by admin", updatedOrderOpt.get().getComment());
        }

        @Test
        @DisplayName("PUT /orders/{id}/status - Tenant 1 - Should reject if order status does not exist")
        void updateOrderStatus_forTenant1_ifStatusNonExistent_shouldReject() throws Exception {
            OrderUpdateRequest request = new OrderUpdateRequest();
            request.setStatusId(999L);
            request.setComment("Trying invalid status");

            mockMvc.perform(put(ORDERS_API_URL + "/" + order1Tenant1.getId() + "/status")
                                    .headers(getAuthHeaders(jwtTenant1))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message", containsString("OrderStatus not found with id : '999'")));
        }

        @Test
        @DisplayName("PUT /orders/{id}/status - Tenant 1 - Should NOT update status of another tenant's order")
        void updateOrderStatus_forTenant1_whenAnothersOrder_shouldReturnNotFound() throws Exception {
            OrderUpdateRequest request = new OrderUpdateRequest();
            request.setStatusId(statusPaid.getId());

            mockMvc.perform(put(ORDERS_API_URL + "/" + order1Tenant2.getId() + "/status")
                                    .headers(getAuthHeaders(jwtTenant1))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("DELETE /orders/{id} Tests")
    class DeleteOrderTests {

        @Test
        @DisplayName("DELETE /orders/{id} - Tenant 1 - Should delete order (when status allows)")
        void deleteOrder_forTenant1_whenStatusAllows_shouldDelete() throws Exception {
            order1Tenant1.setStatus(statusCanceled);
            orderRepository.saveAndFlush(order1Tenant1);

            mockMvc.perform(delete(ORDERS_API_URL + "/" + order1Tenant1.getId())
                                    .headers(getAuthHeaders(jwtTenant1)))
                    .andExpect(status().isNoContent());

            Optional<Order> deletedOrder = orderRepository.findById(order1Tenant1.getId());
            assertTrue(deletedOrder.isEmpty(), "Order should have been deleted");
        }


        @Test
        @DisplayName("DELETE /orders/{id} - Tenant 1 - Should reject delete if status doesn't allow")
        void deleteOrder_forTenant1_ifStatusDoesNotAllow_shouldReject() throws Exception {
            order1Tenant1.setStatus(statusCreated);
            orderRepository.saveAndFlush(order1Tenant1);

            mockMvc.perform(delete(ORDERS_API_URL + "/" + order1Tenant1.getId())
                                    .headers(getAuthHeaders(jwtTenant1)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath(
                            "$.message",
                            containsString("Cannot delete order with status: " + statusCreated.getStatusName())
                    ));
        }

        @Test
        @DisplayName("DELETE /orders/{id} - Tenant 1 - Can't delete order from another tenant")
        void deleteOrder_forTenant1_whenAnothersOrder_shouldReturnNotFound() throws Exception {
            order1Tenant2.setStatus(statusCanceled);
            orderRepository.saveAndFlush(order1Tenant2);

            mockMvc.perform(delete(ORDERS_API_URL + "/" + order1Tenant2.getId())
                                    .headers(getAuthHeaders(jwtTenant1)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /orders/{id}/items Tests")
    class GetOrderItemsTests {

        @Test
        @DisplayName("GET /orders/{id}/items - Tenant 1 - Should return order items for specified order")
        void getOrderItems_forTenant1_shouldReturnItems() throws Exception {
            mockMvc.perform(get(ORDERS_API_URL + "/" + order1Tenant1.getId() + "/items")
                                    .headers(getAuthHeaders(jwtTenant1)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath(
                            "$[0].productId", anyOf(
                                    is(product1Tenant1.getId().intValue()),
                                    is(product2Tenant1.getId().intValue())
                            )
                    ))
                    .andExpect(jsonPath(
                            "$[1].productId", anyOf(
                                    is(product1Tenant1.getId().intValue()),
                                    is(product2Tenant1.getId().intValue())
                            )
                    ));
        }

        @Test
        @DisplayName("GET /orders/{id}/items - Tenant 1 - Should NOT return items of order from another tenant")
        void getOrderItems_forTenant1_whenAnothersOrder_shouldReturnNotFound() throws Exception {
            mockMvc.perform(get(ORDERS_API_URL + "/" + order1Tenant2.getId() + "/items")
                                    .headers(getAuthHeaders(jwtTenant1)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("GET /orders/{id}/items - Tenant 1 - Should return 404 if order does not exist")
        void getOrderItems_forTenant1_whenOrderNonExistent_shouldReturnNotFound() throws Exception {
            mockMvc.perform(get(ORDERS_API_URL + "/99999/items")
                                    .headers(getAuthHeaders(jwtTenant1)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("POST /orders/{id}/cancel Tests")
    class CancelOrderTests {

        @Test
        @DisplayName("POST /{id}/cancel - Tenant 1 - Should cancel order with valid status")
        void cancelOrder_forTenant1_whenValidStatus_shouldCancelSuccessfully() throws Exception {
            order1Tenant1.setStatus(statusCreated);
            orderRepository.saveAndFlush(order1Tenant1);

            int initialStock1 = product1Tenant1.getStockQuantity();
            int initialStock2 = product2Tenant1.getStockQuantity();

            mockMvc.perform(post(ORDERS_API_URL + "/" + order1Tenant1.getId() + "/cancel")
                                    .param("reason", "Customer requested cancellation")
                                    .headers(getAuthHeaders(jwtTenant1)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id", is(order1Tenant1.getId().intValue())))
                    .andExpect(jsonPath("$.statusName", is("Canceled")))
                    .andExpect(jsonPath("$.comment", containsString("Customer requested cancellation")));

            Order updatedOrder = orderRepository.findById(order1Tenant1.getId()).orElseThrow();
            assertEquals("Canceled", updatedOrder.getStatus().getStatusName());

            Product updatedProduct1 = productRepository.findById(product1Tenant1.getId()).orElseThrow();
            Product updatedProduct2 = productRepository.findById(product2Tenant1.getId()).orElseThrow();

            assertEquals(initialStock1, updatedProduct1.getStockQuantity());
            assertEquals(initialStock2, updatedProduct2.getStockQuantity());

        }

        @Test
        @DisplayName("POST /{id}/cancel - Tenant 1 - Should fail to cancel order in Delivered or Completed status")
        void cancelOrder_forTenant1_whenInvalidStatus_shouldReturnBadRequest() throws Exception {
            OrderStatus statusDelivered = orderStatusRepository.findByStatusName("Delivered")
                    .orElseGet(() -> {
                        OrderStatus status = new OrderStatus();
                        status.setStatusName("Delivered");
                        return orderStatusRepository.saveAndFlush(status);
                    });

            order1Tenant1.setStatus(statusDelivered);
            orderRepository.saveAndFlush(order1Tenant1);

            mockMvc.perform(post(ORDERS_API_URL + "/" + order1Tenant1.getId() + "/cancel")
                                    .param("reason", "Trying to cancel delivered order")
                                    .headers(getAuthHeaders(jwtTenant1)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", containsString("Cannot cancel order that is already Delivered")));
        }

        @Test
        @DisplayName("POST /{id}/cancel - Tenant 1 - Should NOT cancel order from another tenant")
        void cancelOrder_forTenant1_whenAnothersOrder_shouldReturnNotFound() throws Exception {
            mockMvc.perform(post(ORDERS_API_URL + "/" + order1Tenant2.getId() + "/cancel")
                                    .headers(getAuthHeaders(jwtTenant1)))
                    .andExpect(status().isNotFound());
        }
    }


    @Nested
    @DisplayName("POST /orders/{id}/payment Tests")
    class ProcessPaymentTests {

        @Test
        @DisplayName("POST /{id}/payment - Tenant 1 - Should process payment for order in Created status")
        void processPayment_forTenant1_whenCreatedStatus_shouldUpdateStatusToPaid() throws Exception {
            order1Tenant1.setStatus(statusCreated);
            orderRepository.saveAndFlush(order1Tenant1);

            String paymentReference = "TRX123456789";

            mockMvc.perform(post(ORDERS_API_URL + "/" + order1Tenant1.getId() + "/payment")
                                    .param("paymentReference", paymentReference)
                                    .headers(getAuthHeaders(jwtTenant1)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id", is(order1Tenant1.getId().intValue())))
                    .andExpect(jsonPath("$.statusName", is("Paid")))
                    .andExpect(jsonPath("$.comment", containsString(paymentReference)));

            Order updatedOrder = orderRepository.findById(order1Tenant1.getId()).orElseThrow();
            assertEquals("Paid", updatedOrder.getStatus().getStatusName());
        }

        @Test
        @DisplayName("POST /{id}/payment - Tenant 1 - Should fail to process payment for order in non-Created status")
        void processPayment_forTenant1_whenNonCreatedStatus_shouldReturnBadRequest() throws Exception {
            order1Tenant1.setStatus(statusProcessing);
            orderRepository.saveAndFlush(order1Tenant1);

            mockMvc.perform(post(ORDERS_API_URL + "/" + order1Tenant1.getId() + "/payment")
                                    .param("paymentReference", "TRX987654321")
                                    .headers(getAuthHeaders(jwtTenant1)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath(
                            "$.message",
                            containsString("Payment can only be processed for orders in 'Created' status")
                    ));
        }

        @Test
        @DisplayName("POST /{id}/payment - Tenant 1 - Should require payment reference")
        void processPayment_forTenant1_whenNoPaymentReference_shouldReturnBadRequest() throws Exception {
            order1Tenant1.setStatus(statusCreated);
            orderRepository.saveAndFlush(order1Tenant1);

            mockMvc.perform(post(ORDERS_API_URL + "/" + order1Tenant1.getId() + "/payment")
                                    .headers(getAuthHeaders(jwtTenant1)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("POST /{id}/payment - Tenant 1 - Should NOT process payment for another tenant's order")
        void processPayment_forTenant1_whenAnothersOrder_shouldReturnNotFound() throws Exception {
            mockMvc.perform(post(ORDERS_API_URL + "/" + order1Tenant2.getId() + "/payment")
                                    .param("paymentReference", "TRX123456789")
                                    .headers(getAuthHeaders(jwtTenant1)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Order Flow Integration Tests")
    class OrderFlowIntegrationTests {

        @Test
        @DisplayName("Should handle complete order flow: create -> pay -> cancel")
        void shouldHandleCompleteOrderFlow() throws Exception {
            OrderCreateRequest createRequest = new OrderCreateRequest();
            createRequest.setCustomerId(customer1Tenant1.getId());
            createRequest.setAddressId(address1Tenant1.getId());

            List<OrderItemCreateRequest> items = new ArrayList<>();
            OrderItemCreateRequest itemRequest = new OrderItemCreateRequest();
            itemRequest.setProductId(product1Tenant1.getId());
            itemRequest.setQuantity(2);
            items.add(itemRequest);
            createRequest.setOrderItems(items);

            int initialStock = product1Tenant1.getStockQuantity();

            MvcResult result = mockMvc.perform(post(ORDERS_API_URL)
                                                       .headers(getAuthHeaders(jwtTenant1))
                                                       .contentType(MediaType.APPLICATION_JSON)
                                                       .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.statusName", is("Created")))
                    .andReturn();

            String responseContent = result.getResponse().getContentAsString();
            Map<String, Object> orderData = objectMapper.readValue(
                    responseContent, new TypeReference<>() {
                    }
            );
            Long orderId = ((Number) orderData.get("id")).longValue();

            Product updatedProduct = productRepository.findById(product1Tenant1.getId()).orElseThrow();
            assertEquals(initialStock - 2, updatedProduct.getStockQuantity());

            mockMvc.perform(post(ORDERS_API_URL + "/" + orderId + "/payment")
                                    .param("paymentReference", "TEST-TRX-12345")
                                    .headers(getAuthHeaders(jwtTenant1)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.statusName", is("Paid")));

            mockMvc.perform(post(ORDERS_API_URL + "/" + orderId + "/cancel")
                                    .param("reason", "Testing complete flow")
                                    .headers(getAuthHeaders(jwtTenant1)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.statusName", is("Canceled")));

            Product finalProduct = productRepository.findById(product1Tenant1.getId()).orElseThrow();
            assertEquals(initialStock, finalProduct.getStockQuantity());
        }
    }

}
