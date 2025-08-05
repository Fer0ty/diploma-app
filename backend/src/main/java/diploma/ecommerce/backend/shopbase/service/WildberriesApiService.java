package diploma.ecommerce.backend.shopbase.service;

import diploma.ecommerce.backend.shopbase.dto.request.WbPriceUpdateRequest;
import diploma.ecommerce.backend.shopbase.dto.request.WbStockUpdateRequest;
import diploma.ecommerce.backend.shopbase.dto.response.WbProductInfoResponse;

import java.util.List;

public interface WildberriesApiService {

    void updateStocks(String apiKey, List<WbStockUpdateRequest> stocks);

    void updatePrices(String apiKey, List<WbPriceUpdateRequest> prices);

    WbProductInfoResponse getProductInfo(String apiKey, Long nmId);

    boolean testConnection(String apiKey);
}