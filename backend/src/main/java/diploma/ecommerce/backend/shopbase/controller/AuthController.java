package diploma.ecommerce.backend.shopbase.controller;

import java.util.Map;

import diploma.ecommerce.backend.shopbase.dto.request.AuthRequest;
import diploma.ecommerce.backend.shopbase.dto.request.RegisterTenantRequest;
import diploma.ecommerce.backend.shopbase.dto.response.AuthResponse;
import diploma.ecommerce.backend.shopbase.dto.response.RegisterTenantResponse;
import diploma.ecommerce.backend.shopbase.security.JwtUtil;
import diploma.ecommerce.backend.shopbase.service.RegistrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "API для аутентификации пользователя (администратора/сотрудника тенанта)")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final RegistrationService registrationService;

    @Operation(summary = "Аутентификация пользователя и получение JWT токена")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Аутентификация успешна, токен выдан",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "400", description = "Некорректный формат запроса (например, пустой " +
                    "логин/пароль)"),
            @ApiResponse(responseCode = "401", description = "Неверные учетные данные (логин/пароль)")
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> authenticateUser(@Valid @RequestBody AuthRequest authRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword())
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String jwt = jwtUtil.generateToken(userDetails);

            return ResponseEntity.ok(new AuthResponse(jwt));

        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (AuthenticationException ex) {
            log.warn("Authentication failed for user {}: {}", authRequest.getUsername(), ex.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @Operation(summary = "Регистрация нового тенанта (магазина) и администратора")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Тенант и администратор успешно созданы",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = RegisterTenantResponse.class))),
            @ApiResponse(responseCode = "400", description = "Некорректный формат запроса или данные уже существуют"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @PostMapping("/register")
    public ResponseEntity<RegisterTenantResponse> registerTenant(@Valid @RequestBody RegisterTenantRequest request) {
        log.debug("REST request to register new tenant: {}", request.getTenantName());
        RegisterTenantResponse response = registrationService.registerTenant(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Проверка доступности поддомена для нового тенанта")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Запрос выполнен успешно"),
            @ApiResponse(responseCode = "400", description = "Некорректный формат поддомена")
    })
    @GetMapping("/check-subdomain/{subdomain}")
    public ResponseEntity<Map<String, Boolean>> checkSubdomainAvailability(
            @PathVariable @Pattern(regexp = "^[a-z0-9](?:[a-z0-9\\-]{0,61}[a-z0-9])?$",
                    message = "Invalid subdomain format") String subdomain
    ) {
        log.debug("REST request to check subdomain availability: {}", subdomain);
        boolean isAvailable = registrationService.isSubdomainAvailable(subdomain);
        return ResponseEntity.ok(Map.of("available", isAvailable));
    }
}
