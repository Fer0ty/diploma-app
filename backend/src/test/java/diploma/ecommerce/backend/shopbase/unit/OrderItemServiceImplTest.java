package diploma.ecommerce.backend.shopbase.unit;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import diploma.ecommerce.backend.shopbase.exception.ResourceNotFoundException;
import diploma.ecommerce.backend.shopbase.model.Order;
import diploma.ecommerce.backend.shopbase.model.OrderItem;
import diploma.ecommerce.backend.shopbase.model.Product;
import diploma.ecommerce.backend.shopbase.model.Tenant;
import diploma.ecommerce.backend.shopbase.repository.OrderItemRepository;
import diploma.ecommerce.backend.shopbase.repository.OrderRepository;
import diploma.ecommerce.backend.shopbase.repository.ProductRepository;
import diploma.ecommerce.backend.shopbase.repository.TenantRepository;
import diploma.ecommerce.backend.shopbase.service.impl.OrderItemServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OrderItemServiceImplTest {

    private static final Long TENANT_ID = 1L;
    private static final Long ORDER_ID = 20L;
    private static final Long PRODUCT_ID = 30L;
    private static final Long ORDER_ITEM_ID = 40L;
    @Mock
    private OrderItemRepository orderItemRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private TenantRepository tenantRepository;
    @InjectMocks
    private OrderItemServiceImpl orderItemService;
    private Tenant tenant;
    private Order order;
    private Product product;
    private OrderItem orderItem;
    private OrderItem orderItemDetails;

    @BeforeEach
    void setUp() {
        tenant = new Tenant();
        tenant.setId(TENANT_ID);
        order = new Order();
        order.setId(ORDER_ID);
        order.setTenant(tenant);
        product = new Product();
        product.setId(PRODUCT_ID);
        product.setTenant(tenant);
        product.setPrice(new BigDecimal("10.00"));
        product.setStockQuantity(10); // Установить достаточное количество товара на складе

        orderItem = new OrderItem();
        orderItem.setId(ORDER_ITEM_ID);
        orderItem.setOrder(order);
        orderItem.setProduct(product);
        orderItem.setTenant(tenant);
        orderItem.setQuantity(2);
        orderItem.setUnitPrice(new BigDecimal("10.00"));
        orderItem.setTotalPrice(new BigDecimal("20.00"));

        orderItemDetails = new OrderItem();
        orderItemDetails.setProduct(new Product());
        orderItemDetails.getProduct().setId(PRODUCT_ID);
        orderItemDetails.setQuantity(3);
    }

    @Nested
    @DisplayName("getOrderItem Tests")
    class GetOrderItemTests {
        @Test
        void getOrderItem_Found_ReturnsItem() {
            when(orderItemRepository.findByTenantIdAndId(TENANT_ID, ORDER_ITEM_ID)).thenReturn(Optional.of(orderItem));
            OrderItem result = orderItemService.getOrderItem(TENANT_ID, ORDER_ITEM_ID);
            assertNotNull(result);
            assertEquals(ORDER_ITEM_ID, result.getId());
            verify(orderItemRepository).findByTenantIdAndId(TENANT_ID, ORDER_ITEM_ID);
        }

        @Test
        void getOrderItem_NotFound_ThrowsException() {
            when(orderItemRepository.findByTenantIdAndId(TENANT_ID, ORDER_ITEM_ID)).thenReturn(Optional.empty());
            assertThrows(
                    ResourceNotFoundException.class,
                    () -> orderItemService.getOrderItem(TENANT_ID, ORDER_ITEM_ID)
            );
            verify(orderItemRepository).findByTenantIdAndId(TENANT_ID, ORDER_ITEM_ID);
        }
    }

    @Nested
    @DisplayName("getOrderItemsByOrderId Tests")
    class GetOrderItemsByOrderIdTests {
        @Test
        void getOrderItemsByOrderId_OrderExists_ReturnsList() {
            when(orderRepository.existsByTenantIdAndId(TENANT_ID, ORDER_ID)).thenReturn(true);
            when(orderItemRepository.findByTenantIdAndOrderId(TENANT_ID, ORDER_ID)).thenReturn(List.of(orderItem));
            List<OrderItem> result = orderItemService.getOrderItemsByOrderId(TENANT_ID, ORDER_ID);
            assertNotNull(result);
            assertEquals(1, result.size());
            verify(orderRepository).existsByTenantIdAndId(TENANT_ID, ORDER_ID);
            verify(orderItemRepository).findByTenantIdAndOrderId(TENANT_ID, ORDER_ID);
        }

        @Test
        void getOrderItemsByOrderId_OrderNotFound_ThrowsException() {
            when(orderRepository.existsByTenantIdAndId(TENANT_ID, ORDER_ID)).thenReturn(false);
            assertThrows(
                    ResourceNotFoundException.class,
                    () -> orderItemService.getOrderItemsByOrderId(TENANT_ID, ORDER_ID)
            );
            verify(orderRepository).existsByTenantIdAndId(TENANT_ID, ORDER_ID);
            verify(orderItemRepository, never()).findByTenantIdAndOrderId(anyLong(), anyLong());
        }
    }

    @Nested
    @DisplayName("createOrderItem Tests")
    class CreateOrderItemTests {
        @Test
        void createOrderItem_ValidData_ReturnsCreatedItem() {
            OrderItem orderItemToSave = new OrderItem();
            orderItemToSave.setProduct(new Product());
            orderItemToSave.getProduct().setId(PRODUCT_ID);
            orderItemToSave.setQuantity(1);
            assertNull(orderItemToSave.getId());

            // Создаем продукт с достаточным количеством на складе
            Product productWithStock = new Product();
            productWithStock.setId(PRODUCT_ID);
            productWithStock.setTenant(tenant);
            productWithStock.setPrice(new BigDecimal("10.00"));
            productWithStock.setStockQuantity(10); // Достаточное количество

            orderItemDetails.setQuantity(1);
            when(tenantRepository.findById(TENANT_ID)).thenReturn(Optional.of(tenant));
            when(orderRepository.findByTenantIdAndId(TENANT_ID, ORDER_ID)).thenReturn(Optional.of(order));
            when(productRepository.findByTenantIdAndId(TENANT_ID, PRODUCT_ID)).thenReturn(Optional.of(productWithStock));
            when(orderItemRepository.findByTenantIdAndOrderIdAndProductId(TENANT_ID, ORDER_ID, PRODUCT_ID)).thenReturn(
                    Optional.empty());
            when(orderItemRepository.save(any(OrderItem.class))).thenAnswer(inv -> {
                OrderItem saved = inv.getArgument(0);
                saved.setId(ORDER_ITEM_ID + 1);
                return saved;
            });
            // Настраиваем сохранение обновленного продукта
            when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

            ArgumentCaptor<OrderItem> captor = ArgumentCaptor.forClass(OrderItem.class);

            OrderItem result = orderItemService.createOrderItem(TENANT_ID, ORDER_ID, orderItemToSave);

            assertNotNull(result);
            assertNotNull(result.getId());
            assertEquals(TENANT_ID, result.getTenant().getId());
            assertEquals(ORDER_ID, result.getOrder().getId());
            assertEquals(PRODUCT_ID, result.getProduct().getId());
            assertEquals(1, result.getQuantity());
            assertEquals(new BigDecimal("10.00"), result.getUnitPrice());
            assertEquals(new BigDecimal("10.00"), result.getTotalPrice());
            verify(orderItemRepository).save(captor.capture());
            verify(productRepository).save(any(Product.class)); // Проверяем сохранение продукта
        }

        @Test
        void createOrderItem_ProductAlreadyInOrder_ThrowsException() {
            when(tenantRepository.findById(TENANT_ID)).thenReturn(Optional.of(tenant));
            when(orderRepository.findByTenantIdAndId(
                    TENANT_ID,
                    ORDER_ID
            )).thenReturn(Optional.of(order));
            when(productRepository.findByTenantIdAndId(
                    TENANT_ID,
                    PRODUCT_ID
            )).thenReturn(Optional.of(product));
            when(orderItemRepository.findByTenantIdAndOrderIdAndProductId(TENANT_ID, ORDER_ID, PRODUCT_ID)).thenReturn(
                    Optional.of(orderItem));

            assertThrows(
                    IllegalArgumentException.class,
                    () -> orderItemService.createOrderItem(TENANT_ID, ORDER_ID, orderItemDetails)
            );
            verify(orderItemRepository, never()).save(any(OrderItem.class));
        }

        @Test
        void createOrderItem_InvalidQuantity_ThrowsException() {
            orderItemDetails.setQuantity(0);
            assertThrows(
                    IllegalArgumentException.class,
                    () -> orderItemService.createOrderItem(TENANT_ID, ORDER_ID, orderItemDetails)
            );
            orderItemDetails.setQuantity(null);
            assertThrows(
                    IllegalArgumentException.class,
                    () -> orderItemService.createOrderItem(TENANT_ID, ORDER_ID, orderItemDetails)
            );
            verify(orderItemRepository, never()).save(any(OrderItem.class));
        }

        @Test
        void createOrderItem_ProductNotFound_ThrowsException() {
            when(tenantRepository.findById(TENANT_ID)).thenReturn(Optional.of(tenant));
            when(orderRepository.findByTenantIdAndId(TENANT_ID, ORDER_ID)).thenReturn(Optional.of(order));
            when(productRepository.findByTenantIdAndId(
                    TENANT_ID,
                    PRODUCT_ID
            )).thenReturn(Optional.empty());

            assertThrows(
                    ResourceNotFoundException.class,
                    () -> orderItemService.createOrderItem(TENANT_ID, ORDER_ID, orderItemDetails)
            );
            verify(orderItemRepository, never()).save(any(OrderItem.class));
        }
    }

    @Nested
    @DisplayName("updateOrderItem Tests")
    class UpdateOrderItemTests {
        @Test
        void updateOrderItem_ValidData_ReturnsUpdatedItem() {
            // Создаем продукт с достаточным количеством на складе для увеличения количества в заказе
            Product productWithStock = new Product();
            productWithStock.setId(PRODUCT_ID);
            productWithStock.setTenant(tenant);
            productWithStock.setPrice(new BigDecimal("10.00"));
            productWithStock.setStockQuantity(10); // Достаточное количество

            // Обновляем orderItem чтобы использовать продукт с запасом
            orderItem.setProduct(productWithStock);

            when(orderItemRepository.findByTenantIdAndId(TENANT_ID, ORDER_ITEM_ID)).thenReturn(Optional.of(orderItem));
            when(productRepository.findByTenantIdAndId(TENANT_ID, PRODUCT_ID)).thenReturn(Optional.of(productWithStock));
            when(orderItemRepository.save(any(OrderItem.class))).thenAnswer(inv -> inv.getArgument(0));
            when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

            ArgumentCaptor<OrderItem> captor = ArgumentCaptor.forClass(OrderItem.class);
            int newQuantity = 3;
            orderItemDetails.setQuantity(newQuantity);

            OrderItem result = orderItemService.updateOrderItem(TENANT_ID, ORDER_ITEM_ID, orderItemDetails);

            assertNotNull(result);
            assertEquals(ORDER_ITEM_ID, result.getId());
            assertEquals(newQuantity, result.getQuantity());
            assertEquals(new BigDecimal("10.00"), result.getUnitPrice());
            assertEquals(new BigDecimal("30.00"), result.getTotalPrice());

            verify(orderItemRepository).findByTenantIdAndId(TENANT_ID, ORDER_ITEM_ID);
            verify(orderItemRepository).save(captor.capture());
            verify(productRepository).save(any(Product.class)); // Проверяем сохранение обновленного продукта

            OrderItem savedItem = captor.getValue();
            assertEquals(ORDER_ITEM_ID, savedItem.getId());
            assertEquals(newQuantity, savedItem.getQuantity());
        }

        @Test
        void updateOrderItem_ItemNotFound_ThrowsException() {
            when(orderItemRepository.findByTenantIdAndId(TENANT_ID, ORDER_ITEM_ID)).thenReturn(Optional.empty());
            assertThrows(
                    ResourceNotFoundException.class,
                    () -> orderItemService.updateOrderItem(TENANT_ID, ORDER_ITEM_ID, orderItemDetails)
            );
            verify(orderItemRepository, never()).save(any(OrderItem.class));
        }

        @Test
        void updateOrderItem_InvalidQuantity_ThrowsException() {
            orderItemDetails.setQuantity(0);
            assertThrows(
                    IllegalArgumentException.class,
                    () -> orderItemService.updateOrderItem(TENANT_ID, ORDER_ITEM_ID, orderItemDetails)
            );
            orderItemDetails.setQuantity(null);
            assertThrows(
                    IllegalArgumentException.class,
                    () -> orderItemService.updateOrderItem(TENANT_ID, ORDER_ITEM_ID, orderItemDetails)
            );
            verify(orderItemRepository, never()).save(any(OrderItem.class));
        }
    }

    @Nested
    @DisplayName("deleteOrderItem Tests")
    class DeleteOrderItemTests {
        @Test
        void deleteOrderItem_ItemFound_DeletesItem() {
            when(orderItemRepository.findByTenantIdAndId(TENANT_ID, ORDER_ITEM_ID)).thenReturn(Optional.of(orderItem));
            doNothing().when(orderItemRepository).delete(any(OrderItem.class));
            ArgumentCaptor<OrderItem> captor = ArgumentCaptor.forClass(OrderItem.class);

            orderItemService.deleteOrderItem(TENANT_ID, ORDER_ITEM_ID);

            verify(orderItemRepository).findByTenantIdAndId(TENANT_ID, ORDER_ITEM_ID);
            verify(orderItemRepository).delete(captor.capture());
            assertEquals(orderItem, captor.getValue());
        }

        @Test
        void deleteOrderItem_ItemNotFound_ThrowsException() {
            when(orderItemRepository.findByTenantIdAndId(TENANT_ID, ORDER_ITEM_ID)).thenReturn(Optional.empty());

            assertThrows(
                    ResourceNotFoundException.class,
                    () -> orderItemService.deleteOrderItem(TENANT_ID, ORDER_ITEM_ID)
            );
            verify(orderItemRepository, never()).delete(any(OrderItem.class));
        }
    }
}