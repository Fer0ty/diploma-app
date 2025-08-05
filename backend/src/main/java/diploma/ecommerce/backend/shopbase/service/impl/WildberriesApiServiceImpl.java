package diploma.ecommerce.backend.shopbase.service.impl;

import diploma.ecommerce.backend.shopbase.dto.request.WbPriceUpdateRequest;
import diploma.ecommerce.backend.shopbase.dto.request.WbStockUpdateRequest;
import diploma.ecommerce.backend.shopbase.dto.response.WbProductInfoResponse;
import diploma.ecommerce.backend.shopbase.service.WildberriesApiService;
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
public class WildberriesApiServiceImpl implements WildberriesApiService {

    private final RestTemplate restTemplate;

    @Value("${wildberries.api.url:https://common-api.wildberries.ru}")
    private String wbApiUrl;

    @Override
    public void updateStocks(String apiKey, List<WbStockUpdateRequest> stocks) {
        String url = wbApiUrl + "/api/v3/stocks/{warehouseId}";

        // Группируем по складам
        Map<Long, List<WbStockUpdateRequest>> stocksByWarehouse = stocks.stream()
                .collect(Collectors.groupingBy(WbStockUpdateRequest::getWarehouseId));

        for (Map.Entry<Long, List<WbStockUpdateRequest>> entry : stocksByWarehouse.entrySet()) {
            Long warehouseId = entry.getKey();
            List<WbStockUpdateRequest> warehouseStocks = entry.getValue();

            HttpHeaders headers = createHeaders(apiKey);

            Map<String, Object> body = new HashMap<>();
            body.put("stocks", warehouseStocks.stream()
                    .map(stock -> Map.of(
                            "sku", stock.getBarcode(),
                            "amount", stock.getStock()
                    ))
                    .collect(Collectors.toList())
            );

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            try {
                String finalUrl = url.replace("{warehouseId}", warehouseId.toString());
                ResponseEntity<Map> response = restTemplate.putForEntity(finalUrl, request, Map.class);
                log.info("Stocks updated successfully for warehouse {}: {}", warehouseId, response.getBody());
            } catch (Exception e) {
                log.error("Error updating stocks for warehouse {}: ", warehouseId, e);
                throw new RuntimeException("Failed to update Wildberries stocks", e);
            }
        }
    }

    @Override
    public void updatePrices(String apiKey, List<WbPriceUpdateRequest> prices) {
        String url = wbApiUrl + "/public/api/v1/prices";

        HttpHeaders headers = createHeaders(apiKey);

        List<Map<String, Object>> priceData = prices.stream()
                .map(price -> Map.<String, Object>of(
                        "nmId", price.getNmId(),
                        "price", price.getPrice()
                ))
                .collect(Collectors.toList());

        HttpEntity<List<Map<String, Object>>> request = new HttpEntity<>(priceData, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            log.info("Prices updated successfully: {}", response.getBody());
        } catch (Exception e) {
            log.error("Error updating prices: ", e);
            throw new RuntimeException("Failed to update Wildberries prices", e);
        }
    }

    @Override
    public WbProductInfoResponse getProductInfo(String apiKey, Long nmId) {
        String url = wbApiUrl + "/content/v2/get/cards/list";

        HttpHeaders headers = createHeaders(apiKey);

        Map<String, Object> body = new HashMap<>();
        body.put("settings", Map.of(
                "filter", Map.of("nmIDs", List.of(nmId)),
                "limit", 1
        ));

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            // Парсим ответ и возвращаем первый элемент
            List<Map<String, Object>> cards = (List<Map<String, Object>>) response.getBody().get("cards");
            if (cards != null && !cards.isEmpty()) {
                Map<String, Object> card = cards.get(0);
                WbProductInfoResponse info = new WbProductInfoResponse();
                info.setNmId(((Number) card.get("nmID")).longValue());
                info.setName((String) card.get("title"));
                info.setVendorCode((String) card.get("vendorCode"));
                info.setBarcode((String) card.get("barcode"));
                return info;
            }
            throw new RuntimeException("Product not found");
        } catch (Exception e) {
            log.error("Error getting product info: ", e);
            throw new RuntimeException("Failed to get Wildberries product info", e);
        }
    }

    @Override
    public boolean testConnection(String apiKey) {
        try {
            String url = wbApiUrl + "/ping";

            HttpHeaders headers = createHeaders(apiKey);
            HttpEntity<Void> request = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.error("Wildberries connection test failed: ", e);
            return false;
        }
    }

    private HttpHeaders createHeaders(String apiKey) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", apiKey);
        return headers;
    }
}