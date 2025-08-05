package diploma.ecommerce.backend.shopbase.controller;

import java.net.URI;
import java.util.List;

import diploma.ecommerce.backend.shopbase.dto.mapper.OrderItemMapper;
import diploma.ecommerce.backend.shopbase.dto.mapper.OrderMapper;
import diploma.ecommerce.backend.shopbase.dto.request.OrderCreateRequest;
import diploma.ecommerce.backend.shopbase.dto.request.OrderUpdateRequest;
import diploma.ecommerce.backend.shopbase.dto.response.OrderItemResponse;
import diploma.ecommerce.backend.shopbase.dto.response.OrderResponse;
import diploma.ecommerce.backend.shopbase.multitenancy.TenantContext;
import diploma.ecommerce.backend.shopbase.service.OrderItemService;
import diploma.ecommerce.backend.shopbase.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Slf4j
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Order Management", description = "API для управления заказами (в контексте тенанта)")
public class OrderController {

    private final OrderService orderService;
    private final OrderMapper orderMapper;

    private final OrderItemService orderItemService;
    private final OrderItemMapper orderItemMapper;

    private Long getCurrentTenantIdOrFail() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            log.error("Tenant ID could not be resolved for the current request. Access denied or configuration issue.");
            throw new AccessDeniedException("Tenant context not established.");
        }
        log.trace("Using Tenant ID: {}", tenantId);
        return tenantId;
    }

    @Operation(summary = "Получить все заказы для текущего тенанта (с пагинацией)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список заказов получен"),
            @ApiResponse(responseCode = "401", description = "Не авторизован")
    })
    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<Page<OrderResponse>> getAllOrders(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        Long tenantId = getCurrentTenantIdOrFail();
        Page<OrderResponse> orderPage = orderService.getAllOrders(tenantId, pageable)
                .map(orderMapper::toOrderResponse);
        return ResponseEntity.ok(orderPage);
    }

    @Operation(summary = "Получить заказ по ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Заказ найден"),
            @ApiResponse(responseCode = "404", description = "Заказ не найден")
    })
    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<OrderResponse> getOrderById(
            @Parameter(description = "ID заказа") @PathVariable Long id
    ) {
        Long tenantId = getCurrentTenantIdOrFail();
        OrderResponse order = orderMapper.toOrderResponse(
                orderService.getOrderById(tenantId, id)
        );
        return ResponseEntity.ok(order);
    }

    @Operation(summary = "Создать новый заказ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Заказ успешно создан"),
            @ApiResponse(responseCode = "400", description = "Некорректный запрос (ошибка валидации, неверные ID и т" +
                    ".д.)"),
            @ApiResponse(responseCode = "409", description = "Конфликт (например, недостаточно товара на складе)")
    })
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @Parameter(description = "Данные для создания заказа") @Valid @RequestBody OrderCreateRequest request
    ) {
        Long tenantId = getCurrentTenantIdOrFail();
        OrderResponse createdOrder = orderMapper.toOrderResponse(
                orderService.createOrder(tenantId, orderMapper.toOrder(request))
        );

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdOrder.getId())
                .toUri();

        return ResponseEntity.created(location).body(createdOrder);
    }

    @Operation(summary = "Обновить статус заказа")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Статус заказа успешно обновлен"),
            @ApiResponse(responseCode = "400", description = "Некорректный запрос (неверный ID статуса)"),
            @ApiResponse(responseCode = "404", description = "Заказ или статус не найден")
    })
    @PutMapping("/{id}/status")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @Parameter(description = "ID заказа для обновления") @PathVariable Long id,
            @Parameter(description = "Данные для обновления статуса") @Valid @RequestBody OrderUpdateRequest request
    ) {
        Long tenantId = getCurrentTenantIdOrFail();
        OrderResponse updatedOrder = orderMapper.toOrderResponse(
                orderService.updateOrderStatus(tenantId, id, request.getStatusId(), request.getComment())
        );
        return ResponseEntity.ok(updatedOrder);
    }

    @Operation(summary = "Удалить заказ", deprecated = true)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Заказ успешно удален"),
            @ApiResponse(responseCode = "404", description = "Заказ не найден"),
            @ApiResponse(responseCode = "409", description = "Невозможно удалить заказ (например, из-за статуса)")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(
            @Parameter(description = "ID заказа для удаления") @PathVariable Long id
    ) {
        Long tenantId = getCurrentTenantIdOrFail();
        orderService.deleteOrder(tenantId, id);
        return ResponseEntity.noContent().build();
    }

    // Управление позициями в заказах

    @Operation(summary = "Получить все позиции для конкретного заказа")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список позиций получен"),
            @ApiResponse(responseCode = "404", description = "Заказ не найден")
    })
    @GetMapping("/{id}/items")
    public ResponseEntity<List<OrderItemResponse>> getOrderItemsForOrder(
            @Parameter(description = "ID заказа") @PathVariable("id") Long orderId
    ) {
        Long tenantId = getCurrentTenantIdOrFail();
        List<OrderItemResponse> items = orderItemService.getOrderItemsByOrderId(tenantId, orderId)
                .stream()
                .map(orderItemMapper::toOrderItemResponse)
                .toList();
        return ResponseEntity.ok(items);
    }

    @Operation(summary = "Отменить заказ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Заказ успешно отменен"),
            @ApiResponse(responseCode = "400", description = "Заказ не может быть отменен в текущем статусе"),
            @ApiResponse(responseCode = "404", description = "Заказ не найден")
    })
    @PostMapping("/{id}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(
            @Parameter(description = "ID заказа") @PathVariable Long id,
            @Parameter(description = "Причина отмены") @RequestParam(required = false) String reason
    ) {
        Long tenantId = getCurrentTenantIdOrFail();
        OrderResponse canceledOrder = orderMapper.toOrderResponse(
                orderService.cancelOrder(tenantId, id, reason)
        );
        return ResponseEntity.ok(canceledOrder);
    }

    @Operation(summary = "Обработать оплату заказа")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Оплата успешно обработана"),
            @ApiResponse(responseCode = "400", description = "Оплата не может быть обработана для заказа в текущем " +
                    "статусе"),
            @ApiResponse(responseCode = "404", description = "Заказ не найден")
    })
    @PostMapping("/{id}/payment")
    public ResponseEntity<OrderResponse> processPayment(
            @Parameter(description = "ID заказа") @PathVariable Long id,
            @Parameter(description = "Идентификатор транзакции") @RequestParam String paymentReference
    ) {
        Long tenantId = getCurrentTenantIdOrFail();
        OrderResponse paidOrder = orderMapper.toOrderResponse(
                orderService.processOrderPayment(tenantId, id, paymentReference)
        );
        return ResponseEntity.ok(paidOrder);
    }

}
