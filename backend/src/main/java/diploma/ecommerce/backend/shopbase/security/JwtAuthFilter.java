package diploma.ecommerce.backend.shopbase.security;

import java.io.IOException;

import diploma.ecommerce.backend.shopbase.multitenancy.TenantContext;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;


@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);

        try {
            String username = jwtUtil.extractUsername(jwt);

            // Проверяем, есть ли tenant_id и full_username в токене
            Long tenantId = null;
            String fullUsername = null;
            try {
                Claims claims = jwtUtil.extractAllClaims(jwt);
                if (claims.containsKey("tenant_id")) {
                    tenantId = claims.get("tenant_id", Long.class);
                }
                if (claims.containsKey("full_username")) {
                    fullUsername = claims.get("full_username", String.class);
                }
            } catch (Exception e) {
                log.warn("Error extracting claims from JWT", e);
            }

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // Если в токене есть full_username, используем его для загрузки пользователя
                UserDetails userDetails = (fullUsername != null)
                        ? userDetailsService.loadUserByUsername(fullUsername)
                        : userDetailsService.loadUserByUsername(username);

                if (!userDetails.isEnabled()) {
                    log.warn("User account '{}' is disabled.", username);
                    throw new DisabledException("User account is disabled");
                }

                if (jwtUtil.isTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    // Устанавливаем tenant_id в контекст, если он был в токене
                    if (tenantId != null) {
                        TenantContext.setTenantId(tenantId);
                    }

                    log.debug("Authentication set for user: {}", username);
                } else {
                    log.warn("JWT token is invalid for user: {}", username);
                }
            }
        } catch (Exception e) {
            log.warn("Cannot set user authentication due to: {}", e.getMessage(), e);
        }

        filterChain.doFilter(request, response);
    }
}
