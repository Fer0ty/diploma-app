package diploma.ecommerce.backend.shopbase.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(description = "Ответ с информацией о продукте")
public class ProductResponse {

    @Schema(description = "Уникальный идентификатор продукта", example = "42", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Schema(description = "Название продукта", example = "Умные часы Pro")
    private String name;

    @Schema(description = "Описание продукта", example = "Водонепроницаемые, GPS, мониторинг сна", nullable = true)
    private String description;

    @Schema(description = "Цена продукта", example = "199.99")
    private BigDecimal price;

    @Schema(description = "Количество на складе", example = "50")
    private Integer stockQuantity;

    @Schema(description = "Категория продукта", example = "Электроника", nullable = true)
    private String category;

    @Schema(description = "Флаг активности", example = "true")
    private boolean active;

    @Schema(description = "Дата и время создания продукта", example = "2023-10-26T14:00:00", accessMode =
            Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;

    @Schema(description = "Дата и время последнего обновления продукта", example = "2023-10-27T09:30:00", accessMode
            = Schema.AccessMode.READ_ONLY)
    private LocalDateTime updatedAt;

    @Schema(description = "Список фотографий продукта", accessMode = Schema.AccessMode.READ_ONLY)
    private List<ProductPhotoResponse> photos;
}
