package diploma.ecommerce.backend.shopbase.unit;

import java.util.List;
import java.util.Optional;

import diploma.ecommerce.backend.shopbase.exception.DataIntegrityViolationException;
import diploma.ecommerce.backend.shopbase.exception.ResourceNotFoundException;
import diploma.ecommerce.backend.shopbase.exception.StatusNameAlreadyExistsException;
import diploma.ecommerce.backend.shopbase.model.OrderStatus;
import diploma.ecommerce.backend.shopbase.repository.OrderRepository;
import diploma.ecommerce.backend.shopbase.repository.OrderStatusRepository;
import diploma.ecommerce.backend.shopbase.service.impl.OrderStatusServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderStatusServiceImplTest {

    private static final Long STATUS_ID = 1L;
    @Mock
    private OrderStatusRepository orderStatusRepository;
    @Mock
    private OrderRepository orderRepository;
    @InjectMocks
    private OrderStatusServiceImpl orderStatusService;
    private OrderStatus status;
    private OrderStatus statusDetails;

    @BeforeEach
    void setUp() {
        status = new OrderStatus();
        status.setId(STATUS_ID);
        status.setStatusName("Created");
        statusDetails = new OrderStatus();
        statusDetails.setStatusName("Pending");
    }

    @Nested
    @DisplayName("getAllOrderStatuses Tests")
    class GetAllOrderStatusesTests { /* Без изменений */
        @Test
        void getAllOrderStatuses_ReturnsList() {
            when(orderStatusRepository.findAll()).thenReturn(List.of(status));
            List<OrderStatus> result = orderStatusService.getAllOrderStatuses();
            assertNotNull(result);
            assertEquals(1, result.size());
            verify(orderStatusRepository).findAll();
        }
    }

    @Nested
    @DisplayName("getOrderStatus Tests")
    class GetOrderStatusTests { /* Без изменений */
        @Test
        void getOrderStatus_Found_ReturnsStatus() {
            when(orderStatusRepository.findById(STATUS_ID)).thenReturn(Optional.of(status));
            OrderStatus result = orderStatusService.getOrderStatusById(STATUS_ID);
            assertNotNull(result);
            assertEquals(STATUS_ID, result.getId());
            verify(orderStatusRepository).findById(STATUS_ID);
        }

        @Test
        void getOrderStatus_NotFound_ThrowsException() {
            when(orderStatusRepository.findById(STATUS_ID)).thenReturn(Optional.empty());
            assertThrows(ResourceNotFoundException.class, () -> orderStatusService.getOrderStatusById(STATUS_ID));
            verify(orderStatusRepository).findById(STATUS_ID);
        }
    }

    @Nested
    @DisplayName("createOrderStatus Tests")
    class CreateOrderStatusTests {
        @Test
        void createOrderStatus_ValidNewName_ReturnsCreatedStatus() {
            statusDetails.setId(null);
            when(orderStatusRepository.existsByStatusName(statusDetails.getStatusName())).thenReturn(false);
            when(orderStatusRepository.save(any(OrderStatus.class))).thenAnswer(inv -> {
                OrderStatus s = inv.getArgument(0);
                s.setId(STATUS_ID + 1);
                return s;
            });
            ArgumentCaptor<OrderStatus> captor = ArgumentCaptor.forClass(OrderStatus.class);

            OrderStatus result = orderStatusService.createOrderStatus(statusDetails);

            assertNotNull(result);
            assertNotNull(result.getId());
            assertEquals(statusDetails.getStatusName(), result.getStatusName());
            verify(orderStatusRepository).existsByStatusName(statusDetails.getStatusName());
            verify(orderStatusRepository).save(captor.capture());
        }

        @Test
        void createOrderStatus_NameExists_ThrowsException() {
            when(orderStatusRepository.existsByStatusName(statusDetails.getStatusName())).thenReturn(true);
            assertThrows(
                    StatusNameAlreadyExistsException.class,
                    () -> orderStatusService.createOrderStatus(statusDetails)
            );
            verify(orderStatusRepository).existsByStatusName(statusDetails.getStatusName());
            verify(orderStatusRepository, never()).save(any(OrderStatus.class));
        }
    }

    @Nested
    @DisplayName("updateOrderStatus Tests")
    class UpdateOrderStatusTests {
        @Test
        void updateOrderStatus_ValidNewName_ReturnsUpdatedStatus() {
            when(orderStatusRepository.findById(STATUS_ID)).thenReturn(Optional.of(status));
            when(orderStatusRepository.existsByStatusNameAndIdNot(statusDetails.getStatusName(), STATUS_ID)).thenReturn(
                    false);
            when(orderStatusRepository.save(any(OrderStatus.class))).thenAnswer(inv -> inv.getArgument(0));
            ArgumentCaptor<OrderStatus> captor = ArgumentCaptor.forClass(OrderStatus.class);

            OrderStatus result = orderStatusService.updateOrderStatus(STATUS_ID, statusDetails);

            assertNotNull(result);
            assertEquals(STATUS_ID, result.getId());
            assertEquals(statusDetails.getStatusName(), result.getStatusName());
            verify(orderStatusRepository).findById(STATUS_ID);
            verify(orderStatusRepository).existsByStatusNameAndIdNot(statusDetails.getStatusName(), STATUS_ID);
            verify(orderStatusRepository).save(captor.capture());
            assertEquals(STATUS_ID, captor.getValue().getId());
            assertEquals(statusDetails.getStatusName(), captor.getValue().getStatusName());
        }

        @Test
        void updateOrderStatus_NotFound_ThrowsException() {
            when(orderStatusRepository.findById(STATUS_ID)).thenReturn(Optional.empty());
            assertThrows(
                    ResourceNotFoundException.class,
                    () -> orderStatusService.updateOrderStatus(STATUS_ID, statusDetails)
            );
            verify(orderStatusRepository).findById(STATUS_ID);
            verify(orderStatusRepository, never()).save(any(OrderStatus.class));
        }

        @Test
        void updateOrderStatus_NewNameExists_ThrowsException() {
            when(orderStatusRepository.findById(STATUS_ID)).thenReturn(Optional.of(status));
            when(orderStatusRepository.existsByStatusNameAndIdNot(statusDetails.getStatusName(), STATUS_ID)).thenReturn(
                    true);
            assertThrows(
                    StatusNameAlreadyExistsException.class,
                    () -> orderStatusService.updateOrderStatus(STATUS_ID, statusDetails)
            );
            verify(orderStatusRepository).findById(STATUS_ID);
            verify(orderStatusRepository).existsByStatusNameAndIdNot(statusDetails.getStatusName(), STATUS_ID);
            verify(orderStatusRepository, never()).save(any(OrderStatus.class));
        }
    }

    @Nested
    @DisplayName("deleteOrderStatus Tests")
    class DeleteOrderStatusTests {
        @Test
        void deleteOrderStatus_NotUsed_DeletesStatus() {
            when(orderStatusRepository.existsById(STATUS_ID)).thenReturn(true);
            doNothing().when(orderStatusRepository).deleteById(STATUS_ID);

            orderStatusService.deleteOrderStatus(STATUS_ID);

            verify(orderStatusRepository).existsById(STATUS_ID);
        }


        @Test
        void deleteOrderStatus_NotFound_ThrowsException() {
            when(orderStatusRepository.existsById(STATUS_ID)).thenReturn(false);

            assertThrows(ResourceNotFoundException.class, () -> orderStatusService.deleteOrderStatus(STATUS_ID));

            verify(orderStatusRepository).existsById(STATUS_ID);
            verify(orderRepository, never()).existsById(anyLong());
            verify(orderStatusRepository, never()).deleteById(anyLong());
        }
    }
}
