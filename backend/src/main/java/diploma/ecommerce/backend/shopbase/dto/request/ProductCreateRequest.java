package diploma.ecommerce.backend.shopbase.dto.request;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(description = "Запрос на создание нового продукта")
public class ProductCreateRequest {

    @Schema(description = "Название продукта", example = "Умные часы Pro", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Product name cannot be blank")
    @Size(max = 255)
    private String name;

    @Schema(description = "Описание продукта", example = "Водонепроницаемые, GPS, мониторинг сна")
    private String description;

    @Schema(description = "Цена продукта", example = "199.99", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Price cannot be null")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be positive")
    private BigDecimal price;

    @Schema(description = "Количество на складе", example = "50", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Stock quantity cannot be null")
    @Min(value = 0, message = "Stock quantity cannot be negative")
    private Integer stockQuantity;

    @Schema(description = "Категория продукта", example = "Электроника")
    @Size(max = 100)
    private String category;

    @Schema(description = "Флаг активности (доступен ли для продажи)", example = "true", defaultValue = "true")
    private Boolean active = true;
}
