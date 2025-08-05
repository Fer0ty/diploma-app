package diploma.ecommerce.backend.shopbase.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(description = "Запрос на обновление атрибутов фотографии продукта (порядок, главный флаг)")
public class ProductPhotoUpdateRequest {

    @Schema(description = "Новый порядок отображения", example = "1")
    private Integer displayOrder;

    @Schema(description = "Новое значение флага 'главная фотография'", example = "true")
    private Boolean main;
}
