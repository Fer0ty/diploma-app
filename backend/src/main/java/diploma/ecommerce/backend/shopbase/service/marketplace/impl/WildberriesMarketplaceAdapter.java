package diploma.ecommerce.backend.shopbase.service.marketplace.impl;

import diploma.ecommerce.backend.shopbase.dto.request.WbPriceUpdateRequest;
import diploma.ecommerce.backend.shopbase.dto.request.WbStockUpdateRequest;
import diploma.ecommerce.backend.shopbase.dto.response.WbProductInfoResponse;
import diploma.ecommerce.backend.shopbase.service.WildberriesApiService;
import diploma.ecommerce.backend.shopbase.service.marketplace.MarketplaceAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class WildberriesMarketplaceAdapter implements MarketplaceAdapter {

    private final WildberriesApiService wildberriesApiService;
    private final String apiKey;

    public WildberriesMarketplaceAdapter(WildberriesApiService wildberriesApiService, String apiKey) {
        this.wildberriesApiService = wildberriesApiService;
        this.apiKey = apiKey;
    }

    @Override
    public void updateStocks(List<StockUpdate> stockUpdates) {
        List<WbStockUpdateRequest> wbRequests = stockUpdates.stream()
                .map(update -> WbStockUpdateRequest.builder()
                        .nmId(Long.parseLong(update.marketplaceProductId()))
                        .stock(update.stock())
                        .warehouseId(update.warehouseId())
                        .build())
                .collect(Collectors.toList());

        wildberriesApiService.updateStocks(apiKey, wbRequests);
    }

    @Override
    public void updatePrices(List<PriceUpdate> priceUpdates) {
        List<WbPriceUpdateRequest> wbRequests = priceUpdates.stream()
                .map(update -> WbPriceUpdateRequest.builder()
                        .nmId(Long.parseLong(update.marketplaceProductId()))
                        .price(Integer.parseInt(update.price()))
                        .build())
                .collect(Collectors.toList());

        wildberriesApiService.updatePrices(apiKey, wbRequests);
    }

    @Override
    public MarketplaceProductInfo getProductInfo(String marketplaceProductId) {
        WbProductInfoResponse info = wildberriesApiService.getProductInfo(
                apiKey, Long.parseLong(marketplaceProductId)
        );

        return new MarketplaceProductInfo(
                info.getNmId().toString(),
                info.getName(),
                info.getVendorCode(),
                info.getBarcode()
        );
    }

    @Override
    public boolean testConnection() {
        return wildberriesApiService.testConnection(apiKey);
    }

    @Override
    public MarketplaceType getType() {
        return MarketplaceType.WILDBERRIES;
    }
}