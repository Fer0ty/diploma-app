package diploma.ecommerce.backend.shopbase.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WbPriceUpdateRequest {
    private Long nmId;
    private Integer price;
}
