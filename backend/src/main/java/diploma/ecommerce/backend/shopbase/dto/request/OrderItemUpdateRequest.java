package diploma.ecommerce.backend.shopbase.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(description = "Запрос на обновление количества позиции в заказе")
public class OrderItemUpdateRequest {

    @Schema(description = "Новое количество единиц продукта", example = "3", requiredMode =
            Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Quantity cannot be null")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;
}
