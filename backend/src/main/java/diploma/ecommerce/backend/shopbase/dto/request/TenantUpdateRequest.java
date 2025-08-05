package diploma.ecommerce.backend.shopbase.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Запрос на обновление информации о магазине")
public class TenantUpdateRequest {

    @Schema(description = "Название магазина", example = "Электроника", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Название магазина не может быть пустым")
    @Size(min = 2, max = 255, message = "Название магазина должно содержать от 2 до 255 символов")
    private String name;

    @Schema(description = "Контактный телефон", example = "+7 (999) 999-99-99")
    @Pattern(regexp = "^$|^\\+?[0-9\\s\\-\\(\\)]+$", message = "Неверный формат телефона")
    @Size(max = 50, message = "Телефон не должен превышать 50 символов")
    private String contactPhone;

    @Schema(description = "Контактный email", example = "shop@example.com")
    @Email(message = "Неверный формат email")
    @Size(max = 255, message = "Email не должен превышать 255 символов")
    private String contactEmail;

    @Schema(description = "API ключ Ozon")
    @Size(max = 500, message = "API ключ Ozon не должен превышать 500 символов")
    private String ozonApiKey;

    @Schema(description = "Client ID для Ozon API")
    @Size(max = 255, message = "Client ID не должен превышать 255 символов")
    private String ozonClientId;

    @Schema(description = "ID склада в Ozon")
    private Long ozonWarehouseId;

    @Schema(description = "Включить синхронизацию с Ozon")
    private Boolean ozonSyncEnabled;

    @Schema(description = "API ключ Wildberries")
    @Size(max = 500, message = "API ключ Wildberries не должен превышать 500 символов")
    private String wildberriesApiKey;

    @Schema(description = "Ключ идемпотентности ЮKassa")
    @Size(max = 500, message = "Ключ идемпотентности не должен превышать 500 символов")
    private String yookassaIdempotencyKey;

    @Schema(description = "Секретный ключ ЮKassa")
    @Size(max = 500, message = "Секретный ключ не должен превышать 500 символов")
    private String yookassaSecretKey;
}