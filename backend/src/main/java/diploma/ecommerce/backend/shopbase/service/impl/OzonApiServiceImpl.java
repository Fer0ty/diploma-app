package diploma.ecommerce.backend.shopbase.service.impl;

import diploma.ecommerce.backend.shopbase.dto.request.OzonPriceUpdateRequest;
import diploma.ecommerce.backend.shopbase.dto.request.OzonStockUpdateRequest;
import diploma.ecommerce.backend.shopbase.dto.response.OzonProductInfoResponse;
import diploma.ecommerce.backend.shopbase.service.OzonApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OzonApiServiceImpl implements OzonApiService {

    private final RestTemplate restTemplate;

    @Value("${ozon.api.url:https://api-seller.ozon.ru}")
    private String ozonApiUrl;

    @Override
    public void updateStocks(String clientId, String apiKey, List<OzonStockUpdateRequest> stocks) {
        String url = ozonApiUrl + "/v2/products/stocks";

        HttpHeaders headers = createHeaders(clientId, apiKey);

        Map<String, Object> body = new HashMap<>();
        body.put("stocks", stocks);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            log.info("Stocks updated successfully: {}", response.getBody());
        } catch (Exception e) {
            log.error("Error updating stocks: ", e);
            throw new RuntimeException("Failed to update Ozon stocks", e);
        }
    }

    @Override
    public void updatePrices(String clientId, String apiKey, List<OzonPriceUpdateRequest> prices) {
        String url = ozonApiUrl + "/v1/product/import/prices";

        HttpHeaders headers = createHeaders(clientId, apiKey);

        Map<String, Object> body = new HashMap<>();
        body.put("prices", prices);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            log.info("Prices updated successfully: {}", response.getBody());
        } catch (Exception e) {
            log.error("Error updating prices: ", e);
            throw new RuntimeException("Failed to update Ozon prices", e);
        }
    }

    @Override
    public OzonProductInfoResponse getProductInfo(String clientId, String apiKey, Long productId) {
        String url = ozonApiUrl + "/v2/product/info";

        HttpHeaders headers = createHeaders(clientId, apiKey);

        Map<String, Object> body = new HashMap<>();
        body.put("product_id", productId);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<OzonProductInfoResponse> response = restTemplate.postForEntity(
                    url, request, OzonProductInfoResponse.class
            );
            return response.getBody();
        } catch (Exception e) {
            log.error("Error getting product info: ", e);
            throw new RuntimeException("Failed to get Ozon product info", e);
        }
    }

    @Override
    public boolean testConnection(String clientId, String apiKey) {
        try {
            String url = ozonApiUrl + "/v1/product/list";

            HttpHeaders headers = createHeaders(clientId, apiKey);

            Map<String, Object> body = new HashMap<>();
            body.put("limit", 1);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.error("Ozon connection test failed: ", e);
            return false;
        }
    }

    private HttpHeaders createHeaders(String clientId, String apiKey) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Client-Id", clientId);
        headers.set("Api-Key", apiKey);
        return headers;
    }
}