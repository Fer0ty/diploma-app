package diploma.ecommerce.backend.shopbase.integration;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import diploma.ecommerce.backend.shopbase.dto.request.OrderItemCreateRequest;
import diploma.ecommerce.backend.shopbase.dto.request.OrderItemUpdateRequest;
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

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("OrderItemController Integration Tests")
public class OrderItemControllerIntegrationTest extends BaseIntegrationTest {

    private static final String ORDER_ITEMS_API_URL = "/api/v1/order-items";
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
    private User customer1Tenant1;
    private Address address1Tenant1;
    private OrderStatus statusCreated, statusProcessing;
    private Order order1Tenant1, order1Tenant2;
    private OrderItem item1Order1Tenant1, item2Order1Tenant1, item1Order1Tenant2;


    @BeforeEach
    void setUpOrderItemTestData() {
        TenantContext.clear();

        statusCreated = orderStatusRepository.findByStatusName("Created")
                .orElseGet(() -> {
                    OrderStatus status = new OrderStatus();
                    status.setStatusName("Created");
                    return orderStatusRepository.saveAndFlush(status);
                });

        statusProcessing = orderStatusRepository.findByStatusName("Processing")
                .orElseGet(() -> {
                    OrderStatus status = new OrderStatus();
                    status.setStatusName("Processing");
                    return orderStatusRepository.saveAndFlush(status);
                });

        product1Tenant1 = new Product();
        product1Tenant1.setTenant(tenant1);
        product1Tenant1.setName("OI Test Laptop T1");
        product1Tenant1.setPrice(new BigDecimal("1300.00"));
        product1Tenant1.setStockQuantity(20);
        product1Tenant1.setActive(true);
        product1Tenant1 = productRepository.saveAndFlush(product1Tenant1);

        product2Tenant1 = new Product();
        product2Tenant1.setTenant(tenant1);
        product2Tenant1.setName("OI Test Mouse T1");
        product2Tenant1.setPrice(new BigDecimal("30.00"));
        product2Tenant1.setStockQuantity(40);
        product2Tenant1.setActive(true);
        product2Tenant1 = productRepository.saveAndFlush(product2Tenant1);

        customer1Tenant1 = new User();
        customer1Tenant1.setTenant(tenant1);
        customer1Tenant1.setFirstName("OrderUser");
        customer1Tenant1.setLastName("ItemTest");
        customer1Tenant1.setEmail("orderitem@tenant1.com");
        customer1Tenant1.setActive(true);
        customer1Tenant1.setCreatedAt(LocalDateTime.now());
        customer1Tenant1 = userRepository.saveAndFlush(customer1Tenant1);

        address1Tenant1 = new Address();
        address1Tenant1.setTenant(tenant1);
        address1Tenant1.setCountry("CountryOI");
        address1Tenant1.setCity("CityOI");
        address1Tenant1.setStreet("StreetOI");
        address1Tenant1.setHouseNumber("1OI");
        address1Tenant1.setPostalCode("01010");
        address1Tenant1 = addressRepository.saveAndFlush(address1Tenant1);

        order1Tenant1 = new Order();
        order1Tenant1.setTenant(tenant1);
        order1Tenant1.setCustomer(customer1Tenant1);
        order1Tenant1.setAddress(address1Tenant1);
        order1Tenant1.setStatus(statusCreated);
        order1Tenant1.setTotalAmount(BigDecimal.ZERO);
        order1Tenant1.setCreatedAt(LocalDateTime.now());
        order1Tenant1.setUpdatedAt(LocalDateTime.now());
        order1Tenant1 = orderRepository.saveAndFlush(order1Tenant1);

        item1Order1Tenant1 = new OrderItem();
        item1Order1Tenant1.setTenant(tenant1);
        item1Order1Tenant1.setOrder(order1Tenant1);
        item1Order1Tenant1.setProduct(product1Tenant1);
        item1Order1Tenant1.setQuantity(1);
        item1Order1Tenant1.setUnitPrice(product1Tenant1.getPrice());
        item1Order1Tenant1.setTotalPrice(product1Tenant1.getPrice());
        item1Order1Tenant1 = orderItemRepository.saveAndFlush(item1Order1Tenant1);

        item2Order1Tenant1 = new OrderItem();
        item2Order1Tenant1.setTenant(tenant1);
        item2Order1Tenant1.setOrder(order1Tenant1);
        item2Order1Tenant1.setProduct(product2Tenant1);
        item2Order1Tenant1.setQuantity(2);
        item2Order1Tenant1.setUnitPrice(product2Tenant1.getPrice());
        item2Order1Tenant1.setTotalPrice(product2Tenant1.getPrice().multiply(BigDecimal.valueOf(2)));
        item2Order1Tenant1 = orderItemRepository.saveAndFlush(item2Order1Tenant1);

        order1Tenant1.setTotalAmount(item1Order1Tenant1.getTotalPrice().add(item2Order1Tenant1.getTotalPrice()));
        orderRepository.saveAndFlush(order1Tenant1);

        product1Tenant2 = new Product();
        product1Tenant2.setTenant(tenant2);
        product1Tenant2.setName("Product For T2 (OI test)");
        product1Tenant2.setPrice(new BigDecimal("10.00"));
        product1Tenant2.setStockQuantity(5);
        product1Tenant2.setActive(true);
        product1Tenant2 = productRepository.saveAndFlush(product1Tenant2);

        User customerForT2 = new User();
        customerForT2.setTenant(tenant2);
        customerForT2.setFirstName("CustT2");
        customerForT2.setLastName("UserT2");
        customerForT2.setEmail("custt2@tenant2.com");
        customerForT2.setActive(true);
        customerForT2.setCreatedAt(LocalDateTime.now());
        customerForT2 = userRepository.saveAndFlush(customerForT2);

        Address addressForT2 = new Address();
        addressForT2.setTenant(tenant2);
        addressForT2.setCountry("CT2");
        addressForT2.setCity("CityT2OI");
        addressForT2.setStreet("StreetT2OI");
        addressForT2.setHouseNumber("1T2OI");
        addressForT2.setPostalCode("22200");
        addressForT2 = addressRepository.saveAndFlush(addressForT2);


        order1Tenant2 = new Order();
        order1Tenant2.setTenant(tenant2);
        order1Tenant2.setCustomer(customerForT2);
        order1Tenant2.setAddress(addressForT2);
        order1Tenant2.setStatus(statusCreated);
        order1Tenant2.setTotalAmount(BigDecimal.ZERO);
        order1Tenant2.setCreatedAt(LocalDateTime.now());
        order1Tenant2.setUpdatedAt(LocalDateTime.now());
        order1Tenant2 = orderRepository.saveAndFlush(order1Tenant2);

        item1Order1Tenant2 = new OrderItem();
        item1Order1Tenant2.setTenant(tenant2);
        item1Order1Tenant2.setOrder(order1Tenant2);
        item1Order1Tenant2.setProduct(product1Tenant2);
        item1Order1Tenant2.setQuantity(1);
        item1Order1Tenant2.setUnitPrice(product1Tenant2.getPrice());
        item1Order1Tenant2.setTotalPrice(product1Tenant2.getPrice());
        item1Order1Tenant2 = orderItemRepository.saveAndFlush(item1Order1Tenant2);
        order1Tenant2.setTotalAmount(item1Order1Tenant2.getTotalPrice());
        orderRepository.saveAndFlush(order1Tenant2);
    }

