package diploma.ecommerce.backend.shopbase.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(description = "Запрос на обновление адреса")
public class AddressUpdateRequest {
    @Schema(description = "Страна", example = "Россия")
    @Size(max = 100, message = "Country must be less than 100 characters")
    private String country;

    @Schema(description = "Город", example = "Санкт-Петербург")
    @Size(max = 100, message = "City must be less than 100 characters")
    private String city;

    @Schema(description = "Улица", example = "Невский проспект")
    @Size(max = 255, message = "Street must be less than 255 characters")
    private String street;

    @Schema(description = "Номер дома", example = "10")
    @Size(max = 20, message = "House number must be less than 20 characters")
    private String houseNumber;

    @Schema(description = "Номер квартиры/офиса", example = "5")
    @Size(max = 20, message = "Apartment must be less than 20 characters")
    private String apartment;

    @Schema(description = "Почтовый индекс", example = "191186")
    @Size(max = 20, message = "Postal code must be less than 20 characters")
    private String postalCode;

    @Schema(description = "Дополнительный комментарий", example = "Код домофона 1234, вход со двора")
    private String comment;
}
