package diploma.ecommerce.backend.shopbase.dto.mapper;

import diploma.ecommerce.backend.shopbase.dto.request.ProductPhotoCreateRequest;
import diploma.ecommerce.backend.shopbase.dto.request.ProductPhotoUpdateRequest;
import diploma.ecommerce.backend.shopbase.dto.response.ProductPhotoResponse;
import diploma.ecommerce.backend.shopbase.model.ProductPhoto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ProductPhotoMapper {
    ProductPhotoResponse toProductPhotoResponse(ProductPhoto productPhoto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenant", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "main", source = "main", defaultValue = "false")
    ProductPhoto toProductPhoto(ProductPhotoCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenant", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "filePath", ignore = true)
    void updateProductPhotoFromRequest(ProductPhotoUpdateRequest request, @MappingTarget ProductPhoto productPhoto);
}
