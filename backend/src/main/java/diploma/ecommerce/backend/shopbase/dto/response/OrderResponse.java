package diploma.ecommerce.backend.shopbase.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(description = "Ответ с полной информацией о заказе")
public class OrderResponse {

    @Schema(description = "ID заказа", example = "55", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Schema(description = "ID клиента", example = "12", accessMode = Schema.AccessMode.READ_ONLY)
    private Long customerId;

    @Schema(description = "Имя клиента", example = "Иван", accessMode = Schema.AccessMode.READ_ONLY)
    private String customerName;

    @Schema(description = "ID адреса доставки", example = "3", accessMode = Schema.AccessMode.READ_ONLY)
    private Long addressId;

    @Schema(description = "ID статуса заказа", example = "3", accessMode = Schema.AccessMode.READ_ONLY)
    private Long statusId;

    @Schema(description = "Название статуса заказа", example = "Processing", accessMode = Schema.AccessMode.READ_ONLY)
    private String statusName;

    @Schema(description = "Общая сумма заказа", example = "2149.97", accessMode = Schema.AccessMode.READ_ONLY)
    private BigDecimal totalAmount;

    @Schema(description = "Комментарий к заказу", example = "Пожалуйста, позвоните за час до доставки", nullable = true)
    private String comment;

    @Schema(description = "Дата и время создания заказа", example = "2023-10-27T10:15:30", accessMode =
            Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;

    @Schema(description = "Дата и время последнего обновления заказа", example = "2023-10-27T11:00:00", accessMode =
            Schema.AccessMode.READ_ONLY)
    private LocalDateTime updatedAt;

    @Schema(description = "Список позиций в заказе", accessMode = Schema.AccessMode.READ_ONLY)
    private List<OrderItemResponse> orderItems;
}
