package diploma.ecommerce.backend.shopbase.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(description = "Запрос на обновление данных клиента")
public class UserUpdateRequest {

    @Schema(description = "Имя клиента", example = "Алексей")
    @Size(max = 100)
    private String firstName;

    @Schema(description = "Фамилия клиента", example = "Иванов")
    @Size(max = 100)
    private String lastName;

    @Schema(description = "Отчество клиента", example = "Петрович", nullable = true)
    @Size(max = 100)
    private String patronymic;

    @Schema(description = "Email клиента", example = "alexey.ivanov@example.com")
    @Email(message = "Invalid email format")
    @Size(max = 255)
    private String email;

    @Schema(description = "Телефон клиента", example = "+79112223355", nullable = true)
    @Size(max = 20)
    private String phone;

    @Schema(description = "Флаг активности клиента", example = "true")
    private Boolean active;
}
