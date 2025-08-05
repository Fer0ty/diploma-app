package diploma.ecommerce.backend.shopbase.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Ответ с JWT токеном доступа")
public class AuthResponse {

    @Schema(description = "JWT токен доступа", example = "eyJhbGciOiJIUzUxMiJ9...")
    private String token;
}