    @AfterEach
    void clearTenantContextAfterOrderItemTest() {
        TenantContext.clear();
    }

    @Nested
    @DisplayName("GET /order-items/{itemId} Tests")
    class GetOrderItemByIdTests {

        @Test
        @DisplayName("GET /{itemId} - Tenant 1 - Should return order item by ID")
        void getOrderItemById_forTenant1_whenOwnItem_shouldReturnItem() throws Exception {
            mockMvc.perform(get(ORDER_ITEMS_API_URL + "/" + item1Order1Tenant1.getId())
                                    .headers(getAuthHeaders(jwtTenant1)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id", is(item1Order1Tenant1.getId().intValue())))
                    .andExpect(jsonPath("$.productId", is(product1Tenant1.getId().intValue())))
                    .andExpect(jsonPath("$.quantity", is(item1Order1Tenant1.getQuantity())));
        }

        @Test
        @DisplayName("GET /{itemId} - Tenant 1 - Should NOT return another tenant's order item")
        void getOrderItemById_forTenant1_whenAnothersItem_shouldReturnNotFound() throws Exception {
            mockMvc.perform(get(ORDER_ITEMS_API_URL + "/" + item1Order1Tenant2.getId())
                                    .headers(getAuthHeaders(jwtTenant1)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("GET /{itemId} - Tenant 1 - Should return 404 for non-existent order item")
        void getOrderItemById_forTenant1_whenItemNonExistent_shouldReturnNotFound() throws Exception {
            mockMvc.perform(get(ORDER_ITEMS_API_URL + "/99999")
                                    .headers(getAuthHeaders(jwtTenant1)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("GET /{itemId} - Should require authentication")
        void getOrderItemById_shouldRequireAuthentication() throws Exception {
            mockMvc.perform(get(ORDER_ITEMS_API_URL + "/" + item1Order1Tenant1.getId()))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /order-items/order/{orderId} Tests")
    class GetOrderItemsByOrderIdTests {

        @Test
        @DisplayName("GET /order/{orderId} - Tenant 1 - Should return all order items for order")
        void getOrderItemsByOrderId_forTenant1_whenOwnOrder_shouldReturnAllItems() throws Exception {
            mockMvc.perform(get(ORDER_ITEMS_API_URL + "/order/" + order1Tenant1.getId())
                                    .headers(getAuthHeaders(jwtTenant1)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].id", is(item1Order1Tenant1.getId().intValue())))
                    .andExpect(jsonPath("$[1].id", is(item2Order1Tenant1.getId().intValue())));
        }

        @Test
        @DisplayName("GET /order/{orderId} - Tenant 1 - Should NOT return items from another tenant's order")
        void getOrderItemsByOrderId_forTenant1_whenAnothersOrder_shouldReturnNotFound() throws Exception {
            mockMvc.perform(get(ORDER_ITEMS_API_URL + "/order/" + order1Tenant2.getId())
                                    .headers(getAuthHeaders(jwtTenant1)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("POST /order-items/order/{orderId} Tests")
    class CreateOrderItemTests {

        @Test
        @DisplayName("POST /order/{orderId} - Tenant 1 - Should create new order item")
        void createOrderItem_forTenant1_whenValidData_shouldCreate() throws Exception {
            Product newProduct = new Product();
            newProduct.setTenant(tenant1);
            newProduct.setName("New Test Product For Create");
            newProduct.setPrice(new BigDecimal("50.00"));
            newProduct.setStockQuantity(10);
            newProduct.setActive(true);
            newProduct = productRepository.saveAndFlush(newProduct);

            OrderItemCreateRequest createRequest = new OrderItemCreateRequest();
            createRequest.setProductId(newProduct.getId());
            createRequest.setQuantity(3);

            int initialProductStock = newProduct.getStockQuantity();
            BigDecimal initialOrderTotal = order1Tenant1.getTotalAmount();

            BigDecimal expectedItemTotal = newProduct.getPrice().multiply(BigDecimal.valueOf(3));

            BigDecimal expectedOrderTotal = initialOrderTotal.add(expectedItemTotal);

            mockMvc.perform(post(ORDER_ITEMS_API_URL + "/order/" + order1Tenant1.getId())
                                    .headers(getAuthHeaders(jwtTenant1))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.productId", is(newProduct.getId().intValue())))
                    .andExpect(jsonPath("$.quantity", is(3)))
                    .andExpect(jsonPath("$.totalPrice", is(expectedItemTotal.doubleValue())));

            Product updatedProduct = productRepository.findById(newProduct.getId()).orElseThrow();
            assertEquals(initialProductStock - 3, updatedProduct.getStockQuantity());

            Order updatedOrder = orderRepository.findById(order1Tenant1.getId()).orElseThrow();
            assertEquals(0, expectedOrderTotal.compareTo(updatedOrder.getTotalAmount()));
        }


        @Test
        @DisplayName("POST /order/{orderId} - Tenant 1 - Should return 400 when insufficient stock")
        void createOrderItem_forTenant1_whenInsufficientStock_shouldReturnBadRequest() throws Exception {
            OrderItemCreateRequest createRequest = new OrderItemCreateRequest();
            createRequest.setProductId(product1Tenant1.getId());
            createRequest.setQuantity(product1Tenant1.getStockQuantity() + 10);

            mockMvc.perform(post(ORDER_ITEMS_API_URL + "/order/" + order1Tenant1.getId())
                                    .headers(getAuthHeaders(jwtTenant1))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("POST /order/{orderId} - Tenant 1 - Should return 400 when product already in order")
        void createOrderItem_forTenant1_whenProductAlreadyInOrder_shouldReturnBadRequest() throws Exception {
            OrderItemCreateRequest createRequest = new OrderItemCreateRequest();
            createRequest.setProductId(product1Tenant1.getId());
            createRequest.setQuantity(1);

            mockMvc.perform(post(ORDER_ITEMS_API_URL + "/order/" + order1Tenant1.getId())
                                    .headers(getAuthHeaders(jwtTenant1))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("POST /order/{orderId} - Tenant 1 - Should return 404 for non-existent order")
        void createOrderItem_forTenant1_whenOrderNonExistent_shouldReturnNotFound() throws Exception {
            OrderItemCreateRequest createRequest = new OrderItemCreateRequest();
            createRequest.setProductId(product1Tenant1.getId());
            createRequest.setQuantity(1);

            mockMvc.perform(post(ORDER_ITEMS_API_URL + "/order/99999")
                                    .headers(getAuthHeaders(jwtTenant1))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("POST /order/{orderId} - Tenant 1 - Should return 404 for non-existent product")
        void createOrderItem_forTenant1_whenProductNonExistent_shouldReturnNotFound() throws Exception {
            OrderItemCreateRequest createRequest = new OrderItemCreateRequest();
            createRequest.setProductId(99999L);
            createRequest.setQuantity(1);

            mockMvc.perform(post(ORDER_ITEMS_API_URL + "/order/" + order1Tenant1.getId())
                                    .headers(getAuthHeaders(jwtTenant1))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PUT /order-items/{itemId} Tests")
    class UpdateOrderItemTests {

        @Test
        @DisplayName("PUT /{itemId} - Tenant 1 - Should update order item quantity")
        void updateOrderItem_forTenant1_whenOwnItem_shouldUpdateQuantity() throws Exception {
            OrderItemUpdateRequest updateRequest = new OrderItemUpdateRequest();
            int newQuantity = item1Order1Tenant1.getQuantity() + 1;
            updateRequest.setQuantity(newQuantity);

            int initialProductStock = product1Tenant1.getStockQuantity();

            BigDecimal initialOrderTotal = order1Tenant1.getTotalAmount();

            BigDecimal expectedItemTotal = item1Order1Tenant1.getUnitPrice().multiply(BigDecimal.valueOf(newQuantity));

            BigDecimal orderTotalDifference = item1Order1Tenant1.getUnitPrice();
            BigDecimal expectedOrderTotal = initialOrderTotal.add(orderTotalDifference);

            mockMvc.perform(put(ORDER_ITEMS_API_URL + "/" + item1Order1Tenant1.getId())
                                    .headers(getAuthHeaders(jwtTenant1))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id", is(item1Order1Tenant1.getId().intValue())))
                    .andExpect(jsonPath("$.quantity", is(newQuantity)))
                    .andExpect(jsonPath("$.totalPrice", is(expectedItemTotal.doubleValue())));

            OrderItem updatedItem = orderItemRepository.findById(item1Order1Tenant1.getId()).orElseThrow();
            assertEquals(newQuantity, updatedItem.getQuantity());
            assertEquals(0, expectedItemTotal.compareTo(updatedItem.getTotalPrice()));

            Product updatedProduct = productRepository.findById(product1Tenant1.getId()).orElseThrow();
            assertEquals(initialProductStock - 1, updatedProduct.getStockQuantity());

            Order updatedOrder = orderRepository.findById(order1Tenant1.getId()).orElseThrow();
            assertEquals(0, expectedOrderTotal.compareTo(updatedOrder.getTotalAmount()));
        }

        @Test
        @DisplayName("PUT /{itemId} - Tenant 1 - Should reject update with invalid quantity (zero or negative)")
        void updateOrderItem_forTenant1_whenInvalidQuantity_shouldReturnBadRequest() throws Exception {
            OrderItemUpdateRequest updateRequest = new OrderItemUpdateRequest();
            updateRequest.setQuantity(0);

            mockMvc.perform(put(ORDER_ITEMS_API_URL + "/" + item1Order1Tenant1.getId())
                                    .headers(getAuthHeaders(jwtTenant1))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.fieldErrors.quantity", containsString("Quantity must be at least 1")));
        }

        @Test
        @DisplayName("PUT /{itemId} - Tenant 1 - Should NOT update another tenant's order item")
        void updateOrderItem_forTenant1_whenAnothersItem_shouldReturnNotFound() throws Exception {
            OrderItemUpdateRequest updateRequest = new OrderItemUpdateRequest();
            updateRequest.setQuantity(5);

            mockMvc.perform(put(ORDER_ITEMS_API_URL + "/" + item1Order1Tenant2.getId())
                                    .headers(getAuthHeaders(jwtTenant1))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("PUT /{itemId} - Tenant 1 - Should return 400 when insufficient stock")
        void updateOrderItem_forTenant1_whenInsufficientStock_shouldReturnBadRequest() throws Exception {
            OrderItemUpdateRequest updateRequest = new OrderItemUpdateRequest();
            updateRequest.setQuantity(item1Order1Tenant1.getQuantity() + product1Tenant1.getStockQuantity() + 10);

            mockMvc.perform(put(ORDER_ITEMS_API_URL + "/" + item1Order1Tenant1.getId())
                                    .headers(getAuthHeaders(jwtTenant1))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("DELETE /order-items/{itemId} Tests")
    class DeleteOrderItemTests {

        @Test
        @DisplayName("DELETE /{itemId} - Tenant 1 - Should delete own order item")
        void deleteOrderItem_forTenant1_whenOwnItem_shouldDelete() throws Exception {
            Long itemIdToDelete = item1Order1Tenant1.getId();
            BigDecimal itemAmount = item1Order1Tenant1.getTotalPrice();
            BigDecimal initialOrderTotal = order1Tenant1.getTotalAmount();

            int initialProductStock = product1Tenant1.getStockQuantity();
            int itemQuantity = item1Order1Tenant1.getQuantity();

            mockMvc.perform(delete(ORDER_ITEMS_API_URL + "/" + itemIdToDelete)
                                    .headers(getAuthHeaders(jwtTenant1)))
                    .andExpect(status().isNoContent());

            assertFalse(orderItemRepository.existsById(itemIdToDelete));

            Order updatedOrder = orderRepository.findById(order1Tenant1.getId()).orElseThrow();
            assertEquals(0, initialOrderTotal.subtract(itemAmount).compareTo(updatedOrder.getTotalAmount()));

            Product updatedProduct = productRepository.findById(product1Tenant1.getId()).orElseThrow();
            assertEquals(initialProductStock + itemQuantity, updatedProduct.getStockQuantity());
        }

        @Test
        @DisplayName("DELETE /{itemId} - Tenant 1 - Should NOT delete another tenant's order item")
        void deleteOrderItem_forTenant1_whenAnothersItem_shouldReturnNotFound() throws Exception {
            mockMvc.perform(delete(ORDER_ITEMS_API_URL + "/" + item1Order1Tenant2.getId())
                                    .headers(getAuthHeaders(jwtTenant1)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("DELETE /{itemId} - Tenant 1 - Should return 404 for non-existent order item")
        void deleteOrderItem_forTenant1_whenItemNonExistent_shouldReturnNotFound() throws Exception {
            mockMvc.perform(delete(ORDER_ITEMS_API_URL + "/99999")
                                    .headers(getAuthHeaders(jwtTenant1)))
                    .andExpect(status().isNotFound());
        }
    }
}
