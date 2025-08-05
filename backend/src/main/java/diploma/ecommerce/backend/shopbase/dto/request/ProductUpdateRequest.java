package diploma.ecommerce.backend.shopbase.dto.request;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(description = "Запрос на обновление продукта")
public class ProductUpdateRequest {
    @Schema(description = "Название продукта", example = "Умные часы Pro (New)")
    @Size(max = 255)
    private String name;

    @Schema(description = "Описание продукта", example = "Водонепроницаемые, GPS, мониторинг сна, NFC")
    private String description;

    @Schema(description = "Цена продукта", example = "209.99")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be positive")
    private BigDecimal price;

    @Schema(description = "Количество на складе", example = "45")
    @Min(value = 0, message = "Stock quantity cannot be negative")
    private Integer stockQuantity;

    @Schema(description = "Категория продукта", example = "Носимая электроника")
    @Size(max = 100)
    private String category;

    @Schema(description = "Флаг активности (доступен ли для продажи)", example = "true")
    private Boolean active;
}
