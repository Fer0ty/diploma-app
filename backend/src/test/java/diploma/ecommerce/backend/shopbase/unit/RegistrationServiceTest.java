package diploma.ecommerce.backend.shopbase.unit;

import java.util.Optional;

import diploma.ecommerce.backend.shopbase.dto.request.RegisterTenantRequest;
import diploma.ecommerce.backend.shopbase.dto.response.RegisterTenantResponse;
import diploma.ecommerce.backend.shopbase.exception.BadRequestException;
import diploma.ecommerce.backend.shopbase.model.Tenant;
import diploma.ecommerce.backend.shopbase.model.TenantUser;
import diploma.ecommerce.backend.shopbase.repository.TenantRepository;
import diploma.ecommerce.backend.shopbase.repository.TenantUserRepository;
import diploma.ecommerce.backend.shopbase.security.JwtUtil;
import diploma.ecommerce.backend.shopbase.service.impl.RegistrationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {

    private static final String TEST_TENANT_NAME = "Test Store";
    private static final String TEST_SUBDOMAIN = "teststore";
    private static final String TEST_USERNAME = "admin";
    private static final String TEST_PASSWORD = "password123";
    private static final String TEST_EMAIL = "admin@teststore.com";
    private static final String TEST_FIRST_NAME = "Admin";
    private static final String TEST_LAST_NAME = "User";
    private static final Long TEST_TENANT_ID = 1L;
    private static final String MOCK_JWT_TOKEN = "mock.jwt.token.string";
    private static final String ENCODED_PASSWORD = "encoded_password";
    private static final String BASE_URL = "https://diploma.ru";

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private TenantUserRepository tenantUserRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private RegistrationServiceImpl registrationService;

    @Captor
    private ArgumentCaptor<Tenant> tenantCaptor;

    @Captor
    private ArgumentCaptor<TenantUser> tenantUserCaptor;

    private RegisterTenantRequest request;
    private Tenant tenant;
    private TenantUser tenantUser;

    @BeforeEach
    void setUp() {
        request = new RegisterTenantRequest(
                TEST_TENANT_NAME,
                TEST_SUBDOMAIN,
                TEST_USERNAME,
                TEST_PASSWORD,
                TEST_EMAIL,
                TEST_FIRST_NAME,
                TEST_LAST_NAME
        );

        tenant = new Tenant();
        tenant.setId(TEST_TENANT_ID);
        tenant.setName(TEST_TENANT_NAME);
        tenant.setSubdomain(TEST_SUBDOMAIN);
        tenant.setActive(true);

        tenantUser = new TenantUser();
        tenantUser.setId(1L);
        tenantUser.setTenant(tenant);
        tenantUser.setUsernameInTenant(TEST_USERNAME);
        tenantUser.setPasswordHash(ENCODED_PASSWORD);
        tenantUser.setEmail(TEST_EMAIL);
        tenantUser.setFirstName(TEST_FIRST_NAME);
        tenantUser.setLastName(TEST_LAST_NAME);
        tenantUser.setRole("ROLE_ADMIN");
        tenantUser.setActive(true);

        ReflectionTestUtils.setField(registrationService, "baseUrl", BASE_URL);
    }

    @Test
    @DisplayName("registerTenant - Valid Data - Returns Response With Token")
    void registerTenant_ValidData_ReturnsResponseWithToken() {
        when(tenantRepository.findBySubdomain(TEST_SUBDOMAIN)).thenReturn(Optional.empty());
        when(tenantRepository.save(any(Tenant.class))).thenReturn(tenant);
        when(passwordEncoder.encode(TEST_PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(tenantUserRepository.save(any(TenantUser.class))).thenReturn(tenantUser);
        when(jwtUtil.generateToken(any(TenantUser.class))).thenReturn(MOCK_JWT_TOKEN);

        RegisterTenantResponse response = registrationService.registerTenant(request);

        assertNotNull(response);
        assertEquals(TEST_TENANT_ID, response.getTenantId());
        assertEquals(TEST_TENANT_NAME, response.getTenantName());
        assertEquals(TEST_SUBDOMAIN, response.getSubdomain());
        assertEquals(MOCK_JWT_TOKEN, response.getAccessToken());
        assertEquals("https://teststore.diploma.ru", response.getLoginUrl());

        verify(tenantRepository).findBySubdomain(TEST_SUBDOMAIN);
        verify(tenantRepository).save(tenantCaptor.capture());
        Tenant capturedTenant = tenantCaptor.getValue();
        assertNull(capturedTenant.getId());
        assertEquals(TEST_TENANT_NAME, capturedTenant.getName());
        assertEquals(TEST_SUBDOMAIN, capturedTenant.getSubdomain());
        assertTrue(capturedTenant.isActive());

        verify(passwordEncoder).encode(TEST_PASSWORD);
        verify(tenantUserRepository).save(tenantUserCaptor.capture());
        TenantUser capturedUser = tenantUserCaptor.getValue();
        assertNull(capturedUser.getId());
        assertEquals(tenant, capturedUser.getTenant());
        assertEquals(TEST_USERNAME, capturedUser.getUsernameInTenant());
        assertEquals(ENCODED_PASSWORD, capturedUser.getPasswordHash());
        assertEquals(TEST_EMAIL, capturedUser.getEmail());
        assertEquals(TEST_FIRST_NAME, capturedUser.getFirstName());
        assertEquals(TEST_LAST_NAME, capturedUser.getLastName());
        assertEquals("ROLE_ADMIN", capturedUser.getRole());
        assertTrue(capturedUser.isActive());

        verify(jwtUtil).generateToken(tenantUser);
    }

    @Test
    @DisplayName("registerTenant - Subdomain Already Taken - Throws BadRequestException")
    void registerTenant_EmailAlreadyTaken_ThrowsBadRequestException() {
        when(tenantRepository.findBySubdomain(TEST_SUBDOMAIN)).thenReturn(Optional.of(tenant));

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> registrationService.registerTenant(request)
        );

        assertEquals("Subdomain '" + TEST_EMAIL + "' is already taken", exception.getMessage());

        verify(tenantRepository).findBySubdomain(TEST_SUBDOMAIN);
        verify(tenantRepository, never()).save(any(Tenant.class));
        verify(passwordEncoder, never()).encode(anyString());
        verify(tenantUserRepository, never()).save(any(TenantUser.class));
        verify(jwtUtil, never()).generateToken(any(TenantUser.class));
    }

    @Test
    @DisplayName("registerTenant - Database Error - Rethrows With BadRequestException")
    void registerTenant_DatabaseError_RethrowsWithBadRequestException() {
        when(tenantRepository.findBySubdomain(TEST_SUBDOMAIN)).thenReturn(Optional.empty());
        when(tenantRepository.save(any(Tenant.class))).thenReturn(tenant);
        when(tenantUserRepository.save(any(TenantUser.class)))
                .thenThrow(new DataIntegrityViolationException("Duplicate email"));

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> registrationService.registerTenant(request)
        );

        assertTrue(exception.getMessage().contains("Email or username might already be in use"));

        verify(tenantRepository).findBySubdomain(TEST_SUBDOMAIN);
        verify(tenantRepository).save(any(Tenant.class));
        verify(passwordEncoder).encode(TEST_PASSWORD);
        verify(tenantUserRepository).save(any(TenantUser.class));
        verify(jwtUtil, never()).generateToken(any(TenantUser.class));
    }

    @Test
    @DisplayName("isSubdomainAvailable - Available Subdomain - Returns True")
    void isSubdomainAvailable_AvailableSubdomain_ReturnsTrue() {
        when(tenantRepository.findBySubdomain(TEST_SUBDOMAIN)).thenReturn(Optional.empty());

        boolean result = registrationService.isSubdomainAvailable(TEST_SUBDOMAIN);

        assertTrue(result);
        verify(tenantRepository).findBySubdomain(TEST_SUBDOMAIN);
    }

    @Test
    @DisplayName("isSubdomainAvailable - Unavailable Subdomain - Returns False")
    void isSubdomainAvailable_UnavailableSubdomain_ReturnsFalse() {
        when(tenantRepository.findBySubdomain(TEST_SUBDOMAIN)).thenReturn(Optional.of(tenant));

        boolean result = registrationService.isSubdomainAvailable(TEST_SUBDOMAIN);

        assertFalse(result);
        verify(tenantRepository).findBySubdomain(TEST_SUBDOMAIN);
    }
}
