package diploma.ecommerce.backend.shopbase.integration;

import java.util.Optional;

import diploma.ecommerce.backend.shopbase.dto.request.AuthRequest;
import diploma.ecommerce.backend.shopbase.dto.request.RegisterTenantRequest;
import diploma.ecommerce.backend.shopbase.dto.response.AuthResponse;
import diploma.ecommerce.backend.shopbase.dto.response.RegisterTenantResponse;
import diploma.ecommerce.backend.shopbase.model.Tenant;
import diploma.ecommerce.backend.shopbase.model.TenantUser;
import diploma.ecommerce.backend.shopbase.multitenancy.TenantContext;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.blankOrNullString;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("AuthController Integration Tests")
public class AuthControllerIntegrationTest extends BaseIntegrationTest {

    private static final String AUTH_LOGIN_URL = "/api/v1/auth/login";
    private static final String AUTH_REGISTER_URL = "/api/v1/auth/register";
    private static final String CHECK_SUBDOMAIN_URL = "/api/v1/auth/check-subdomain/";
    private static final String RAW_PASSWORD = "password";

    @BeforeEach
    void clearTenantContextBeforeAuthTest() {
        TenantContext.clear();
    }

    @AfterEach
    void clearTenantContextAfterAuthTest() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("POST /login - Valid Credentials - Returns 200 OK with Token and User Info")
    void authenticateUser_whenValidCredentials_shouldReturnTokenAndUserInfo() throws Exception {
        AuthRequest authRequest = new AuthRequest();
        String loginUsername = tenant1.getSubdomain() + ":" + userTenant1.getUsernameInTenant();
        authRequest.setUsername(loginUsername);
        authRequest.setPassword(RAW_PASSWORD);

        MvcResult mvcResult = mockMvc.perform(post(AUTH_LOGIN_URL)
                                                      .contentType(MediaType.APPLICATION_JSON)
                                                      .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token", notNullValue()))
                .andReturn();

        String responseBody = mvcResult.getResponse().getContentAsString();
        AuthResponse authResponse = objectMapper.readValue(responseBody, AuthResponse.class);
        assertNotNull(authResponse.getToken());

        String subjectFromToken = jwtUtil.extractClaim(authResponse.getToken(), Claims::getSubject);
        assertEquals(
                userTenant1.getUsernameInTenant(),
                subjectFromToken,
                "Subject в JWT токене должен соответствовать username из запроса"
        );
    }

    @Test
    @DisplayName("POST /login - Invalid Password - Returns 401 Unauthorized")
    void authenticateUser_whenInvalidPassword_shouldReturnUnauthorized() throws Exception {
        AuthRequest authRequest = new AuthRequest();
        authRequest.setUsername(tenant1.getSubdomain() + ":" + userTenant1.getUsernameInTenant());
        authRequest.setPassword("wrongpassword");

        mockMvc.perform(post(AUTH_LOGIN_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(blankOrNullString()));
    }

    @Test
    @DisplayName("POST /login - NonExistent User in Tenant - Returns 401 Unauthorized")
    void authenticateUser_whenUserDoesNotExistInTenant_shouldReturnUnauthorized() throws Exception {
        AuthRequest authRequest = new AuthRequest();
        authRequest.setUsername(tenant1.getSubdomain() + ":nonexistentuser");
        authRequest.setPassword(RAW_PASSWORD);

        mockMvc.perform(post(AUTH_LOGIN_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(blankOrNullString()));
    }

    @Test
    @DisplayName("POST /login - NonExistent Tenant Subdomain in Username - Returns 401 Unauthorized")
    void authenticateUser_whenTenantSubdomainDoesNotExist_shouldReturnUnauthorized() throws Exception {
        AuthRequest authRequest = new AuthRequest();
        authRequest.setUsername("nonexistenttenant:" + (userTenant1 != null ? userTenant1.getUsernameInTenant() :
                "editor1"));
        authRequest.setPassword(RAW_PASSWORD);

        mockMvc.perform(post(AUTH_LOGIN_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(blankOrNullString()));
    }


    @Test
    @DisplayName("POST /login - Only username without tenant prefix - Returns 200 OK now")
    void authenticateUser_whenOnlyUsername_shouldSucceed() throws Exception {
        AuthRequest authRequest = new AuthRequest();
        authRequest.setUsername(userTenant1.getUsernameInTenant()); // "editor1" без "store1:"
        authRequest.setPassword(RAW_PASSWORD);

        mockMvc.perform(post(AUTH_LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token", notNullValue()));
    }


    @Test
    @DisplayName("POST /login - Username Missing (null) - Returns 400 Bad Request")
    void authenticateUser_whenUsernameIsNull_shouldReturnBadRequest() throws Exception {
        AuthRequest authRequest = new AuthRequest();
        authRequest.setUsername(null);
        authRequest.setPassword(RAW_PASSWORD);

        mockMvc.perform(post(AUTH_LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is(HttpStatus.BAD_REQUEST.value())))
                // Обновляем проверку сообщения - убираем упоминание tenant identifier
                .andExpect(jsonPath("$.fieldErrors.username", is("Username is required")))
                .andExpect(jsonPath("$.fieldErrors.password").doesNotExist());
    }

    @Test
    @DisplayName("POST /login - Username Blank - Returns 400 Bad Request")
    void authenticateUser_whenUsernameIsBlank_shouldReturnBadRequest() throws Exception {
        AuthRequest authRequest = new AuthRequest();
        authRequest.setUsername("   ");
        authRequest.setPassword(RAW_PASSWORD);

        mockMvc.perform(post(AUTH_LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is(HttpStatus.BAD_REQUEST.value())))
                // Обновляем проверку сообщения - убираем упоминание tenant identifier
                .andExpect(jsonPath("$.fieldErrors.username", is("Username is required")))
                .andExpect(jsonPath("$.fieldErrors.password").doesNotExist());
    }

    @Test
    @DisplayName("POST /login - Password Missing (null) - Returns 400 Bad Request")
    void authenticateUser_whenPasswordIsNull_shouldReturnBadRequest() throws Exception {
        AuthRequest authRequest = new AuthRequest();
        authRequest.setUsername(tenant1.getSubdomain() + ":" + userTenant1.getUsernameInTenant());
        authRequest.setPassword(null);

        mockMvc.perform(post(AUTH_LOGIN_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is(HttpStatus.BAD_REQUEST.value())))
                .andExpect(jsonPath("$.fieldErrors.password", is("Password is required")))
                .andExpect(jsonPath("$.fieldErrors.username").doesNotExist());
    }

    @Test
    @DisplayName("POST /login - Password Blank - Returns 400 Bad Request")
    void authenticateUser_whenPasswordIsBlank_shouldReturnBadRequest() throws Exception {
        AuthRequest authRequest = new AuthRequest();
        authRequest.setUsername(tenant1.getSubdomain() + ":" + userTenant1.getUsernameInTenant());
        authRequest.setPassword("  ");

        mockMvc.perform(post(AUTH_LOGIN_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is(HttpStatus.BAD_REQUEST.value())))
                .andExpect(jsonPath("$.fieldErrors.password", is("Password is required")))
                .andExpect(jsonPath("$.fieldErrors.username").doesNotExist());
    }

    @Test
    @DisplayName("POST /login - Empty JSON Request Body - Returns 400 Bad Request with two validation errors")
    void authenticateUser_whenBodyIsEmptyJson_shouldReturnBadRequestWithTwoErrors() throws Exception {
        mockMvc.perform(post(AUTH_LOGIN_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is(HttpStatus.BAD_REQUEST.value())))
                .andExpect(jsonPath("$.message", is("Validation failed")))
                .andExpect(jsonPath("$.fieldErrors.username", is("Username is required")))
                .andExpect(jsonPath("$.fieldErrors.password", is("Password is required")));
    }

    @Test
    @DisplayName("POST /login - Inactive Tenant - Returns 401 Unauthorized (via DisabledException)")
    void authenticateUser_whenTenantIsInactive_shouldReturnUnauthorized() throws Exception {
        tenant1.setActive(false);
        tenantRepository.saveAndFlush(tenant1);

        AuthRequest authRequest = new AuthRequest();
        String loginUsername = tenant1.getSubdomain() + ":" + userTenant1.getUsernameInTenant();
        authRequest.setUsername(loginUsername);
        authRequest.setPassword(RAW_PASSWORD);

        mockMvc.perform(post(AUTH_LOGIN_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(blankOrNullString()));

        tenant1.setActive(true);
        tenantRepository.saveAndFlush(tenant1);
    }

    @Test
    @DisplayName("POST /login - Inactive User - Returns 401 Unauthorized (via DisabledException)")
    void authenticateUser_whenUserIsInactive_shouldReturnUnauthorized() throws Exception {
        userTenant1.setActive(false);
        tenantUserRepository.saveAndFlush(userTenant1);

        AuthRequest authRequest = new AuthRequest();
        authRequest.setUsername(tenant1.getSubdomain() + ":" + userTenant1.getUsernameInTenant());
        authRequest.setPassword(RAW_PASSWORD);

        mockMvc.perform(post(AUTH_LOGIN_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(blankOrNullString()));

        userTenant1.setActive(true);
        tenantUserRepository.saveAndFlush(userTenant1);
    }

    @Nested
    @DisplayName("Tenant Registration Tests")
    class TenantRegistrationTests {

        private RegisterTenantRequest validRequest;

        @BeforeEach
        void setUp() {
            TenantContext.clear();
            validRequest = new RegisterTenantRequest();
            validRequest.setTenantName("New Unique Store");
            validRequest.setSubdomain("newuniquestore");
            validRequest.setUsername("mainadmin");
            validRequest.setPassword("password123Secure!");
            validRequest.setEmail("mainadmin@newuniquestore.com");
            validRequest.setFirstName("Main");
            validRequest.setLastName("Admin");
        }

        @AfterEach
        void tearDownNested() {
            tenantRepository.findBySubdomain("newuniquestore").ifPresent(tenant -> {
                tenantUserRepository.findByTenantIdAndUsernameInTenant(tenant.getId(), "mainadmin")
                        .ifPresent(tenantUserRepository::delete);
                tenantRepository.delete(tenant);
            });
        }


        @Test
        @DisplayName("POST /register - Valid Request - Returns 201 Created with Tenant and Token")
        void registerTenant_whenValidRequest_shouldReturnCreatedTenantWithToken() throws Exception {
            MvcResult mvcResult = mockMvc.perform(post(AUTH_REGISTER_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.tenantId", notNullValue()))
                    .andExpect(jsonPath("$.tenantName", is(validRequest.getTenantName())))
                    .andExpect(jsonPath("$.subdomain", is(validRequest.getSubdomain())))
                    .andExpect(jsonPath("$.accessToken", notNullValue()))
                    .andExpect(jsonPath("$.loginUrl", containsString(validRequest.getSubdomain() + rootDomain)))
                    .andReturn();

            String responseBody = mvcResult.getResponse().getContentAsString();
            RegisterTenantResponse response = objectMapper.readValue(responseBody, RegisterTenantResponse.class);

            // Проверки на создание tenant и user остаются прежними
            Optional<Tenant> createdTenantOpt = tenantRepository.findById(response.getTenantId());
            assertTrue(createdTenantOpt.isPresent(), "Tenant should be created in database");
            Tenant createdTenant = createdTenantOpt.get();
            assertEquals(validRequest.getTenantName(), createdTenant.getName());
            assertEquals(validRequest.getSubdomain(), createdTenant.getSubdomain());
            assertTrue(createdTenant.isActive());

            Optional<TenantUser> createdUserOpt = tenantUserRepository.findByTenantIdAndUsernameInTenant(
                    response.getTenantId(), validRequest.getUsername());
            assertTrue(createdUserOpt.isPresent(), "Admin user should be created in database");
            TenantUser createdUser = createdUserOpt.get();
            assertEquals(validRequest.getEmail(), createdUser.getEmail());
            assertEquals("ROLE_ADMIN", createdUser.getRole());
            assertTrue(createdUser.isActive());
            assertEquals(validRequest.getFirstName(), createdUser.getFirstName());
            assertEquals(validRequest.getLastName(), createdUser.getLastName());

            String token = response.getAccessToken();
            // Проверяем, что subject в токене теперь просто username
            String subjectFromToken = jwtUtil.extractClaim(token, Claims::getSubject);
            assertEquals(
                    validRequest.getUsername(), subjectFromToken,
                    "Subject in JWT token should be just the username"
            );

            // Проверяем, что в токене есть tenant_id
            Long tenantIdFromToken = jwtUtil.extractClaim(token,
                    claims -> claims.get("tenant_id", Long.class));
            assertEquals(
                    response.getTenantId(),
                    tenantIdFromToken,
                    "JWT token should contain correct tenant_id"
            );

            // Проверяем, что в токене есть full_username
            String fullUsernameFromToken = jwtUtil.extractClaim(token,
                    claims -> claims.get("full_username", String.class));
            assertEquals(
                    validRequest.getSubdomain() + ":" + validRequest.getUsername(),
                    fullUsernameFromToken,
                    "JWT token should contain correct full_username"
            );
        }

        @Test
        @DisplayName("POST /register - Existing Subdomain - Returns 400 Bad Request")
        void registerTenant_whenSubdomainAlreadyExists_shouldReturnBadRequest() throws Exception {
            validRequest.setSubdomain(tenant1.getSubdomain());

            mockMvc.perform(post(AUTH_REGISTER_URL)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status", is(HttpStatus.BAD_REQUEST.value())))
                    .andExpect(jsonPath(
                            "$.message",
                            containsString("Subdomain '" + tenant1.getSubdomain() + "' is already taken")
                    ));
        }

        @Test
        @DisplayName("POST /register - Invalid Subdomain Format - Returns 400 Bad Request")
        void registerTenant_whenInvalidSubdomainFormat_shouldReturnBadRequest() throws Exception {
            validRequest.setSubdomain("invalid subdomain with spaces");

            mockMvc.perform(post(AUTH_REGISTER_URL)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath(
                            "$.fieldErrors.subdomain",
                            containsString("Subdomain can only contain lowercase letters, " +
                                                   "numbers, and hyphens, and cannot start or end with a hyphen")
                    ));
        }
    }

    @Nested
    @DisplayName("Subdomain Availability Tests")
    class SubdomainAvailabilityTests {

        @Test
        @DisplayName("GET /check-subdomain/{subdomain} - Available Subdomain - Returns true")
        void checkSubdomainAvailability_whenSubdomainAvailable_shouldReturnTrue() throws Exception {
            String availableSubdomain = "unique-new-subdomain";
            tenantRepository.findBySubdomain(availableSubdomain).ifPresent(tenantRepository::delete);


            mockMvc.perform(get(CHECK_SUBDOMAIN_URL + availableSubdomain))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.available", is(true)));
        }

        @Test
        @DisplayName("GET /check-subdomain/{subdomain} - Existing Subdomain - Returns false")
        void checkSubdomainAvailability_whenSubdomainExists_shouldReturnFalse() throws Exception {
            String existingSubdomain = tenant1.getSubdomain();

            mockMvc.perform(get(CHECK_SUBDOMAIN_URL + existingSubdomain))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.available", is(false)));
        }

        @Test
        @DisplayName("GET /check-subdomain/{subdomain} - Invalid Subdomain Format (PathVariable) - Returns 400 Bad " +
                "Request")
        void checkSubdomainAvailability_whenInvalidFormatAsPathVariable_shouldReturnBadRequest() throws Exception {
            String invalidSubdomain = "Invalid Subdomain With Spaces";

            mockMvc.perform(get(CHECK_SUBDOMAIN_URL + invalidSubdomain))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath(
                            "$.message",
                            containsString(
                                    "Validation failure")
                    ));
        }
    }

    @Test
    @DisplayName("POST /login - Valid Credentials with only username (no tenant prefix) - Returns 200 OK with Token")
    void authenticateUser_whenValidCredentialsWithoutTenantPrefix_shouldReturnToken() throws Exception {
        AuthRequest authRequest = new AuthRequest();
        // Используем только имя пользователя без поддомена
        authRequest.setUsername(userTenant1.getUsernameInTenant());
        authRequest.setPassword(RAW_PASSWORD);

        MvcResult mvcResult = mockMvc.perform(post(AUTH_LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token", notNullValue()))
                .andReturn();

        String responseBody = mvcResult.getResponse().getContentAsString();
        AuthResponse authResponse = objectMapper.readValue(responseBody, AuthResponse.class);
        assertNotNull(authResponse.getToken());

        // Проверяем, что в JWT токене правильный subject (просто имя пользователя)
        String subjectFromToken = jwtUtil.extractClaim(authResponse.getToken(), Claims::getSubject);
        assertEquals(
                userTenant1.getUsernameInTenant(),
                subjectFromToken,
                "Subject в JWT токене должен соответствовать username из запроса без поддомена"
        );

        // Проверяем, что в JWT токене содержится информация о tenant_id
        Long tenantIdFromToken = jwtUtil.extractClaim(authResponse.getToken(),
                claims -> claims.get("tenant_id", Long.class));
        assertEquals(
                userTenant1.getTenant().getId(),
                tenantIdFromToken,
                "В JWT токене должен быть правильный tenant_id"
        );

        // Проверяем, что в JWT токене содержится full_username
        String fullUsernameFromToken = jwtUtil.extractClaim(authResponse.getToken(),
                claims -> claims.get("full_username", String.class));
        assertEquals(
                tenant1.getSubdomain() + ":" + userTenant1.getUsernameInTenant(),
                fullUsernameFromToken,
                "В JWT токене должен быть правильный full_username"
        );
    }

    @Test
    @DisplayName("POST /login - Username exists in multiple tenants - Returns 401 Unauthorized (ambiguous username)")
    void authenticateUser_whenUsernameExistsInMultipleTenants_shouldReturnUnauthorized() throws Exception {
        // Создаем пользователя с таким же username, но в другом tenant
        TenantUser duplicateUser = new TenantUser();
        duplicateUser.setTenant(tenant2);
        duplicateUser.setUsernameInTenant(userTenant1.getUsernameInTenant()); // Дублирующийся username
        duplicateUser.setPasswordHash(passwordEncoder.encode(RAW_PASSWORD));
        duplicateUser.setEmail("duplicate@" + tenant2.getSubdomain() + ".com");
        duplicateUser.setActive(true);
        duplicateUser.setRole("ROLE_ADMIN");
        tenantUserRepository.saveAndFlush(duplicateUser);

        AuthRequest authRequest = new AuthRequest();
        authRequest.setUsername(userTenant1.getUsernameInTenant());
        authRequest.setPassword(RAW_PASSWORD);

        // Проверяем, что аутентификация не прошла из-за неоднозначности
        mockMvc.perform(post(AUTH_LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(blankOrNullString()));

        // Удаляем созданного пользователя
        tenantUserRepository.delete(duplicateUser);
    }
}
