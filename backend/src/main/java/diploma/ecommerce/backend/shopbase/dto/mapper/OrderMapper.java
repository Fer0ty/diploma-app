package diploma.ecommerce.backend.shopbase.dto.mapper;

import diploma.ecommerce.backend.shopbase.dto.request.OrderCreateRequest;
import diploma.ecommerce.backend.shopbase.dto.response.OrderResponse;
import diploma.ecommerce.backend.shopbase.model.Address;
import diploma.ecommerce.backend.shopbase.model.Order;
import diploma.ecommerce.backend.shopbase.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring", uses = {OrderItemMapper.class})
public interface OrderMapper {

    @Mapping(source = "customer.id", target = "customerId")
    @Mapping(source = "customer.firstName", target = "customerName")
    @Mapping(source = "address.id", target = "addressId")
    @Mapping(source = "status.id", target = "statusId")
    @Mapping(source = "status.statusName", target = "statusName")
    OrderResponse toOrderResponse(Order order);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenant", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "totalAmount", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(source = "customerId", target = "customer", qualifiedByName = "customerIdToUserShell")
    @Mapping(source = "addressId", target = "address", qualifiedByName = "addressIdToAddressShell")
    Order toOrder(OrderCreateRequest request);

    @Named("customerIdToUserShell")
    default User customerIdToUserShell(Long customerId) {
        if (customerId == null) {
            return null;
        }
        User user = new User();
        user.setId(customerId);
        return user;
    }

    @Named("addressIdToAddressShell")
    default Address addressIdToAddressShell(Long addressId) {
        if (addressId == null) {
            return null;
        }
        Address address = new Address();
        address.setId(addressId);
        return address;
    }
}
