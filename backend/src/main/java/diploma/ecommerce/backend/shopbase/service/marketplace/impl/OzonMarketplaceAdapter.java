package diploma.ecommerce.backend.shopbase.service.marketplace.impl;

import diploma.ecommerce.backend.shopbase.dto.request.OzonPriceUpdateRequest;
import diploma.ecommerce.backend.shopbase.dto.request.OzonStockUpdateRequest;
import diploma.ecommerce.backend.shopbase.dto.response.OzonProductInfoResponse;
import diploma.ecommerce.backend.shopbase.service.OzonApiService;
import diploma.ecommerce.backend.shopbase.service.marketplace.MarketplaceAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class OzonMarketplaceAdapter implements MarketplaceAdapter {

    private final OzonApiService ozonApiService;
    private final String clientId;
    private final String apiKey;

    public OzonMarketplaceAdapter(OzonApiService ozonApiService, String clientId, String apiKey) {
        this.ozonApiService = ozonApiService;
        this.clientId = clientId;
        this.apiKey = apiKey;
    }


    @Override
    public void updateStocks(List<StockUpdate> stockUpdates) {
        List<OzonStockUpdateRequest> ozonRequests = stockUpdates.stream()
                .map(update -> OzonStockUpdateRequest.builder()
                        .product_id(Long.parseLong(update.marketplaceProductId()))
                        .stock(update.stock().longValue())
                        .warehouse_id(update.warehouseId())
                        .build())
                .collect(Collectors.toList());

        ozonApiService.updateStocks(clientId, apiKey, ozonRequests);
    }

    @Override
    public void updatePrices(List<PriceUpdate> priceUpdates) {
        List<OzonPriceUpdateRequest> ozonRequests = priceUpdates.stream()
                .map(update -> OzonPriceUpdateRequest.builder()
                        .product_id(Long.parseLong(update.marketplaceProductId()))
                        .price(update.price())
                        .old_price(update.oldPrice())
                        .premium_price(update.price())
                        .build())
                .collect(Collectors.toList());

        ozonApiService.updatePrices(clientId, apiKey, ozonRequests);
    }

    @Override
    public MarketplaceProductInfo getProductInfo(String marketplaceProductId) {
        OzonProductInfoResponse info = ozonApiService.getProductInfo(
                clientId, apiKey, Long.parseLong(marketplaceProductId)
        );

        return new MarketplaceProductInfo(
                info.getId().toString(),
                info.getName(),
                info.getOffer_id(),
                info.getBarcode()
        );
    }

    @Override
    public boolean testConnection() {
        return ozonApiService.testConnection(clientId, apiKey);
    }

    @Override
    public MarketplaceType getType() {
        return MarketplaceType.OZON;
    }
}