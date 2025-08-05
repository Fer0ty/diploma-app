package diploma.ecommerce.backend.shopbase.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class WbProductInfoResponse {
    private Long nmId;
    private Long imtId;
    private String name;
    private String vendorCode;
    private String barcode;
}
