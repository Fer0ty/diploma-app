package diploma.ecommerce.backend.shopbase.dto.mapper;

import diploma.ecommerce.backend.shopbase.dto.request.ProductOzonMappingRequest;
import diploma.ecommerce.backend.shopbase.dto.response.ProductOzonMappingResponse;
import diploma.ecommerce.backend.shopbase.model.ProductOzonMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductOzonMappingMapper {

    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.name", target = "productName")
    ProductOzonMappingResponse toResponse(ProductOzonMapping mapping);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenant", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "lastPriceSync", ignore = true)
    @Mapping(target = "lastStockSync", ignore = true)
    @Mapping(target = "syncStatus", ignore = true)
    @Mapping(target = "syncError", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    ProductOzonMapping toEntity(ProductOzonMappingRequest request);
}