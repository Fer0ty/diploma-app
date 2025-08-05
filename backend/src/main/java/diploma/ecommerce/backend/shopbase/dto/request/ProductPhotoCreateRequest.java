package diploma.ecommerce.backend.shopbase.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(description = "Запрос на добавление фотографии к продукту")
public class ProductPhotoCreateRequest {

    @Schema(description = "Путь к файлу фотографии", example = "/images/products/watch_pro_1.jpg", requiredMode =
            Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "File path cannot be blank")
    @Size(max = 255)
    private String filePath;

    @Schema(description = "Порядок отображения (меньше -> раньше)", example = "0", defaultValue = "0")
    private Integer displayOrder = 0;

    @Schema(description = "Является ли фотография главной для продукта", example = "false", defaultValue = "false")
    private Boolean main = false;
}
