package diploma.ecommerce.backend.shopbase.unit;

import diploma.ecommerce.backend.shopbase.exception.BadRequestException;
import diploma.ecommerce.backend.shopbase.exception.InsufficientStockException;
import diploma.ecommerce.backend.shopbase.exception.ResourceNotFoundException;
import diploma.ecommerce.backend.shopbase.model.*;
import diploma.ecommerce.backend.shopbase.repository.*;
import diploma.ecommerce.backend.shopbase.service.impl.OrderServiceImpl;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OrderServiceImplTest {

    private static final Long TENANT_ID = 1L;
    private static final Long ORDER_ID = 50L;
    private static final Long CUSTOMER_ID = 60L;
    private static final Long ADDRESS_ID = 70L;
    private static final Long PRODUCT1_ID = 80L;
    private static final Long PRODUCT2_ID = 90L;
    private static final Long STATUS_CREATED_ID = 1L;
    private static final Long STATUS_SHIPPED_ID = 4L;
    private static final Long STATUS_CANCELED_ID = 6L;
    @Captor
    ArgumentCaptor<Order> orderCaptor;
    @Captor
    ArgumentCaptor<Product> productCaptor;
    @Captor
    ArgumentCaptor<List<Product>> productListCaptor; // Добавляем новый captor для списка
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private AddressRepository addressRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private OrderStatusRepository orderStatusRepository;
    @Mock
    private TenantRepository tenantRepository;
    @Mock
    private OrderItemRepository orderItemRepository;
    @InjectMocks
    private OrderServiceImpl orderService;
    private Tenant tenant;
    private User customer;
    private Address address;
    private Product product1;
    private Product product2;
    private OrderStatus statusCreated;
    private OrderStatus statusShipped;
    private OrderStatus statusCanceled;
    private Order order;
    private Order orderRequest;

    @BeforeEach
    void setUp() {
        tenant = new Tenant();
        tenant.setId(TENANT_ID);
        customer = new User();
        customer.setId(CUSTOMER_ID);
        customer.setTenant(tenant);
        address = new Address();
        address.setId(ADDRESS_ID);
        address.setTenant(tenant);
        statusCreated = new OrderStatus();
        statusCreated.setId(STATUS_CREATED_ID);
        statusCreated.setStatusName("Created");
        statusShipped = new OrderStatus();
        statusShipped.setId(STATUS_SHIPPED_ID);
        statusShipped.setStatusName("Shipped");
        statusCanceled = new OrderStatus();
        statusCanceled.setId(STATUS_CANCELED_ID);
        statusCanceled.setStatusName("Canceled");

        product1 = new Product();
        product1.setId(PRODUCT1_ID);
        product1.setTenant(tenant);
        product1.setPrice(new BigDecimal("25.50"));
        product1.setStockQuantity(10);
        product1.setName("P1");
        product2 = new Product();
        product2.setId(PRODUCT2_ID);
        product2.setTenant(tenant);
        product2.setPrice(new BigDecimal("10.00"));
        product2.setStockQuantity(5);
        product2.setName("P2");

        order = new Order();
        order.setId(ORDER_ID);
        order.setTenant(tenant);
        order.setCustomer(customer);
        order.setAddress(address);
        order.setStatus(statusCreated);
        order.setTotalAmount(new BigDecimal("61.00"));
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        OrderItem item1 = new OrderItem();
        item1.setId(101L);
        item1.setTenant(tenant);
        item1.setOrder(order);
        item1.setProduct(product1);
        item1.setQuantity(1);
        item1.setUnitPrice(product1.getPrice());
        item1.setTotalPrice(product1.getPrice());
        OrderItem item2 = new OrderItem();
        item2.setId(102L);
        item2.setTenant(tenant);
        item2.setOrder(order);
        item2.setProduct(product2);
        item2.setQuantity(2);
        item2.setUnitPrice(product2.getPrice());
        item2.setTotalPrice(product2.getPrice().multiply(BigDecimal.valueOf(2)));
        order.setOrderItems(new ArrayList<>(List.of(item1, item2)));

        orderRequest = new Order();
        orderRequest.setCustomer(new User());
        orderRequest.getCustomer().setId(CUSTOMER_ID);
        orderRequest.setAddress(new Address());
        orderRequest.getAddress().setId(ADDRESS_ID);
        OrderItem oiReq1 = new OrderItem();
        oiReq1.setProduct(new Product());
        oiReq1.getProduct().setId(PRODUCT1_ID);
        oiReq1.setQuantity(1);
        OrderItem oiReq2 = new OrderItem();
        oiReq2.setProduct(new Product());
        oiReq2.getProduct().setId(PRODUCT2_ID);
        oiReq2.setQuantity(2);
        orderRequest.setOrderItems(List.of(oiReq1, oiReq2));
    }

    @Nested
    @DisplayName("getAllOrders Tests")
    class GetAllOrdersTests {
        @Test
        void getAllOrders_TenantExists_ReturnsPage() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Order> orderPage = new PageImpl<>(List.of(order), pageable, 1);
            when(tenantRepository.existsById(TENANT_ID)).thenReturn(true);
            when(orderRepository.findAllByTenantId(TENANT_ID, pageable)).thenReturn(orderPage);

            Page<Order> result = orderService.getAllOrders(TENANT_ID, pageable);

            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            assertEquals(order, result.getContent().getFirst());
            verify(tenantRepository).existsById(TENANT_ID);
            verify(orderRepository).findAllByTenantId(TENANT_ID, pageable);
        }

        @Test
        void getAllOrders_TenantNotFound_ThrowsException() {
            Pageable pageable = PageRequest.of(0, 10);
            when(tenantRepository.existsById(TENANT_ID)).thenReturn(false);
            assertThrows(ResourceNotFoundException.class, () -> orderService.getAllOrders(TENANT_ID, pageable));
            verify(orderRepository, never()).findAllByTenantId(anyLong(), any());
        }
    }

    @Nested
    @DisplayName("getOrderById Tests")
    class GetOrderByIdTests {
        @Test
        void getOrderById_Found_ReturnsOrder() {
            when(orderRepository.findByTenantIdAndId(TENANT_ID, ORDER_ID)).thenReturn(Optional.of(order));
            Order result = orderService.getOrderById(TENANT_ID, ORDER_ID);
            assertNotNull(result);
            assertEquals(ORDER_ID, result.getId());

            verify(orderRepository).findByTenantIdAndId(TENANT_ID, ORDER_ID);
        }

        @Test
        void getOrderById_NotFound_ThrowsException() {
            when(orderRepository.findByTenantIdAndId(TENANT_ID, ORDER_ID)).thenReturn(Optional.empty());
            assertThrows(ResourceNotFoundException.class, () -> orderService.getOrderById(TENANT_ID, ORDER_ID));
        }
    }

    @Nested
    @DisplayName("createOrder Tests")
    class CreateOrderTests {
        @BeforeEach
        void setupCreateMocks() {
            when(tenantRepository.findById(TENANT_ID)).thenReturn(Optional.of(tenant));
            when(userRepository.findByTenantIdAndId(TENANT_ID, CUSTOMER_ID)).thenReturn(Optional.of(customer));
            when(addressRepository.findByTenantIdAndId(TENANT_ID, ADDRESS_ID)).thenReturn(Optional.of(address));
            when(orderStatusRepository.findByStatusName("Created")).thenReturn(Optional.of(statusCreated));
            when(productRepository.findByTenantIdAndId(TENANT_ID, PRODUCT1_ID)).thenReturn(Optional.of(product1));
            when(productRepository.findByTenantIdAndId(TENANT_ID, PRODUCT2_ID)).thenReturn(Optional.of(product2));
            when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));
            when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
                Order saved = inv.getArgument(0);
                saved.setId(ORDER_ID + 1);
                if (saved.getOrderItems() != null) {
                    long itemId = 200L;
                    for (OrderItem item : saved.getOrderItems()) {
                        item.setId(itemId);
                        itemId++;
                    }
                }
                return saved;
            });
        }

        @Test
        void createOrder_ValidDataSufficientStock_ReturnsCreatedOrder() {
            Order result = orderService.createOrder(TENANT_ID, orderRequest);

            assertNotNull(result);
            assertNotNull(result.getId());
            assertEquals(TENANT_ID, result.getTenant().getId());
            assertEquals(CUSTOMER_ID, result.getCustomer().getId());
            assertEquals(ADDRESS_ID, result.getAddress().getId());
            assertEquals(STATUS_CREATED_ID, result.getStatus().getId());
            assertEquals(2, result.getOrderItems().size());

            assertEquals(0, new BigDecimal("45.50").compareTo(result.getTotalAmount()));

            verify(tenantRepository).findById(TENANT_ID);
            verify(userRepository).findByTenantIdAndId(TENANT_ID, CUSTOMER_ID);
            verify(addressRepository).findByTenantIdAndId(TENANT_ID, ADDRESS_ID);
            verify(orderStatusRepository).findByStatusName("Created");
            verify(productRepository).findByTenantIdAndId(TENANT_ID, PRODUCT1_ID);
            verify(productRepository).findByTenantIdAndId(TENANT_ID, PRODUCT2_ID);
            verify(productRepository, times(2)).save(productCaptor.capture());
            List<Product> savedProducts = productCaptor.getAllValues();
            assertEquals(
                    9,
                    savedProducts.stream().filter(p -> p.getId().equals(PRODUCT1_ID))
                            .findFirst().get().getStockQuantity()
            );
            assertEquals(
                    3,
                    savedProducts.stream().filter(p -> p.getId().equals(PRODUCT2_ID))
                            .findFirst().get().getStockQuantity()
            );
            verify(orderRepository).save(orderCaptor.capture());
            Order savedOrder = orderCaptor.getValue();
            assertEquals(statusCreated, savedOrder.getStatus());
            assertEquals(customer, savedOrder.getCustomer());
            assertEquals(address, savedOrder.getAddress());
            assertEquals(2, savedOrder.getOrderItems().size());
            assertEquals(0, new BigDecimal("45.50").compareTo(savedOrder.getTotalAmount()));
            OrderItem savedItem1 = savedOrder.getOrderItems().stream().filter(oi -> oi.getProduct().getId().equals(
                    PRODUCT1_ID)).findFirst().get();
            assertEquals(1, savedItem1.getQuantity());
            assertEquals(product1.getPrice(), savedItem1.getUnitPrice());
            OrderItem savedItem2 = savedOrder.getOrderItems().stream().filter(oi -> oi.getProduct().getId().equals(
                    PRODUCT2_ID)).findFirst().get();
            assertEquals(2, savedItem2.getQuantity());
            assertEquals(product2.getPrice(), savedItem2.getUnitPrice());

            assertTrue(result.getId() > ORDER_ID);
        }

        @Test
        void createOrder_InsufficientStock_ThrowsInsufficientStockException() {
            orderRequest.getOrderItems().getFirst().setQuantity(11);

            assertThrows(InsufficientStockException.class, () -> orderService.createOrder(TENANT_ID, orderRequest));
            verify(orderRepository, never()).save(any(Order.class));
            verify(productRepository, never()).save(product1);
            verify(productRepository, times(1)).findByTenantIdAndId(TENANT_ID, PRODUCT1_ID);
            verify(productRepository, atMostOnce()).findByTenantIdAndId(TENANT_ID, PRODUCT2_ID);
        }

        @Test
        void createOrder_ProductNotFound_ThrowsResourceNotFoundException() {
            when(productRepository.findByTenantIdAndId(
                    TENANT_ID,
                    PRODUCT2_ID
            )).thenReturn(Optional.empty());
            assertThrows(ResourceNotFoundException.class, () -> orderService.createOrder(TENANT_ID, orderRequest));
            verify(orderRepository, never()).save(any(Order.class));
            verify(productRepository, atMostOnce()).save(product1);
        }

        @Test
        void createOrder_CustomerNotFound_ThrowsResourceNotFoundException() {
            when(userRepository.findByTenantIdAndId(TENANT_ID, CUSTOMER_ID)).thenReturn(Optional.empty());
            assertThrows(ResourceNotFoundException.class, () -> orderService.createOrder(TENANT_ID, orderRequest));
            verify(orderRepository, never()).save(any(Order.class));
        }

        @Test
        void createOrder_EmptyItems_ThrowsBadRequestException() {
            orderRequest.setOrderItems(new ArrayList<>());
            assertThrows(IllegalArgumentException.class, () -> orderService.createOrder(TENANT_ID, orderRequest));
            verify(orderRepository, never()).save(any(Order.class));
        }
    }

    @Nested
    @DisplayName("updateOrderStatus Tests")
    class UpdateOrderStatusTests {
        @Test
        void updateOrderStatus_ValidData_ReturnsUpdatedOrder() {
            String comment = "Test status update";
            when(orderRepository.findByTenantIdAndId(TENANT_ID, ORDER_ID)).thenReturn(Optional.of(order));
            when(orderStatusRepository.findById(STATUS_SHIPPED_ID)).thenReturn(Optional.of(statusShipped));
            when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

            Order result = orderService.updateOrderStatus(TENANT_ID, ORDER_ID, STATUS_SHIPPED_ID, comment);

            assertNotNull(result);
            assertEquals(ORDER_ID, result.getId());
            assertEquals(statusShipped, result.getStatus());
            assertNotNull(result.getComment());
            assertTrue(result.getComment().contains(comment));

            verify(orderRepository).findByTenantIdAndId(TENANT_ID, ORDER_ID);
            verify(orderStatusRepository).findById(STATUS_SHIPPED_ID);
            verify(orderRepository).save(orderCaptor.capture());
            Order savedOrder = orderCaptor.getValue();
            assertEquals(statusShipped, savedOrder.getStatus());
        }

        @Test
        void updateOrderStatus_ToCanceled_ReplenishesStock() {
            int initialStockP1 = product1.getStockQuantity();
            int initialStockP2 = product2.getStockQuantity();
            int quantityP1 = order.getOrderItems().get(0).getQuantity();
            int quantityP2 = order.getOrderItems().get(1).getQuantity();

            when(orderRepository.findByTenantIdAndId(TENANT_ID, ORDER_ID)).thenReturn(Optional.of(order));
            when(orderStatusRepository.findById(STATUS_CANCELED_ID)).thenReturn(Optional.of(statusCanceled));
            when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
            when(productRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0)); // Изменили на saveAll

            orderService.updateOrderStatus(TENANT_ID, ORDER_ID, STATUS_CANCELED_ID, "Canceled by user");

            // Изменили проверку на saveAll
            verify(productRepository).saveAll(productListCaptor.capture());
            List<Product> savedProducts = productListCaptor.getValue();

            Product savedP1 = savedProducts.stream().filter(p -> p.getId().equals(PRODUCT1_ID)).findFirst().get();
            Product savedP2 = savedProducts.stream().filter(p -> p.getId().equals(PRODUCT2_ID)).findFirst().get();

            assertEquals(initialStockP1 + quantityP1, savedP1.getStockQuantity());
            assertEquals(initialStockP2 + quantityP2, savedP2.getStockQuantity());
        }

        @Test
        void updateOrderStatus_OrderNotFound_ThrowsException() {
            when(orderRepository.findByTenantIdAndId(TENANT_ID, ORDER_ID)).thenReturn(Optional.empty());
            assertThrows(
                    ResourceNotFoundException.class,
                    () -> orderService.updateOrderStatus(TENANT_ID, ORDER_ID, STATUS_SHIPPED_ID, null)
            );
            verify(orderRepository, never()).save(any(Order.class));
            verify(productRepository, never()).save(any(Product.class));
            verify(productRepository, never()).saveAll(anyList()); // Добавили проверку saveAll
        }

        @Test
        void updateOrderStatus_StatusNotFound_ThrowsException() {
            when(orderRepository.findByTenantIdAndId(TENANT_ID, ORDER_ID)).thenReturn(Optional.of(order));
            when(orderStatusRepository.findById(STATUS_SHIPPED_ID)).thenReturn(Optional.empty());
            assertThrows(
                    ResourceNotFoundException.class,
                    () -> orderService.updateOrderStatus(TENANT_ID, ORDER_ID, STATUS_SHIPPED_ID, null)
            );
            verify(orderRepository, never()).save(any(Order.class));
            verify(productRepository, never()).save(any(Product.class));
            verify(productRepository, never()).saveAll(anyList()); // Добавили проверку saveAll
        }
    }

    @Nested
    @DisplayName("deleteOrder Tests")
    class DeleteOrderTests {

        @Test
        void deleteOrder_ValidCanceledOrder_DeletesOrder() {
            order.setStatus(statusCanceled);

            when(orderRepository.findByTenantIdAndId(TENANT_ID, ORDER_ID)).thenReturn(Optional.of(order));

            orderService.deleteOrder(TENANT_ID, ORDER_ID);

            verify(orderRepository).findByTenantIdAndId(TENANT_ID, ORDER_ID);
            verify(orderRepository).delete(order);
        }

        @Test
        void deleteOrder_ValidReturnedOrder_DeletesOrder() {
            OrderStatus statusReturned = new OrderStatus();
            statusReturned.setId(7L);
            statusReturned.setStatusName("Returned");
            order.setStatus(statusReturned);

            when(orderRepository.findByTenantIdAndId(TENANT_ID, ORDER_ID)).thenReturn(Optional.of(order));

            orderService.deleteOrder(TENANT_ID, ORDER_ID);

            verify(orderRepository).delete(order);
        }

        @Test
        void deleteOrder_OrderNotFound_ThrowsException() {
            when(orderRepository.findByTenantIdAndId(TENANT_ID, ORDER_ID)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> orderService.deleteOrder(TENANT_ID, ORDER_ID));

            verify(orderRepository, never()).delete(any(Order.class));
        }

        @Test
        void deleteOrder_InvalidStatus_ThrowsException() {
            when(orderRepository.findByTenantIdAndId(TENANT_ID, ORDER_ID)).thenReturn(Optional.of(order));

            assertThrows(BadRequestException.class, () -> orderService.deleteOrder(TENANT_ID, ORDER_ID));

            verify(orderRepository, never()).delete(any(Order.class));
        }
    }

    @Nested
    @DisplayName("cancelOrder Tests")
    class CancelOrderTests {

        @Test
        void cancelOrder_DeliveredOrder_ThrowsException() {
            OrderStatus statusDelivered = new OrderStatus();
            statusDelivered.setStatusName("Delivered");
            order.setStatus(statusDelivered);

            when(orderRepository.findByTenantIdAndId(TENANT_ID, ORDER_ID)).thenReturn(Optional.of(order));

            assertThrows(ResourceNotFoundException.class, () -> orderService.cancelOrder(TENANT_ID, ORDER_ID, "test"));

            verify(orderRepository, never()).save(any(Order.class));
        }

        @Test
        void cancelOrder_CompletedOrder_ThrowsException() {
            OrderStatus statusCompleted = new OrderStatus();
            statusCompleted.setStatusName("Completed");
            order.setStatus(statusCompleted);

            when(orderRepository.findByTenantIdAndId(TENANT_ID, ORDER_ID)).thenReturn(Optional.of(order));

            assertThrows(ResourceNotFoundException.class, () -> orderService.cancelOrder(TENANT_ID, ORDER_ID, "test"));

            verify(orderRepository, never()).save(any(Order.class));
        }

        @Test
        void cancelOrder_StatusNotFound_ThrowsException() {
            when(orderRepository.findByTenantIdAndId(TENANT_ID, ORDER_ID)).thenReturn(Optional.of(order));
            when(orderStatusRepository.findByStatusName("Canceled")).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> orderService.cancelOrder(TENANT_ID, ORDER_ID, "test"));
        }
    }

    @Nested
    @DisplayName("processOrderPayment Tests")
    class ProcessOrderPaymentTests {

        @Test
        void processOrderPayment_ValidOrder_ReturnsUpdatedOrder() {
            String paymentReference = "PAY-123456789";
            OrderStatus statusPaid = new OrderStatus();
            statusPaid.setId(2L);
            statusPaid.setStatusName("Paid");

            when(orderRepository.findByTenantIdAndId(TENANT_ID, ORDER_ID)).thenReturn(Optional.of(order));
            when(orderStatusRepository.findByStatusName("Paid")).thenReturn(Optional.of(statusPaid));
            when(orderStatusRepository.findById(2L)).thenReturn(Optional.of(statusPaid));
            when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

            Order result = orderService.processOrderPayment(TENANT_ID, ORDER_ID, paymentReference);

            assertNotNull(result);
            assertEquals(statusPaid, result.getStatus());
            assertTrue(result.getComment().contains(paymentReference));
            assertTrue(result.getComment().contains("Payment processed successfully"));

            verify(orderStatusRepository).findByStatusName("Paid");
        }

        @Test
        void processOrderPayment_InvalidStatus_ThrowsException() {
            order.setStatus(statusShipped);

            when(orderRepository.findByTenantIdAndId(TENANT_ID, ORDER_ID)).thenReturn(Optional.of(order));

            assertThrows(BadRequestException.class,
                    () -> orderService.processOrderPayment(TENANT_ID, ORDER_ID, "PAY-123"));

            verify(orderRepository, never()).save(any(Order.class));
        }

        @Test
        void processOrderPayment_StatusNotFound_ThrowsException() {
            when(orderRepository.findByTenantIdAndId(TENANT_ID, ORDER_ID)).thenReturn(Optional.of(order));
            when(orderStatusRepository.findByStatusName("Paid")).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> orderService.processOrderPayment(TENANT_ID, ORDER_ID, "PAY-123"));
        }
    }

    @Nested
    @DisplayName("Additional CreateOrder Edge Cases")
    class CreateOrderAdditionalTests {

        @BeforeEach
        void setupAdditionalMocks() {
            when(tenantRepository.findById(TENANT_ID)).thenReturn(Optional.of(tenant));
            when(userRepository.findByTenantIdAndId(TENANT_ID, CUSTOMER_ID)).thenReturn(Optional.of(customer));
            when(addressRepository.findByTenantIdAndId(TENANT_ID, ADDRESS_ID)).thenReturn(Optional.of(address));
            when(orderStatusRepository.findByStatusName("Created")).thenReturn(Optional.of(statusCreated));
            when(productRepository.findByTenantIdAndId(TENANT_ID, PRODUCT1_ID)).thenReturn(Optional.of(product1));
        }

        @Test
        void createOrder_NullCustomer_ThrowsException() {
            orderRequest.setCustomer(null);

            assertThrows(IllegalArgumentException.class, () -> orderService.createOrder(TENANT_ID, orderRequest));

            verify(orderRepository, never()).save(any(Order.class));
        }

        @Test
        void createOrder_NullCustomerId_ThrowsException() {
            orderRequest.getCustomer().setId(null);

            assertThrows(IllegalArgumentException.class, () -> orderService.createOrder(TENANT_ID, orderRequest));

            verify(orderRepository, never()).save(any(Order.class));
        }

        @Test
        void createOrder_NullAddress_ThrowsException() {
            orderRequest.setAddress(null);

            assertThrows(IllegalArgumentException.class, () -> orderService.createOrder(TENANT_ID, orderRequest));

            verify(orderRepository, never()).save(any(Order.class));
        }

        @Test
        void createOrder_NullAddressId_ThrowsException() {
            orderRequest.getAddress().setId(null);

            assertThrows(IllegalArgumentException.class, () -> orderService.createOrder(TENANT_ID, orderRequest));

            verify(orderRepository, never()).save(any(Order.class));
        }

        @Test
        void createOrder_TenantNotFound_ThrowsException() {
            when(tenantRepository.findById(TENANT_ID)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> orderService.createOrder(TENANT_ID, orderRequest));

            verify(orderRepository, never()).save(any(Order.class));
        }

        @Test
        void createOrder_AddressNotFound_ThrowsException() {
            when(addressRepository.findByTenantIdAndId(TENANT_ID, ADDRESS_ID)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> orderService.createOrder(TENANT_ID, orderRequest));

            verify(orderRepository, never()).save(any(Order.class));
        }

        @Test
        void createOrder_StatusNotFound_ThrowsException() {
            when(orderStatusRepository.findByStatusName("Created")).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> orderService.createOrder(TENANT_ID, orderRequest));

            verify(orderRepository, never()).save(any(Order.class));
        }

        @Test
        void createOrder_NullProductInItem_ThrowsException() {
            orderRequest.getOrderItems().get(0).setProduct(null);

            assertThrows(IllegalArgumentException.class, () -> orderService.createOrder(TENANT_ID, orderRequest));

            verify(orderRepository, never()).save(any(Order.class));
        }

        @Test
        void createOrder_NullProductId_ThrowsException() {
            orderRequest.getOrderItems().get(0).getProduct().setId(null);

            assertThrows(IllegalArgumentException.class, () -> orderService.createOrder(TENANT_ID, orderRequest));

            verify(orderRepository, never()).save(any(Order.class));
        }

        @Test
        void createOrder_NullQuantity_ThrowsException() {
            orderRequest.getOrderItems().get(0).setQuantity(null);

            assertThrows(IllegalArgumentException.class, () -> orderService.createOrder(TENANT_ID, orderRequest));

            verify(orderRepository, never()).save(any(Order.class));
        }

        @Test
        void createOrder_ZeroQuantity_ThrowsException() {
            orderRequest.getOrderItems().get(0).setQuantity(0);

            assertThrows(IllegalArgumentException.class, () -> orderService.createOrder(TENANT_ID, orderRequest));

            verify(orderRepository, never()).save(any(Order.class));
        }

        @Test
        void createOrder_NegativeQuantity_ThrowsException() {
            orderRequest.getOrderItems().get(0).setQuantity(-1);

            assertThrows(IllegalArgumentException.class, () -> orderService.createOrder(TENANT_ID, orderRequest));

            verify(orderRepository, never()).save(any(Order.class));
        }
    }

    @Nested
    @DisplayName("Additional UpdateOrderStatus Tests")
    class UpdateOrderStatusAdditionalTests {

        @Test
        void updateOrderStatus_WithoutComment_UpdatesSuccessfully() {
            when(orderRepository.findByTenantIdAndId(TENANT_ID, ORDER_ID)).thenReturn(Optional.of(order));
            when(orderStatusRepository.findById(STATUS_SHIPPED_ID)).thenReturn(Optional.of(statusShipped));
            when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

            Order result = orderService.updateOrderStatus(TENANT_ID, ORDER_ID, STATUS_SHIPPED_ID, null);

            assertNotNull(result);
            assertEquals(statusShipped, result.getStatus());
            // Comment should remain unchanged
            assertEquals(order.getComment(), result.getComment());
        }

        @Test
        void updateOrderStatus_WithBlankComment_UpdatesSuccessfully() {
            when(orderRepository.findByTenantIdAndId(TENANT_ID, ORDER_ID)).thenReturn(Optional.of(order));
            when(orderStatusRepository.findById(STATUS_SHIPPED_ID)).thenReturn(Optional.of(statusShipped));
            when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

            Order result = orderService.updateOrderStatus(TENANT_ID, ORDER_ID, STATUS_SHIPPED_ID, "   ");

            assertNotNull(result);
            assertEquals(statusShipped, result.getStatus());
            // Comment should remain unchanged for blank comment
            assertEquals(order.getComment(), result.getComment());
        }

        @Test
        void updateOrderStatus_WithExistingComment_AppendsComment() {
            order.setComment("Previous comment");
            String newComment = "Additional comment";

            when(orderRepository.findByTenantIdAndId(TENANT_ID, ORDER_ID)).thenReturn(Optional.of(order));
            when(orderStatusRepository.findById(STATUS_SHIPPED_ID)).thenReturn(Optional.of(statusShipped));
            when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

            Order result = orderService.updateOrderStatus(TENANT_ID, ORDER_ID, STATUS_SHIPPED_ID, newComment);

            assertNotNull(result);
            assertTrue(result.getComment().contains("Previous comment"));
            assertTrue(result.getComment().contains(newComment));
            assertTrue(result.getComment().contains("Status changed to Shipped"));
        }

        @Test
        void updateOrderStatus_ToReturnedStatus_ReplenishesStock() {
            OrderStatus statusReturned = new OrderStatus();
            statusReturned.setId(8L);
            statusReturned.setStatusName("Returned");

            int initialStockP1 = product1.getStockQuantity();
            int initialStockP2 = product2.getStockQuantity();
            int quantityP1 = order.getOrderItems().get(0).getQuantity();
            int quantityP2 = order.getOrderItems().get(1).getQuantity();

            when(orderRepository.findByTenantIdAndId(TENANT_ID, ORDER_ID)).thenReturn(Optional.of(order));
            when(orderStatusRepository.findById(8L)).thenReturn(Optional.of(statusReturned));
            when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
            when(productRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

            orderService.updateOrderStatus(TENANT_ID, ORDER_ID, 8L, "Returned by customer");

            verify(productRepository).saveAll(productListCaptor.capture());
            List<Product> savedProducts = productListCaptor.getValue();

            Product savedP1 = savedProducts.stream().filter(p -> p.getId().equals(PRODUCT1_ID)).findFirst().get();
            Product savedP2 = savedProducts.stream().filter(p -> p.getId().equals(PRODUCT2_ID)).findFirst().get();

            assertEquals(initialStockP1 + quantityP1, savedP1.getStockQuantity());
            assertEquals(initialStockP2 + quantityP2, savedP2.getStockQuantity());
        }

        @Test
        void updateOrderStatus_FromCanceledToCanceled_DoesNotReplenishStock() {
            order.setStatus(statusCanceled);

            when(orderRepository.findByTenantIdAndId(TENANT_ID, ORDER_ID)).thenReturn(Optional.of(order));
            when(orderStatusRepository.findById(STATUS_CANCELED_ID)).thenReturn(Optional.of(statusCanceled));
            when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

            orderService.updateOrderStatus(TENANT_ID, ORDER_ID, STATUS_CANCELED_ID, "Still canceled");

            verify(productRepository, never()).saveAll(anyList());
        }
    }

    @Nested
    @DisplayName("restoreInventoryForOrder Private Method Tests")
    class RestoreInventoryTests {

        @Test
        void updateOrderStatus_CancelWithNullOrderItems_HandlesGracefully() {
            order.setOrderItems(null);

            when(orderRepository.findByTenantIdAndId(TENANT_ID, ORDER_ID)).thenReturn(Optional.of(order));
            when(orderStatusRepository.findById(STATUS_CANCELED_ID)).thenReturn(Optional.of(statusCanceled));
            when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

            Order result = orderService.updateOrderStatus(TENANT_ID, ORDER_ID, STATUS_CANCELED_ID, "Test");

            assertNotNull(result);
            verify(productRepository, never()).saveAll(anyList());
        }

        @Test
        void updateOrderStatus_CancelWithEmptyOrderItems_HandlesGracefully() {
            order.setOrderItems(new ArrayList<>());

            when(orderRepository.findByTenantIdAndId(TENANT_ID, ORDER_ID)).thenReturn(Optional.of(order));
            when(orderStatusRepository.findById(STATUS_CANCELED_ID)).thenReturn(Optional.of(statusCanceled));
            when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

            Order result = orderService.updateOrderStatus(TENANT_ID, ORDER_ID, STATUS_CANCELED_ID, "Test");

            assertNotNull(result);
            verify(productRepository, never()).saveAll(anyList());
        }

        @Test
        void updateOrderStatus_CancelWithNullProductInItem_HandlesGracefully() {
            order.getOrderItems().get(0).setProduct(null);

            when(orderRepository.findByTenantIdAndId(TENANT_ID, ORDER_ID)).thenReturn(Optional.of(order));
            when(orderStatusRepository.findById(STATUS_CANCELED_ID)).thenReturn(Optional.of(statusCanceled));
            when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
            when(productRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

            Order result = orderService.updateOrderStatus(TENANT_ID, ORDER_ID, STATUS_CANCELED_ID, "Test");

            assertNotNull(result);
            verify(productRepository).saveAll(productListCaptor.capture());
            List<Product> savedProducts = productListCaptor.getValue();

            // Should only restore inventory for the item with non-null product
            assertEquals(1, savedProducts.size());
            assertEquals(PRODUCT2_ID, savedProducts.get(0).getId());
        }
    }
}