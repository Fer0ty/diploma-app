package diploma.ecommerce.backend.shopbase.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(description = "Ответ с информацией о статусе заказа")
public class OrderStatusResponse {

    @Schema(description = "ID статуса", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Schema(description = "Название статуса", example = "Created", accessMode = Schema.AccessMode.READ_ONLY)
    private String statusName;
}
