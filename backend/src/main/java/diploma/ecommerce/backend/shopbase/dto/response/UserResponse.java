package diploma.ecommerce.backend.shopbase.dto.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(description = "Ответ с информацией о клиенте")
public class UserResponse {

    @Schema(description = "Уникальный идентификатор клиента", example = "12", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Schema(description = "Имя клиента", example = "Алексей")
    private String firstName;

    @Schema(description = "Фамилия клиента", example = "Иванов")
    private String lastName;

    @Schema(description = "Отчество клиента", example = "Петрович", nullable = true)
    private String patronymic;

    @Schema(description = "Email клиента", example = "alex.ivanov@example.com")
    private String email;

    @Schema(description = "Телефон клиента", example = "+79112223344", nullable = true)
    private String phone;

    @Schema(description = "Флаг активности клиента", example = "true")
    private boolean active;

    @Schema(description = "Дата и время регистрации клиента", example = "2023-08-15T12:00:00", accessMode =
            Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;
}
