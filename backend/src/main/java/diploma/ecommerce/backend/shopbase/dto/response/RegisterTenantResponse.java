package diploma.ecommerce.backend.shopbase.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Ответ с информацией о созданном теннанте и администраторе")
public class RegisterTenantResponse {

    @Schema(description = "Уникальный идентификатор тенанта", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long tenantId;

    @Schema(description = "Название тенанта (магазина)", example = "Digital Store")
    private String tenantName;

    @Schema(description = "Поддомен тенанта", example = "digitalstore")
    private String subdomain;

    @Schema(description = "JWT токен для автоматической авторизации", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9" +
            "...", accessMode = Schema.AccessMode.READ_ONLY)
    private String accessToken;

    @Schema(description = "URL для доступа к новому тенанту", example = "https://digitalstore.diploma.ru",
            accessMode = Schema.AccessMode.READ_ONLY)
    private String loginUrl;
}
