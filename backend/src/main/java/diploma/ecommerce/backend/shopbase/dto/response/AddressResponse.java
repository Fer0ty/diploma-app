package diploma.ecommerce.backend.shopbase.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(description = "Ответ с информацией об адресе")
public class AddressResponse {

    @Schema(description = "ID адреса", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Schema(description = "Страна", example = "Россия")
    private String country;

    @Schema(description = "Город", example = "Санкт-Петербург")
    private String city;

    @Schema(description = "Улица", example = "Невский проспект")
    private String street;

    @Schema(description = "Номер дома", example = "10")
    private String houseNumber;

    @Schema(description = "Номер квартиры/офиса", example = "5", nullable = true)
    private String apartment;

    @Schema(description = "Почтовый индекс", example = "191186", nullable = true)
    private String postalCode;

    @Schema(description = "Дополнительный комментарий", example = "Код домофона 1234", nullable = true)
    private String comment;
}
