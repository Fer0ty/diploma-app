package diploma.ecommerce.backend.shopbase.dto.mapper;

import diploma.ecommerce.backend.shopbase.dto.request.OrderStatusCreateRequest;
import diploma.ecommerce.backend.shopbase.dto.request.OrderStatusUpdateRequest;
import diploma.ecommerce.backend.shopbase.dto.response.OrderStatusResponse;
import diploma.ecommerce.backend.shopbase.model.OrderStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface OrderStatusMapper {

    OrderStatusResponse toOrderStatusResponse(OrderStatus orderStatus);

    @Mapping(target = "id", ignore = true)
    OrderStatus toOrderStatus(OrderStatusCreateRequest request);

    @Mapping(target = "id", ignore = true)
    OrderStatus toOrderStatus(OrderStatusUpdateRequest request);

    @Mapping(target = "id", ignore = true)
    void updateOrderStatusFromRequest(OrderStatusUpdateRequest request, @MappingTarget OrderStatus orderStatus);
}
