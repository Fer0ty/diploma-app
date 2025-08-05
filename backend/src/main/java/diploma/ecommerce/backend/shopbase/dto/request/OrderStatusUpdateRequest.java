package diploma.ecommerce.backend.shopbase.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(description = "Запрос на обновление названия статуса заказа (глобально)")
public class OrderStatusUpdateRequest {

    @Schema(description = "Новое название статуса", example = "Shipped Out", requiredMode =
            Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Status name cannot be blank")
    @Size(max = 50, message = "Status name must be less than 50 characters")
    private String statusName;
}
