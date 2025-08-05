package diploma.ecommerce.backend.shopbase.security;

import diploma.ecommerce.backend.shopbase.model.Tenant;
import diploma.ecommerce.backend.shopbase.repository.TenantRepository;
import diploma.ecommerce.backend.shopbase.repository.TenantUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service("tenantUserDetailsService")
@RequiredArgsConstructor
public class DetailsService implements UserDetailsService {

    private final TenantUserRepository tenantUserRepository;
    private final TenantRepository tenantRepository;

    @Value("${app.security.tenant-username-separator::}")
    private String separator;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Attempting to load user by username: {}", username);

        if (username == null || username.isBlank()) {
            log.warn("Username cannot be empty.");
            throw new UsernameNotFoundException("Username cannot be empty.");
        }

        int separatorIndex = username.indexOf(separator);

        if (separatorIndex > 0 && separatorIndex < username.length() - 1) {
            // Старый формат: subdomain:username
            String tenantIdentifier = username.substring(0, separatorIndex);
            String usernameInTenant = username.substring(separatorIndex + 1);
            log.debug("Parsed tenant identifier: '{}', username: '{}'", tenantIdentifier, usernameInTenant);

            Tenant tenant = tenantRepository.findBySubdomain(tenantIdentifier)
                    .orElseThrow(() -> {
                        log.warn("Tenant not found for identifier: {}", tenantIdentifier);
                        return new UsernameNotFoundException("Tenant not found for identifier: " + tenantIdentifier);
                    });

            if (!tenant.isActive()) {
                log.warn("Attempt to login for inactive tenant: {}", tenantIdentifier);
                throw new DisabledException("Tenant " + tenantIdentifier + " is inactive.");
            }

            Long tenantId = tenant.getId();
            log.debug("Found active tenant '{}' with ID: {}", tenant.getName(), tenantId);

            return tenantUserRepository.findByTenantIdAndUsernameInTenant(tenantId, usernameInTenant)
                    .map(tenantUser -> {
                        if (!tenantUser.isActive()) {
                            log.warn("User '{}' in tenant '{}' is inactive.", usernameInTenant, tenantIdentifier);
                        }
                        return (UserDetails) tenantUser;
                    })
                    .orElseThrow(() -> {
                        log.warn("TenantUser not found with username '{}' for Tenant ID: {}", usernameInTenant, tenantId);
                        return new UsernameNotFoundException("Invalid username or password.");
                    });
        } else {
            // Новый формат: только username
            return tenantUserRepository.findByUsernameInTenant(username)
                    .map(tenantUser -> {
                        if (!tenantUser.isActive()) {
                            log.warn("User '{}' is inactive.", username);
                            throw new DisabledException("User account is disabled");
                        }

                        Tenant tenant = tenantUser.getTenant();
                        if (tenant == null) {
                            log.error("User '{}' has no associated tenant.", username);
                            throw new UsernameNotFoundException("User has no associated tenant");
                        }

                        if (!tenant.isActive()) {
                            log.warn("Attempt to login for inactive tenant: {}", tenant.getSubdomain());
                            throw new DisabledException("Tenant " + tenant.getSubdomain() + " is inactive.");
                        }

                        log.debug("Found user '{}' for tenant '{}'", username, tenant.getName());
                        return (UserDetails) tenantUser;
                    })
                    .orElseThrow(() -> {
                        log.warn("TenantUser not found with username '{}'", username);
                        return new UsernameNotFoundException("Invalid username or password.");
                    });
        }
    }
}
