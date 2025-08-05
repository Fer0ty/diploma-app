package diploma.ecommerce.backend.shopbase.dto.request;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(description = "Запрос на создание нового заказа")
public class OrderCreateRequest {

    @Schema(description = "ID клиента, оформляющего заказ", example = "12", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Customer ID cannot be null")
    private Long customerId;

    @Schema(description = "ID адреса доставки", example = "3", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Address ID cannot be null")
    private Long addressId;

    @Schema(description = "Список позиций заказа", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "Order must contain at least one item")
    @Valid
    private List<OrderItemCreateRequest> orderItems;
}
