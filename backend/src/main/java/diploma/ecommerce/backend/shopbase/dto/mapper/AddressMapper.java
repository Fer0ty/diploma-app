package diploma.ecommerce.backend.shopbase.dto.mapper;

import diploma.ecommerce.backend.shopbase.dto.request.AddressCreateRequest;
import diploma.ecommerce.backend.shopbase.dto.request.AddressUpdateRequest;
import diploma.ecommerce.backend.shopbase.dto.response.AddressResponse;
import diploma.ecommerce.backend.shopbase.model.Address;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AddressMapper {

    AddressResponse toAddressResponse(Address address);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenant", ignore = true)
    Address toAddress(AddressCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenant", ignore = true)
    Address toAddress(AddressUpdateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenant", ignore = true)
    void updateAddressFromRequest(AddressUpdateRequest request, @MappingTarget Address address);
}

