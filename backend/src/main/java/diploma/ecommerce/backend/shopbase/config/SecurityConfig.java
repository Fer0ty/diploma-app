package diploma.ecommerce.backend.shopbase.config;

import diploma.ecommerce.backend.shopbase.multitenancy.TenantIdentificationFilter;
import diploma.ecommerce.backend.shopbase.security.DetailsService;
import diploma.ecommerce.backend.shopbase.security.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final TenantIdentificationFilter tenantIdentificationFilter;
    private final DetailsService userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/health", "/api/v1/test").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // swagger
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

                        // авторизация
                        .requestMatchers("/api/v1/auth/**").permitAll()

                        // товары и фотографи товаров
                        .requestMatchers(HttpMethod.GET, "/api/v1/products/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/product-photos/**").permitAll()

                        // CUD товаров
                        .requestMatchers(HttpMethod.POST, "/api/v1/products").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/products/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/products/**").hasRole("ADMIN")

                        // CUD фотографий
                        .requestMatchers(HttpMethod.POST, "/api/v1/product-photos/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/product-photos/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/product-photos/**").hasRole("ADMIN")

                        // статусы заказов
                        .requestMatchers("/api/v1/order-statuses/**").hasRole("ADMIN")

                        // заказы
                        .requestMatchers(HttpMethod.POST, "/api/v1/orders").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/orders/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/orders/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/orders/**").hasRole("ADMIN")
                        .requestMatchers("/api/v1/order-items/**").hasRole("ADMIN")

                        // адрес
                        .requestMatchers(HttpMethod.GET, "/api/v1/addresses/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/v1/addresses").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/api/v1/addresses/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/addresses/**").hasRole("ADMIN")

                        // Покупатели
                        .requestMatchers(HttpMethod.POST, "/api/v1/users").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/users/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/users/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/users/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/v1/users/*/activate").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/v1/users/*/deactivate").hasRole("ADMIN")

                        // Управление магазином
                        .requestMatchers("/api/v1/store/**").hasRole("ADMIN")

                        // Управление темой
                        .requestMatchers("/api/v1/theme").hasRole("ADMIN")
                        .requestMatchers("/api/v1/public/theme").permitAll()

                        // Загрузка файлов
                        .requestMatchers("/api/v1/files/**").hasRole("ADMIN")
                        .requestMatchers("/uploads/**").permitAll()

                        // тестовый контролле
                        .requestMatchers("/test-tenant-api/current-tenant-id").permitAll()
                        .requestMatchers("/test-tenant-api/protected-echo-tenant-id").authenticated()

                        // все остальнео
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(tenantIdentificationFilter, JwtAuthFilter.class);

        return http.build();
    }
}