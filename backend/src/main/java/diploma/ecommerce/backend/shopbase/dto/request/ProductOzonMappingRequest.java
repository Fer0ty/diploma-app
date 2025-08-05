package diploma.ecommerce.backend.shopbase.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(description = "Запрос на создание связи товара с Ozon")
public class ProductOzonMappingRequest {

    @Schema(description = "ID товара в Ozon", example = "123456", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Ozon product ID is required")
    private Long ozonProductId;

    @Schema(description = "SKU товара в Ozon", example = "78901", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Ozon SKU is required")
    private Long ozonSku;

    @Schema(description = "FBO SKU товара в Ozon", example = "111222")
    private Long ozonFboSku;

    @Schema(description = "FBS SKU товара в Ozon", example = "333444")
    private Long ozonFbsSku;

    @Schema(description = "ID склада", example = "1")
    private Long warehouseId;
}