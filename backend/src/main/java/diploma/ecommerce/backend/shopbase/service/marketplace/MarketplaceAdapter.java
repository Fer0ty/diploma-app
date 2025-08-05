package diploma.ecommerce.backend.shopbase.service.marketplace;

import java.util.List;

import diploma.ecommerce.backend.shopbase.model.Product;

public interface MarketplaceAdapter {

    /**
     * Обновить остатки товаров
     */
    void updateStocks(List<StockUpdate> stockUpdates);

    /**
     * Обновить цены товаров
     */
    void updatePrices(List<PriceUpdate> priceUpdates);

    /**
     * Получить информацию о товаре
     */
    MarketplaceProductInfo getProductInfo(String marketplaceProductId);

    /**
     * Проверить подключение к маркетплейсу
     */
    boolean testConnection();

    /**
     * Получить тип маркетплейса
     */
    MarketplaceType getType();

    // Общие DTO для всех маркетплейсов
    record StockUpdate(
            String marketplaceProductId,
            Integer stock,
            Long warehouseId
    ) {}

    record PriceUpdate(
            String marketplaceProductId,
            String price,
            String oldPrice
    ) {}

    record MarketplaceProductInfo(
            String id,
            String name,
            String sku,
            String barcode
    ) {}

    enum MarketplaceType {
        OZON, WILDBERRIES
    }
}