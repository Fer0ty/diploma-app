package diploma.ecommerce.backend.shopbase.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class OzonProductInfoResponse {
    private Long id;
    private String name;
    private String offer_id;
    private String barcode;
    private Long category_id;
    private Long fbo_sku;
    private Long fbs_sku;
    private String created_at;
    private String updated_at;
}