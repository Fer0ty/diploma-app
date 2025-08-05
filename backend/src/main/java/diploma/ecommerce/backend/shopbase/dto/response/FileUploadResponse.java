package diploma.ecommerce.backend.shopbase.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Ответ на запрос загрузки файла")
public class FileUploadResponse {

    @Schema(description = "Успешна ли загрузка", example = "true")
    private boolean success;

    @Schema(description = "URL загруженного файла", example = "https://example.com/uploads/tenant_1/logos/1234-5678" +
            ".jpg")
    private String fileUrl;

    @Schema(description = "Сообщение", example = "File uploaded successfully")
    private String message;
}
