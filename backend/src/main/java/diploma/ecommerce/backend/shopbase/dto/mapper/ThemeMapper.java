package diploma.ecommerce.backend.shopbase.dto.mapper;

import diploma.ecommerce.backend.shopbase.dto.request.ThemeUpdateRequest;
import diploma.ecommerce.backend.shopbase.dto.response.ThemeResponse;
import diploma.ecommerce.backend.shopbase.model.Theme;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ThemeMapper {

    ThemeResponse toThemeResponse(Theme theme);

    void updateThemeFromRequest(ThemeUpdateRequest request, @MappingTarget Theme theme);

    Theme createThemeFromRequest(ThemeUpdateRequest request);
}