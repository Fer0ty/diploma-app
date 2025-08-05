package diploma.ecommerce.backend.shopbase.dto.response;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(description = "Ответ с информацией о позиции заказа")
public class OrderItemResponse {

    @Schema(description = "ID позиции заказа", example = "101", accessMode =
            Schema.AccessMode.READ_ONLY)
    private Long id;

    @Schema(description = "ID связанного продукта", example = "5", accessMode = Schema.AccessMode.READ_ONLY)
    private Long productId;

    @Schema(description = "Название связанного продукта", example = "Смартфон Model X", accessMode =
            Schema.AccessMode.READ_ONLY)
    private String productName;

    @Schema(description = "Количество", example = "2")
    private Integer quantity;

    @Schema(description = "Цена за единицу на момент покупки", example = "999.99", accessMode =
            Schema.AccessMode.READ_ONLY)
    private BigDecimal unitPrice;

    @Schema(description = "Общая стоимость позиции (quantity * unitPrice)", example = "1999.98", accessMode =
            Schema.AccessMode.READ_ONLY)
    private BigDecimal totalPrice;
}
