package diploma.ecommerce.backend.shopbase.integration.tenantIdentification;

import java.time.LocalDateTime;

import diploma.ecommerce.backend.shopbase.integration.BaseIntegrationTest;
import diploma.ecommerce.backend.shopbase.model.Tenant;
import diploma.ecommerce.backend.shopbase.model.TenantUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("TenantIdentificationFilter - Integration Tests (using BaseIntegrationTest)")
public class TenantIdentificationFilterIntegrationTest extends BaseIntegrationTest {

    private static final String TEST_API_CURRENT_TENANT = "/test-tenant-api/current-tenant-id";
    private static final String TEST_API_PROTECTED_ECHO_TENANT = "/test-tenant-api/protected-echo-tenant-id";
    private final String rootDomain = ".diploma.ru";
    private Tenant tenantInactiveGlobal;

    @BeforeEach
    void specificSetUp() {
        tenantInactiveGlobal = new Tenant();
        tenantInactiveGlobal.setName("Globally Inactive Shop");
        tenantInactiveGlobal.setSubdomain("inactiveglobal");
        tenantInactiveGlobal.setActive(false);
        tenantInactiveGlobal.setCreatedAt(LocalDateTime.now());
        tenantInactiveGlobal.setUpdatedAt(LocalDateTime.now());
        tenantRepository.saveAndFlush(tenantInactiveGlobal);
    }

    @AfterEach
    void specificTearDown() {
        if (tenantInactiveGlobal != null && tenantInactiveGlobal.getId() != null) {
            tenantRepository.findById(tenantInactiveGlobal.getId()).ifPresent(tenantRepository::delete);
        }
    }


    @Test
    @DisplayName("JWT Strategy: TenantContext IS SET for API request with valid TenantUser JWT")
    void jwtStrategy_tenantContextSet_forApiWithValidJwt() throws Exception {
        mockMvc.perform(get(TEST_API_PROTECTED_ECHO_TENANT)
                                .headers(getAuthHeaders(jwtTenant1))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.tenantId", is(tenant1.getId().intValue())));
    }

    @Test
    @DisplayName("JWT Strategy: Returns 401 if TenantUser from JWT not found by DetailsService (e.g. dummysub)")
    void jwtStrategy_returns401_ifTenantUserNotFoundInDb() throws Exception {
        Tenant dummyTenantForUser = new Tenant();
        dummyTenantForUser.setSubdomain("dummysub");

        TenantUser userWithProblematicTenant = new TenantUser();
        userWithProblematicTenant.setTenant(dummyTenantForUser);
        userWithProblematicTenant.setUsernameInTenant("problemuser");
        userWithProblematicTenant.setEmail("problem@example.com");
        userWithProblematicTenant.setPasswordHash(passwordEncoder.encode("password"));
        userWithProblematicTenant.setRole("ROLE_USER");
        userWithProblematicTenant.setActive(true);
        String jwtToken = jwtUtil.generateToken(userWithProblematicTenant);

        mockMvc.perform(get(TEST_API_PROTECTED_ECHO_TENANT)
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken))
                .andExpect(status().isUnauthorized());
    }


