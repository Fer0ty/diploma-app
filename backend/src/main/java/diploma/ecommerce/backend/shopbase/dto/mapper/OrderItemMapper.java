package diploma.ecommerce.backend.shopbase.dto.mapper;

import diploma.ecommerce.backend.shopbase.dto.request.OrderItemCreateRequest;
import diploma.ecommerce.backend.shopbase.dto.request.OrderItemUpdateRequest;
import diploma.ecommerce.backend.shopbase.dto.response.OrderItemResponse;
import diploma.ecommerce.backend.shopbase.model.OrderItem;
import diploma.ecommerce.backend.shopbase.model.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface OrderItemMapper {

    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.name", target = "productName")
    OrderItemResponse toOrderItemResponse(OrderItem orderItem);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenant", ignore = true)
    @Mapping(target = "order", ignore = true)
    @Mapping(target = "unitPrice", ignore = true)
    @Mapping(target = "totalPrice", ignore = true)
    @Mapping(source = "productId", target = "product", qualifiedByName = "productIdToProductShell")
    OrderItem toOrderItem(OrderItemCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenant", ignore = true)
    @Mapping(target = "order", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "unitPrice", ignore = true)
    @Mapping(target = "totalPrice", ignore = true)
    void updateOrderItemFromRequest(OrderItemUpdateRequest request, @MappingTarget OrderItem orderItem);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenant", ignore = true)
    @Mapping(target = "order", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "unitPrice", ignore = true)
    @Mapping(target = "totalPrice", ignore = true)
    OrderItem toOrderItem(OrderItemUpdateRequest request);

    @Named("productIdToProductShell")
    default Product productIdToProductShell(Long productId) {
        if (productId == null) {
            return null;
        }
        Product product = new Product();
        product.setId(productId);
        return product;
    }
}
