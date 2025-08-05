package diploma.ecommerce.backend.shopbase.dto.response;

import diploma.ecommerce.backend.shopbase.model.ProductOzonMapping.SyncStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Schema(description = "Ответ с информацией о связи товара с Ozon")
public class ProductOzonMappingResponse {

    @Schema(description = "ID маппинга", example = "1")
    private Long id;

    @Schema(description = "ID товара в системе", example = "42")
    private Long productId;

    @Schema(description = "Название товара", example = "Умные часы Pro")
    private String productName;

    @Schema(description = "ID товара в Ozon", example = "123456")
    private Long ozonProductId;

    @Schema(description = "SKU товара в Ozon", example = "78901")
    private Long ozonSku;

    @Schema(description = "FBO SKU товара в Ozon", example = "111222")
    private Long ozonFboSku;

    @Schema(description = "FBS SKU товара в Ozon", example = "333444")
    private Long ozonFbsSku;

    @Schema(description = "ID склада", example = "1")
    private Long warehouseId;

    @Schema(description = "Время последней синхронизации цен")
    private LocalDateTime lastPriceSync;

    @Schema(description = "Время последней синхронизации остатков")
    private LocalDateTime lastStockSync;

    @Schema(description = "Статус синхронизации", example = "SYNCED")
    private SyncStatus syncStatus;

    @Schema(description = "Ошибка синхронизации")
    private String syncError;

    @Schema(description = "Активен ли маппинг", example = "true")
    private Boolean active;

    @Schema(description = "Дата создания")
    private LocalDateTime createdAt;

    @Schema(description = "Дата обновления")
    private LocalDateTime updatedAt;
}