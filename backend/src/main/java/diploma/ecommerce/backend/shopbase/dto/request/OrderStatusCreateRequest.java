package diploma.ecommerce.backend.shopbase.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(description = "Запрос на создание нового статуса заказа (глобально)")
public class OrderStatusCreateRequest {

    @Schema(description = "Название нового статуса", example = "Awaiting Shipment", requiredMode =
            Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Status name cannot be blank")
    @Size(max = 50, message = "Status name must be less than 50 characters")
    private String statusName;
}