    @Test
    @DisplayName("Subdomain Strategy: TenantContext IS SET for public API request with valid active subdomain")
    void subdomainStrategy_tenantContextSet_forPublicApiWithValidSubdomain() throws Exception {
        mockMvc.perform(get(TEST_API_CURRENT_TENANT)
                                .with(serverName(tenant1.getSubdomain() + rootDomain))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.tenantId", is(tenant1.getId().intValue())));
    }

    @Test
    @DisplayName("Subdomain Strategy: Returns 404 for public API if subdomain does NOT exist")
    void subdomainStrategy_returns404_ifSubdomainDoesNotExist() throws Exception {
        mockMvc.perform(get(TEST_API_CURRENT_TENANT)
                                .with(serverName("unknownshop" + rootDomain))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(result -> {
                    assertEquals(HttpStatus.NOT_FOUND.value(), result.getResponse().getStatus());
                    assertEquals(
                            "The store at 'unknownshop" + rootDomain + "' was not found.",
                            result.getResponse().getErrorMessage()
                    );
                });
    }

    @Test
    @DisplayName("Subdomain Strategy: Returns 503 for public API if tenant for subdomain is INACTIVE")
    void subdomainStrategy_returns503_ifTenantIsInactive() throws Exception {
        mockMvc.perform(get(TEST_API_CURRENT_TENANT)
                                .with(serverName(tenantInactiveGlobal.getSubdomain() + rootDomain))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isServiceUnavailable())
                .andExpect(result -> {
                    assertEquals(HttpStatus.SERVICE_UNAVAILABLE.value(), result.getResponse().getStatus());
                    assertEquals("This store is temporarily unavailable.", result.getResponse().getErrorMessage());
                });
    }

    @Test
    @DisplayName("Subdomain Strategy: Returns 404 for API if 'api.diploma.ru' (subdomain 'api') does not map to a " +
            "tenant")
    void subdomainStrategy_404_forApiSubdomainNoTenant() throws Exception {
        mockMvc.perform(get(TEST_API_CURRENT_TENANT)
                                .with(serverName("api" + rootDomain))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(result -> {
                    assertEquals(HttpStatus.NOT_FOUND.value(), result.getResponse().getStatus());
                    assertEquals(
                            "The store at 'api" + rootDomain + "' was not found.",
                            result.getResponse().getErrorMessage()
                    );
                });
    }


    @Test
    @DisplayName("Subdomain Strategy: TenantContext IS NULL when root domain is used (no subdomain extracted)")
    void subdomainStrategy_tenantContextNull_forRootDomainNoSubdomain() throws Exception {
        mockMvc.perform(get(TEST_API_CURRENT_TENANT)
                                .with(serverName(rootDomain.substring(1)))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.tenantId", is(nullValue())));
    }


    @Test
    @DisplayName("Subdomain Strategy: TenantContext IS NULL for public API if serverName is localhost")
    void subdomainStrategy_tenantContextNull_forLocalhost() throws Exception {
        mockMvc.perform(get(TEST_API_CURRENT_TENANT)
                                .with(serverName("localhost"))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.tenantId", is(nullValue())));
    }

    @Test
    @DisplayName("Mixed Strategy: JWT has priority over subdomain for API requests")
    void mixedStrategy_jwtHasPriorityOverSubdomain() throws Exception {
        mockMvc.perform(get(TEST_API_PROTECTED_ECHO_TENANT)
                                .headers(getAuthHeaders(jwtTenant1))
                                .with(serverName(tenant2.getSubdomain() + rootDomain))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.tenantId", is(tenant1.getId().intValue())));
    }

    @Test
    @DisplayName("General Case: TenantContext IS NULL for API if no JWT and serverName does not yield subdomain")
    void generalCase_tenantContextNull_ifNoJwtNoExtractableSubdomain() throws Exception {
        mockMvc.perform(get(TEST_API_CURRENT_TENANT)
                                .with(serverName("completely-unrelated-domain.com"))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.tenantId", is(nullValue())));
    }

    @Test
    @DisplayName("General Case: Returns 404 for API if no JWT and subdomain is valid format but tenant not found")
    void generalCase_404_ifNoJwtSubdomainValidFormatButTenantNotFound() throws Exception {
        mockMvc.perform(get(TEST_API_CURRENT_TENANT)
                                .with(serverName("validsubdomainformat" + rootDomain))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(result -> {
                    assertEquals(HttpStatus.NOT_FOUND.value(), result.getResponse().getStatus());
                    assertEquals(
                            "The store at 'validsubdomainformat" + rootDomain + "' was not found.",
                            result.getResponse().getErrorMessage()
                    );
                });
    }

    @Test
    @DisplayName("JWT Strategy: Authentication fails (401) if TenantUser is inactive")
    void jwtStrategy_authFails_ifTenantUserIsInactive() throws Exception {
        userTenant1.setActive(false);
        tenantUserRepository.saveAndFlush(userTenant1);

        mockMvc.perform(get(TEST_API_PROTECTED_ECHO_TENANT)
                                .headers(getAuthHeaders(jwtTenant1))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}
