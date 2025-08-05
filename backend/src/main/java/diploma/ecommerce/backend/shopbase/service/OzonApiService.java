package diploma.ecommerce.backend.shopbase.service;

import diploma.ecommerce.backend.shopbase.dto.request.OzonPriceUpdateRequest;
import diploma.ecommerce.backend.shopbase.dto.request.OzonStockUpdateRequest;
import diploma.ecommerce.backend.shopbase.dto.response.OzonProductInfoResponse;

import java.util.List;

public interface OzonApiService {

    void updateStocks(String clientId, String apiKey, List<OzonStockUpdateRequest> stocks);

    void updatePrices(String clientId, String apiKey, List<OzonPriceUpdateRequest> prices);

    OzonProductInfoResponse getProductInfo(String clientId, String apiKey, Long productId);

    boolean testConnection(String clientId, String apiKey);
}