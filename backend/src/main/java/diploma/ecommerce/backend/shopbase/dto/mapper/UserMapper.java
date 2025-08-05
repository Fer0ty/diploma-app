package diploma.ecommerce.backend.shopbase.dto.mapper;

import diploma.ecommerce.backend.shopbase.dto.request.UserCreateRequest;
import diploma.ecommerce.backend.shopbase.dto.request.UserUpdateRequest;
import diploma.ecommerce.backend.shopbase.dto.response.UserResponse;
import diploma.ecommerce.backend.shopbase.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserMapper {

    UserResponse toUserResponse(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenant", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    User toUser(UserCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenant", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    User toUser(UserUpdateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenant", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateUserFromRequest(UserUpdateRequest request, @MappingTarget User user);
}
