package diploma.ecommerce.backend.shopbase.dto.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(description = "Ответ с информацией о фотографии продукта")
public class ProductPhotoResponse {

    @Schema(description = "Уникальный идентификатор фотографии", example = "201", accessMode =
            Schema.AccessMode.READ_ONLY)
    private Long id;

    @Schema(description = "Путь к файлу фотографии", example = "/images/products/watch_pro_1.jpg", accessMode =
            Schema.AccessMode.READ_ONLY)
    private String filePath;

    @Schema(description = "Порядок отображения", example = "0")
    private Integer displayOrder;

    @Schema(description = "Является ли фотография главной", example = "true")
    private boolean main;

    @Schema(description = "Дата и время добавления фотографии", example = "2023-10-26T14:05:00", accessMode =
            Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;
}
