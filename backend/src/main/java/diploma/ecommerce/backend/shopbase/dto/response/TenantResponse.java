package diploma.ecommerce.backend.shopbase.dto.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Информация о магазине")
public class TenantResponse {

    @Schema(description = "Идентификатор магазина", example = "1")
    private Long id;

    @Schema(description = "Название магазина", example = "Электроника")
    private String name;

    @Schema(description = "Поддомен магазина", example = "electronics")
    private String subdomain;

    @Schema(description = "Контактный телефон", example = "+7 (999) 999-99-99")
    private String contactPhone;

    @Schema(description = "Контактный email", example = "shop@example.com")
    private String contactEmail;

    @Schema(description = "API ключ Ozon (не зашифрованный)")
    private String ozonApiKey;

    @Schema(description = "API ключ Wildberries (не зашифрованный)")
    private String wildberriesApiKey;

    @Schema(description = "Ключ идемпотентности ЮKassa (не зашифрованный)")
    private String yookassaIdempotencyKey;

    @Schema(description = "Секретный ключ ЮKassa (не зашифрованный)")
    private String yookassaSecretKey;

    @Schema(description = "Дата создания магазина")
    private LocalDateTime createdAt;

    @Schema(description = "Дата последнего обновления магазина")
    private LocalDateTime updatedAt;

    @Schema(description = "Активен ли магазин", example = "true")
    private boolean active;
}