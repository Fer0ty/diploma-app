package diploma.ecommerce.backend.shopbase.service;

import diploma.ecommerce.backend.shopbase.dto.request.RegisterTenantRequest;
import diploma.ecommerce.backend.shopbase.dto.response.RegisterTenantResponse;

public interface RegistrationService {

    RegisterTenantResponse registerTenant(RegisterTenantRequest request);

    boolean isSubdomainAvailable(String subdomain);

    boolean isEmailAvailable(String email);

    boolean isUsernameAvailable(String username);

    boolean isShopNameAvailable(String shopName);

    void validateRequest(RegisterTenantRequest request);
}
