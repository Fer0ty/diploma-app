package diploma.ecommerce.backend.shopbase.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Запрос на создание позиции в заказе")
public class OrderItemCreateRequest {

    @Schema(description = "ID продукта, добавляемого в заказ", example = "5", requiredMode =
            Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Product ID cannot be null")
    private Long productId;

    @Schema(description = "Количество единиц продукта", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Quantity cannot be null")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;
}
