package diploma.ecommerce.backend.shopbase.multitenancy;

import diploma.ecommerce.backend.shopbase.model.Tenant;
import diploma.ecommerce.backend.shopbase.model.TenantUser;
import diploma.ecommerce.backend.shopbase.repository.TenantRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TenantIdentificationFilter extends OncePerRequestFilter {

    private final TenantRepository tenantRepository;

    private static final String HEADER_TENANT_SUBDOMAIN = "X-Tenant-Subdomain";
    private static final String HEADER_TENANT_HOST = "X-Tenant-Host";
    private static final String ROOT_DOMAIN = ".diploma.ru";
    private static final String LOCAL_DEV_HOST = "localhost";

    private static boolean isIgnoredPath(String path) {
        return path.startsWith("/swagger-ui/") ||
               path.startsWith("/v3/api-docs") ||
               path.startsWith("/api/v1/auth/");
    }

    private static String extractSubdomain(String host) {
        if (host == null || host.isEmpty()) return null;

        int colonIndex = host.indexOf(':');
        if (colonIndex != -1) {
            host = host.substring(0, colonIndex);
        }

        if (host.equalsIgnoreCase(LOCAL_DEV_HOST) || host.equalsIgnoreCase(ROOT_DOMAIN.substring(1))) {
            return null;
        }

        if (host.endsWith(ROOT_DOMAIN)) {
            String subdomain = host.substring(0, host.length() - ROOT_DOMAIN.length());
            if (!subdomain.isEmpty() && !subdomain.contains(".")) {
                return subdomain;
            }
        }

        return null;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        if (isIgnoredPath(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }

        Long tenantId = extractTenantIdFromAuthentication();
        if (tenantId == null) {
            String subdomain = resolveSubdomain(request);
            if (subdomain != null) {
                Optional<Tenant> tenantOpt = tenantRepository.findBySubdomain(subdomain);
                if (tenantOpt.isEmpty()) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND,
                            "The store at '" + request.getServerName() + "' was not found.");
                    return;
                }

                Tenant tenant = tenantOpt.get();

                if (!tenant.isActive()) {
                    response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                            "This store is temporarily unavailable.");
                    return;
                }

                tenantId = tenant.getId();
            }
        }

        if (tenantId != null) {
            TenantContext.setTenantId(tenantId);
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }

    private Long extractTenantIdFromAuthentication() throws IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof TenantUser tenantUser) {
            Tenant tenant = tenantUser.getTenant();
            if (tenant != null && tenant.getId() != null) {
                return tenant.getId();
            }
        }
        return null;
    }

    private String resolveSubdomain(HttpServletRequest request) {
        String subdomain = request.getHeader(HEADER_TENANT_SUBDOMAIN);
        if (subdomain == null || subdomain.isEmpty()) {
            String tenantHost = request.getHeader(HEADER_TENANT_HOST);
            if (tenantHost != null && !tenantHost.isEmpty()) {
                subdomain = extractSubdomain(tenantHost);
            } else {
                subdomain = extractSubdomain(request.getServerName());
            }
        }
        return subdomain;
    }
}