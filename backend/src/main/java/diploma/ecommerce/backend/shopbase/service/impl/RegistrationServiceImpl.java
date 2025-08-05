package diploma.ecommerce.backend.shopbase.service.impl;

import diploma.ecommerce.backend.shopbase.dto.request.RegisterTenantRequest;
import diploma.ecommerce.backend.shopbase.dto.response.RegisterTenantResponse;
import diploma.ecommerce.backend.shopbase.exception.BadRequestException;
import diploma.ecommerce.backend.shopbase.model.Tenant;
import diploma.ecommerce.backend.shopbase.model.TenantUser;
import diploma.ecommerce.backend.shopbase.repository.TenantRepository;
import diploma.ecommerce.backend.shopbase.repository.TenantUserRepository;
import diploma.ecommerce.backend.shopbase.security.JwtUtil;
import diploma.ecommerce.backend.shopbase.service.RegistrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegistrationServiceImpl implements RegistrationService {

    private final TenantRepository tenantRepository;
    private final TenantUserRepository tenantUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Value("${app.base-url:https://diploma.ru}")
    private String baseUrl;

    @Override
    @Transactional
    public RegisterTenantResponse registerTenant(RegisterTenantRequest request) {
        log.info(
                "Starting registration process for tenant: {}, subdomain: {}",
                request.getTenantName(), request.getSubdomain()
        );

        validateRequest(request);

        try {
            Tenant tenant = createTenant(request);
            log.info("Created new tenant: {} (ID: {})", tenant.getName(), tenant.getId());

            TenantUser tenantUser = createTenantAdmin(request, tenant);
            log.info(
                    "Created admin user '{}' for tenant ID: {}",
                    tenantUser.getUsernameInTenant(), tenant.getId()
            );

            String jwt = jwtUtil.generateToken(tenantUser);

            // Формируем URL для входа в магазин
            String loginUrl = baseUrl.replace("diploma.ru", request.getSubdomain() + ".diploma.ru");

            RegisterTenantResponse response = new RegisterTenantResponse(
                    tenant.getId(),
                    tenant.getName(),
                    tenant.getSubdomain(),
                    jwt,
                    loginUrl
            );

            log.info("Successfully completed registration for tenant ID: {}", tenant.getId());
            return response;

        } catch (DataIntegrityViolationException ex) {
            log.error("Failed to register tenant due to data integrity violation", ex);
            throw new BadRequestException("Tenant registration failed. Email or username might already be in use.");
        } catch (Exception ex) {
            log.error("Unexpected error during tenant registration", ex);
            throw new RuntimeException("Tenant registration failed due to an unexpected error", ex);
        }
    }

    @Override
    public void validateRequest(RegisterTenantRequest request){
        if (!isShopNameAvailable(request.getTenantName())){
            log.warn("ShopName '{}' is already taken", request.getTenantName());
            throw new BadRequestException("ShopName '" + request.getTenantName() + "' is already taken");
        }

        if (!isSubdomainAvailable(request.getSubdomain())) {
            log.warn("Subdomain '{}' is already taken", request.getSubdomain());
            throw new BadRequestException("Subdomain '" + request.getEmail() + "' is already taken");
        }

        if (!isEmailAvailable(request.getEmail())){
            log.warn("Email '{}' is already taken", request.getEmail());
            throw new BadRequestException("Email '" + request.getEmail() + "' is already taken");
        }

        if (!isUsernameAvailable(request.getUsername())){
            log.warn("Username '{}' is already taken", request.getUsername());
            throw new BadRequestException("Username '" + request.getUsername() + "' is already taken");
        }
    }

    @Override
    public boolean isSubdomainAvailable(String subdomain) {
        return tenantRepository.findBySubdomain(subdomain).isEmpty();
    }

    @Override
    public boolean isUsernameAvailable(String username) {
        return tenantUserRepository.findByUsernameInTenant(username).isEmpty();
    }

    @Override
    public boolean isShopNameAvailable(String shopName) {
        return !tenantRepository.existsByName(shopName);
    }

    @Override
    public boolean isEmailAvailable(String email){
        return !tenantUserRepository.existsByEmail(email);
    }

    private Tenant createTenant(RegisterTenantRequest request) {
        Tenant tenant = new Tenant();
        tenant.setName(request.getTenantName());
        tenant.setSubdomain(request.getSubdomain().toLowerCase().trim());
        tenant.setActive(true);

        return tenantRepository.save(tenant);
    }

    private TenantUser createTenantAdmin(RegisterTenantRequest request, Tenant tenant) {
        TenantUser tenantUser = new TenantUser();
        tenantUser.setTenant(tenant);
        tenantUser.setUsernameInTenant(request.getUsername().trim());
        tenantUser.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        tenantUser.setEmail(request.getEmail().trim().toLowerCase());
        tenantUser.setFirstName(request.getFirstName());
        tenantUser.setLastName(request.getLastName());
        tenantUser.setRole("ROLE_ADMIN");
        tenantUser.setActive(true);

        return tenantUserRepository.save(tenantUser);
    }
}
