package diploma.ecommerce.backend.shopbase.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(description = "Запрос на обновление статуса заказа")
public class OrderUpdateRequest {

    @Schema(description = "ID нового статуса заказа", example = "3", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Status ID cannot be null")
    private Long statusId;

    @Schema(description = "Комментарий к изменению статуса (опционально)", example = "Заказ передан в службу доставки")
    private String comment;
}
