package diploma.ecommerce.backend.shopbase.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(description = "Запрос на создание нового клиента")
public class UserCreateRequest {

    @Schema(description = "Имя", example = "Алексей", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "First name cannot be blank")
    @Size(max = 100)
    private String firstName;

    @Schema(description = "Фамилия", example = "Иванов", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Last name cannot be blank")
    @Size(max = 100)
    private String lastName;

    @Schema(description = "Отчество", example = "Петрович", nullable = true)
    @Size(max = 100)
    private String patronymic;

    @Schema(description = "Email", example = "alex.ivanov@example.com",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Invalid email format")
    @Size(max = 255)
    private String email;

    @Schema(description = "Телефон", example = "+79112223344", nullable = true)
    @Size(max = 20)
    private String phone;

    @Schema(example = "true", defaultValue = "true")
    private Boolean active = true;
}
