package diploma.ecommerce.backend.shopbase.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OzonStockUpdateRequest {
    private Long product_id;
    private Long stock;
    private Long warehouse_id;
}