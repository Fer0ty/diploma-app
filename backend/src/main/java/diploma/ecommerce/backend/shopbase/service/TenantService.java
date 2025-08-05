package diploma.ecommerce.backend.shopbase.service;

import diploma.ecommerce.backend.shopbase.dto.request.TenantUpdateRequest;
import diploma.ecommerce.backend.shopbase.dto.response.TenantResponse;
import diploma.ecommerce.backend.shopbase.model.Tenant;
import diploma.ecommerce.backend.shopbase.model.Theme;

public interface TenantService {

    Tenant getTenantById(Long tenantId);

    Tenant updateTenant(Long tenantId, Tenant tenant);

    Tenant updateTenantFromRequest(Long tenantId, TenantUpdateRequest request);

    TenantResponse getTenantResponse(Long tenantId);

    Tenant updateTenantTheme(Long tenantId, Theme themeData);
}