package diploma.ecommerce.backend.shopbase.controller;

import java.util.List;
import java.util.stream.Collectors;

import diploma.ecommerce.backend.shopbase.dto.mapper.OrderItemMapper;
import diploma.ecommerce.backend.shopbase.dto.request.OrderItemCreateRequest;
import diploma.ecommerce.backend.shopbase.dto.request.OrderItemUpdateRequest;
import diploma.ecommerce.backend.shopbase.dto.response.OrderItemResponse;
import diploma.ecommerce.backend.shopbase.model.OrderItem;
import diploma.ecommerce.backend.shopbase.multitenancy.TenantContext;
import diploma.ecommerce.backend.shopbase.service.OrderItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/order-items")
@RequiredArgsConstructor
@Tag(name = "Order Item Management", description = "API для управления отдельными позициями заказа (ПОСЛЕ создания " +
        "заказа)")
public class OrderItemController {

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

    @Operation(summary = "Получить позицию заказа по её ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Позиция найдена"),
            @ApiResponse(responseCode = "404", description = "Позиция заказа не найдена")
    })
    @GetMapping("/{itemId}")
    @Transactional(readOnly = true)
    public ResponseEntity<OrderItemResponse> getOrderItemById(
            @Parameter(description = "ID позиции заказа") @PathVariable Long itemId
    ) {
        Long tenantId = getCurrentTenantIdOrFail();
        OrderItemResponse item = orderItemMapper.toOrderItemResponse(
                orderItemService.getOrderItem(tenantId, itemId)
        );
        return ResponseEntity.ok(item);
    }

    @Operation(summary = "Получить все позиции конкретного заказа")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Позиции заказа найдены"),
            @ApiResponse(responseCode = "404", description = "Заказ не найден")
    })
    @GetMapping("/order/{orderId}")
    @Transactional(readOnly = true)
    public ResponseEntity<List<OrderItemResponse>> getOrderItemsByOrderId(
            @Parameter(description = "ID заказа") @PathVariable Long orderId
    ) {
        Long tenantId = getCurrentTenantIdOrFail();
        List<OrderItemResponse> items = orderItemService.getOrderItemsByOrderId(tenantId, orderId)
                .stream()
                .map(orderItemMapper::toOrderItemResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(items);
    }

    @Operation(summary = "Добавить новую позицию в существующий заказ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Позиция успешно добавлена"),
            @ApiResponse(responseCode = "400", description = "Некорректный запрос"),
            @ApiResponse(responseCode = "404", description = "Заказ или продукт не найден"),
            @ApiResponse(responseCode = "409", description = "Конфликт (например, продукт уже в заказе)")
    })
    @PostMapping("/order/{orderId}")
    public ResponseEntity<OrderItemResponse> addOrderItem(
            @Parameter(description = "ID заказа") @PathVariable Long orderId,
            @Parameter(description = "Данные о добавляемой позиции") @Valid @RequestBody OrderItemCreateRequest request
    ) {
        Long tenantId = getCurrentTenantIdOrFail();

        OrderItem orderItem = orderItemMapper.toOrderItem(request);
        OrderItem createdItem = orderItemService.createOrderItem(tenantId, orderId, orderItem);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderItemMapper.toOrderItemResponse(createdItem));
    }

    @Operation(summary = "Обновить количество существующей позиции заказа")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Позиция успешно обновлена"),
            @ApiResponse(responseCode = "400", description = "Некорректный запрос (неверное количество)"),
            @ApiResponse(responseCode = "404", description = "Позиция заказа не найдена"),
            @ApiResponse(responseCode = "409", description = "Конфликт (недостаточно товара для увеличения количества)")
    })
    @PutMapping("/{itemId}")
    public ResponseEntity<OrderItemResponse> updateOrderItemQuantity(
            @Parameter(description = "ID позиции заказа для обновления") @PathVariable Long itemId,
            @Parameter(description = "Данные для обновления (количество)") @Valid @RequestBody OrderItemUpdateRequest request
    ) {
        Long tenantId = getCurrentTenantIdOrFail();
        OrderItemResponse updatedItem = orderItemMapper.toOrderItemResponse(
                orderItemService.updateOrderItem(tenantId, itemId, orderItemMapper.toOrderItem(request))
        );
        return ResponseEntity.ok(updatedItem);
    }

    @Operation(summary = "Удалить позицию из существующего заказа")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Позиция успешно удалена"),
            @ApiResponse(responseCode = "404", description = "Позиция заказа не найдена")
    })
    @DeleteMapping("/{itemId}")
    public ResponseEntity<Void> deleteOrderItem(
            @Parameter(description = "ID позиции заказа для удаления") @PathVariable Long itemId
    ) {
        Long tenantId = getCurrentTenantIdOrFail();
        orderItemService.deleteOrderItem(tenantId, itemId);
        return ResponseEntity.noContent().build();
    }
}
