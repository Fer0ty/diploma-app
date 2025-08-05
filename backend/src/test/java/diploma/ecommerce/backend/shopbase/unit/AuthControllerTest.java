package diploma.ecommerce.backend.shopbase.unit;

import java.util.Collections;
import java.util.Map;

import diploma.ecommerce.backend.shopbase.controller.AuthController;
import diploma.ecommerce.backend.shopbase.dto.request.AuthRequest;
import diploma.ecommerce.backend.shopbase.dto.request.RegisterTenantRequest;
import diploma.ecommerce.backend.shopbase.dto.response.AuthResponse;
import diploma.ecommerce.backend.shopbase.dto.response.RegisterTenantResponse;
import diploma.ecommerce.backend.shopbase.exception.BadRequestException;
import diploma.ecommerce.backend.shopbase.security.JwtUtil;
import diploma.ecommerce.backend.shopbase.service.RegistrationService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private static final String TEST_USERNAME = "store1:admin";
    private static final String TEST_PASSWORD = "password123";
    private static final String MOCK_JWT_TOKEN = "mock.jwt.token.string";

    private static final String TEST_TENANT_NAME = "Test Store";
    private static final String TEST_SUBDOMAIN = "teststore";
    private static final String TEST_EMAIL = "admin@teststore.com";
    private static final String TEST_FIRST_NAME = "Admin";
    private static final String TEST_LAST_NAME = "User";
    private static final Long TEST_TENANT_ID = 1L;
    private static final String TEST_LOGIN_URL = "https://teststore.diploma.ru";

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private RegistrationService registrationService;

    @InjectMocks
    private AuthController authController;

    @Captor
    private ArgumentCaptor<UsernamePasswordAuthenticationToken> authenticationTokenCaptor;

    @Captor
    private ArgumentCaptor<RegisterTenantRequest> registerRequestCaptor;

    private AuthRequest authRequest;
    private RegisterTenantRequest registerRequest;
    private RegisterTenantResponse registerResponse;

    @BeforeEach
    void setUp() {
        authRequest = new AuthRequest();
        authRequest.setUsername(TEST_USERNAME);
        authRequest.setPassword(TEST_PASSWORD);

        registerRequest = new RegisterTenantRequest();
        registerRequest.setTenantName(TEST_TENANT_NAME);
        registerRequest.setSubdomain(TEST_SUBDOMAIN);
        registerRequest.setUsername(TEST_USERNAME);
        registerRequest.setPassword(TEST_PASSWORD);
        registerRequest.setEmail(TEST_EMAIL);
        registerRequest.setFirstName(TEST_FIRST_NAME);
        registerRequest.setLastName(TEST_LAST_NAME);

        registerResponse = new RegisterTenantResponse(
                TEST_TENANT_ID,
                TEST_TENANT_NAME,
                TEST_SUBDOMAIN,
                MOCK_JWT_TOKEN,
                TEST_LOGIN_URL
        );
    }

    @Nested
    @DisplayName("Authenticate User Tests")
    class AuthenticateUserTests {

        @Test
        @DisplayName("authenticateUser - Valid Credentials - Returns Token")
        void authenticateUser_ValidCredentials_ReturnsToken() {
            UserDetails userDetails = new User(TEST_USERNAME, TEST_PASSWORD, Collections.emptyList());
            Authentication authentication = mock(Authentication.class);

            when(authentication.getPrincipal()).thenReturn(userDetails);
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(authentication);
            when(jwtUtil.generateToken(userDetails)).thenReturn(MOCK_JWT_TOKEN);

            ResponseEntity<AuthResponse> responseEntity = authController.authenticateUser(authRequest);

            assertNotNull(responseEntity);
            assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
            assertNotNull(responseEntity.getBody());
            assertEquals(MOCK_JWT_TOKEN, responseEntity.getBody().getToken());

            verify(authenticationManager).authenticate(authenticationTokenCaptor.capture());
            UsernamePasswordAuthenticationToken capturedToken = authenticationTokenCaptor.getValue();
            assertEquals(TEST_USERNAME, capturedToken.getName());
            assertEquals(TEST_PASSWORD, capturedToken.getCredentials());

            verify(jwtUtil).generateToken(userDetails);
        }

        @Test
        @DisplayName("authenticateUser - Invalid Credentials - Returns Unauthorized")
        void authenticateUser_InvalidCredentials_ReturnsUnauthorized() {
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenThrow(new BadCredentialsException("Invalid credentials"));

            ResponseEntity<AuthResponse> responseEntity = authController.authenticateUser(authRequest);

            assertNotNull(responseEntity);
            assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
            assertNull(responseEntity.getBody());

            verify(authenticationManager).authenticate(authenticationTokenCaptor.capture());
            UsernamePasswordAuthenticationToken capturedToken = authenticationTokenCaptor.getValue();
            assertEquals(TEST_USERNAME, capturedToken.getName());
            assertEquals(TEST_PASSWORD, capturedToken.getCredentials());

            verify(jwtUtil, never()).generateToken(any(UserDetails.class));
        }
    }

    @Nested
    @DisplayName("Register Tenant Tests")
    class RegisterTenantTests {

        @Test
        @DisplayName("registerTenant - Valid Data - Returns Created Tenant With Token")
        void registerTenant_ValidData_ReturnsCreatedTenantWithToken() {

            when(registrationService.registerTenant(any(RegisterTenantRequest.class)))
                    .thenReturn(registerResponse);

            ResponseEntity<RegisterTenantResponse> responseEntity =
                    authController.registerTenant(registerRequest);

            assertNotNull(responseEntity);
            assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
            assertNotNull(responseEntity.getBody());
            assertEquals(TEST_TENANT_ID, responseEntity.getBody().getTenantId());
            assertEquals(TEST_TENANT_NAME, responseEntity.getBody().getTenantName());
            assertEquals(TEST_SUBDOMAIN, responseEntity.getBody().getSubdomain());
            assertEquals(MOCK_JWT_TOKEN, responseEntity.getBody().getAccessToken());
            assertEquals(TEST_LOGIN_URL, responseEntity.getBody().getLoginUrl());

            verify(registrationService).registerTenant(registerRequestCaptor.capture());
            RegisterTenantRequest capturedRequest = registerRequestCaptor.getValue();
            assertEquals(TEST_TENANT_NAME, capturedRequest.getTenantName());
            assertEquals(TEST_SUBDOMAIN, capturedRequest.getSubdomain());
            assertEquals(TEST_USERNAME, capturedRequest.getUsername());
            assertEquals(TEST_PASSWORD, capturedRequest.getPassword());
            assertEquals(TEST_EMAIL, capturedRequest.getEmail());
            assertEquals(TEST_FIRST_NAME, capturedRequest.getFirstName());
            assertEquals(TEST_LAST_NAME, capturedRequest.getLastName());
        }

        @Test
        @DisplayName("registerTenant - Subdomain Already Taken - Returns Bad Request")
        void registerTenant_SubdomainAlreadyTaken_ReturnsBadRequest() {
            when(registrationService.registerTenant(any(RegisterTenantRequest.class)))
                    .thenThrow(new BadRequestException("Subdomain '" + TEST_SUBDOMAIN + "' is already taken"));
            Assertions.assertThrows(
                    BadRequestException.class,
                    () -> authController.registerTenant(registerRequest)
            );

            verify(registrationService).registerTenant(any(RegisterTenantRequest.class));
        }
    }

    @Nested
    @DisplayName("Check Subdomain Availability Tests")
    class CheckSubdomainAvailabilityTests {

        @Test
        @DisplayName("checkSubdomainAvailability - Available Subdomain - Returns True")
        void checkSubdomainAvailability_AvailableSubdomain_ReturnsTrue() {
            when(registrationService.isSubdomainAvailable(TEST_SUBDOMAIN)).thenReturn(true);

            ResponseEntity<Map<String, Boolean>> responseEntity =
                    authController.checkSubdomainAvailability(TEST_SUBDOMAIN);

            assertNotNull(responseEntity);
            assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
            assertNotNull(responseEntity.getBody());
            assertTrue(responseEntity.getBody().get("available"));

            verify(registrationService).isSubdomainAvailable(eq(TEST_SUBDOMAIN));
        }

        @Test
        @DisplayName("checkSubdomainAvailability - Unavailable Subdomain - Returns False")
        void checkSubdomainAvailability_UnavailableSubdomain_ReturnsFalse() {
            when(registrationService.isSubdomainAvailable(TEST_SUBDOMAIN)).thenReturn(false);

            ResponseEntity<Map<String, Boolean>> responseEntity =
                    authController.checkSubdomainAvailability(TEST_SUBDOMAIN);

            assertNotNull(responseEntity);
            assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
            assertNotNull(responseEntity.getBody());
            assertFalse(responseEntity.getBody().get("available"));

            verify(registrationService).isSubdomainAvailable(eq(TEST_SUBDOMAIN));
        }
    }
}

