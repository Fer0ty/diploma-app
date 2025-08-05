package diploma.ecommerce.backend.shopbase.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Данные для регистрации нового тенанта (магазина) и его администратора")
public class RegisterTenantRequest {

    @NotBlank(message = "Tenant name is required")
    @Size(min = 2, max = 255, message = "Tenant name must be between 2 and 255 characters")
    @Schema(description = "Название тенанта (магазина)", example = "Digital Store",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String tenantName;

    @NotBlank(message = "Subdomain is required")
    @Size(min = 2, max = 63, message = "Subdomain must be between 2 and 63 characters")
    @Pattern(regexp = "^[a-z0-9](?:[a-z0-9\\-]{0,61}[a-z0-9])?$",
            message = "Subdomain can only contain lowercase letters, numbers, and hyphens, and cannot start or end " +
                    "with a hyphen")
    @Schema(description = "Поддомен тенанта (используется для доступа к магазину)",
            example = "digitalstore",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String subdomain;

    @NotBlank(message = "Admin username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$",
            message = "Username can only contain letters, numbers, and underscores")
    @Schema(description = "Логин пользователя", example = "admin",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 72, message = "Password must be between 8 and 72 characters")
    @Schema(description = "Пароль администратора (не менее 8 символов)", example = "securePassword123",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Schema(description = "Email администратора", example = "admin@digitalstore.com",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @Size(max = 100)
    @Schema(description = "Имя администратора", example = "John")
    private String firstName;

    @Size(max = 100)
    @Schema(description = "Фамилия администратора", example = "Doe")
    private String lastName;
}
