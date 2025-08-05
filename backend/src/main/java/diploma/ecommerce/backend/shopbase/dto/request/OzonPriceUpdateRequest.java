package diploma.ecommerce.backend.shopbase.dto.request;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OzonPriceUpdateRequest {
    private Long product_id;
    private String price;
    private String old_price;
    private String premium_price;
}
