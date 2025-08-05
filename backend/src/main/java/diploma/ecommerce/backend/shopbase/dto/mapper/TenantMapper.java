package diploma.ecommerce.backend.shopbase.dto.mapper;

import diploma.ecommerce.backend.shopbase.dto.request.TenantUpdateRequest;
import diploma.ecommerce.backend.shopbase.model.Tenant;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TenantMapper {

    void updateTenantFromRequest(TenantUpdateRequest request, @MappingTarget Tenant tenant);
}