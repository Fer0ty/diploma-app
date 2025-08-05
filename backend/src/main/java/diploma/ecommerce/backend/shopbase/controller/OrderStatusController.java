package diploma.ecommerce.backend.shopbase.controller;

import java.net.URI;
import java.util.List;

import diploma.ecommerce.backend.shopbase.dto.mapper.OrderStatusMapper;
import diploma.ecommerce.backend.shopbase.dto.request.OrderStatusCreateRequest;
import diploma.ecommerce.backend.shopbase.dto.request.OrderStatusUpdateRequest;
import diploma.ecommerce.backend.shopbase.dto.response.OrderStatusResponse;
import diploma.ecommerce.backend.shopbase.service.OrderStatusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/order-statuses")
@RequiredArgsConstructor
@Tag(name = "Order Status Management (Global)", description = "API для управления глобальными статусами заказов")
public class OrderStatusController {

    private final OrderStatusService orderStatusService;
    private final OrderStatusMapper orderStatusMapper;

    @Operation(summary = "Получить все глобальные статусы заказов")
    @ApiResponse(responseCode = "200", description = "Список статусов получен")
    @GetMapping
    public ResponseEntity<List<OrderStatusResponse>> getAllOrderStatuses() {
        List<OrderStatusResponse> statuses = orderStatusService.getAllOrderStatuses()
                .stream()
                .map(orderStatusMapper::toOrderStatusResponse)
                .toList();
        return ResponseEntity.ok(statuses);
    }

    @Operation(summary = "Получить статус заказа по ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Статус найден"),
            @ApiResponse(responseCode = "404", description = "Статус не найден")
    })
    @GetMapping("/{id}")
    public ResponseEntity<OrderStatusResponse> getOrderStatusById(
            @Parameter(description = "ID статуса") @PathVariable Long id
    ) {
        OrderStatusResponse status = orderStatusMapper.toOrderStatusResponse(
                orderStatusService.getOrderStatusById(id)
        );
        return ResponseEntity.ok(status);
    }

    @Operation(summary = "Создать новый глобальный статус заказа")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Статус успешно создан"),
            @ApiResponse(responseCode = "400", description = "Некорректный запрос (например, пустое имя)"),
            @ApiResponse(responseCode = "409", description = "Статус с таким именем уже существует")
    })
    @PostMapping
    public ResponseEntity<OrderStatusResponse> createOrderStatus(
            @Parameter(description = "Данные для создания статуса") @Valid @RequestBody OrderStatusCreateRequest request
    ) {
        OrderStatusResponse createdStatus = orderStatusMapper.toOrderStatusResponse(
                orderStatusService.createOrderStatus(orderStatusMapper.toOrderStatus(request))
        );

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdStatus.getId())
                .toUri();

        return ResponseEntity.created(location).body(createdStatus);
    }

    @Operation(summary = "Обновить название глобального статуса заказа")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Статус успешно обновлен"),
            @ApiResponse(responseCode = "400", description = "Некорректный запрос"),
            @ApiResponse(responseCode = "404", description = "Статус не найден"),
            @ApiResponse(responseCode = "409", description = "Статус с таким новым именем уже существует")
    })
    @PutMapping("/{id}")
    public ResponseEntity<OrderStatusResponse> updateOrderStatus(
            @Parameter(description = "ID статуса для обновления") @PathVariable Long id,
            @Parameter(description = "Данные для обновления") @Valid @RequestBody OrderStatusUpdateRequest request
    ) {
        OrderStatusResponse updatedStatus = orderStatusMapper.toOrderStatusResponse(
                orderStatusService.updateOrderStatus(id, orderStatusMapper.toOrderStatus(request))
        );
        return ResponseEntity.ok(updatedStatus);
    }

    @Operation(summary = "Удалить глобальный статус заказа (ОСТОРОЖНО!)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Статус успешно удален"),
            @ApiResponse(responseCode = "404", description = "Статус не найден"),
            @ApiResponse(responseCode = "409", description = "Невозможно удалить статус (используется в заказах)")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrderStatus(
            @Parameter(description = "ID статуса для удаления") @PathVariable Long id
    ) {
        orderStatusService.deleteOrderStatus(id);
        return ResponseEntity.noContent().build();
    }
}
