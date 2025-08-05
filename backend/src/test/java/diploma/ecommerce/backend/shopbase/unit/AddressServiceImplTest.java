package diploma.ecommerce.backend.shopbase.unit;

import java.util.List;
import java.util.Optional;

import diploma.ecommerce.backend.shopbase.exception.ResourceNotFoundException;
import diploma.ecommerce.backend.shopbase.model.Address;
import diploma.ecommerce.backend.shopbase.model.Tenant;
import diploma.ecommerce.backend.shopbase.repository.AddressRepository;
import diploma.ecommerce.backend.shopbase.repository.TenantRepository;
import diploma.ecommerce.backend.shopbase.service.impl.AddressServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AddressServiceImplTest {

    private static final Long TENANT_ID = 1L;
    private static final Long ADDRESS_ID = 10L;
    @Mock
    private AddressRepository addressRepository;
    @Mock
    private TenantRepository tenantRepository;
    @InjectMocks
    private AddressServiceImpl addressService;
    private Tenant tenant;
    private Address address;
    private Address addressDetails;

    @BeforeEach
    void setUp() {
        tenant = new Tenant();
        tenant.setId(TENANT_ID);
        tenant.setName("Test Tenant");

        address = new Address();
        address.setId(ADDRESS_ID);
        address.setTenant(tenant);
        address.setCountry("Country A");
        address.setCity("City A");
        address.setStreet("Street A");
        address.setHouseNumber("1");

        addressDetails = new Address();
        addressDetails.setCountry("Country B");
        addressDetails.setCity("City B");
        addressDetails.setStreet("Street B");
        addressDetails.setHouseNumber("2");
        addressDetails.setPostalCode("12345");
    }

    @Nested
    @DisplayName("getAllAddresses Tests")
    class GetAllAddressesTests {

        @Test
        @DisplayName("Should return list of addresses when tenant exists")
        void getAllAddresses_TenantExists_ReturnsAddressList() {
            when(tenantRepository.existsById(TENANT_ID)).thenReturn(true);
            when(addressRepository.findAllByTenantId(TENANT_ID)).thenReturn(List.of(address));

            List<Address> result = addressService.getAllAddresses(TENANT_ID);

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(address.getId(), result.getFirst().getId());
            verify(tenantRepository).existsById(TENANT_ID);
            verify(addressRepository).findAllByTenantId(TENANT_ID);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when tenant does not exist")
        void getAllAddresses_TenantDoesNotExist_ThrowsResourceNotFoundException() {
            when(tenantRepository.existsById(TENANT_ID)).thenReturn(false);

            assertThrows(ResourceNotFoundException.class, () -> addressService.getAllAddresses(TENANT_ID));
            verify(tenantRepository).existsById(TENANT_ID);
            verify(addressRepository, never()).findAllByTenantId(anyLong());
        }
    }

    @Nested
    @DisplayName("getAddress Tests")
    class GetAddressTests {

        @Test
        @DisplayName("Should return address when found")
        void getAddress_AddressFound_ReturnsAddress() {
            when(addressRepository.findByTenantIdAndId(TENANT_ID, ADDRESS_ID)).thenReturn(Optional.of(address));

            Address result = addressService.getAddress(TENANT_ID, ADDRESS_ID);

            assertNotNull(result);
            assertEquals(ADDRESS_ID, result.getId());
            assertEquals(TENANT_ID, result.getTenant().getId());
            verify(addressRepository).findByTenantIdAndId(TENANT_ID, ADDRESS_ID);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when address not found")
        void getAddress_AddressNotFound_ThrowsResourceNotFoundException() {
            when(addressRepository.findByTenantIdAndId(TENANT_ID, ADDRESS_ID)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> addressService.getAddress(TENANT_ID, ADDRESS_ID));
            verify(addressRepository).findByTenantIdAndId(TENANT_ID, ADDRESS_ID);
        }
    }

    @Nested
    @DisplayName("createAddress Tests")
    class CreateAddressTests {
        @Test
        @DisplayName("Should create and return address when tenant exists")
        void createAddress_TenantExists_ReturnsCreatedAddress() {
            addressDetails.setId(null);
            when(tenantRepository.findById(TENANT_ID)).thenReturn(Optional.of(tenant));
            when(addressRepository.save(any(Address.class))).thenAnswer(invocation -> {
                Address saved = invocation.getArgument(0);
                saved.setId(ADDRESS_ID + 1);
                return saved;
            });

            ArgumentCaptor<Address> addressCaptor = ArgumentCaptor.forClass(Address.class);

            Address result = addressService.createAddress(TENANT_ID, addressDetails);

            assertNotNull(result);
            assertNotNull(result.getId());
            assertEquals("Country B", result.getCountry());
            assertNotNull(result.getTenant());
            assertEquals(TENANT_ID, result.getTenant().getId());

            verify(tenantRepository).findById(TENANT_ID);
            verify(addressRepository).save(addressCaptor.capture());
            Address savedAddress = addressCaptor.getValue();
            assertEquals(tenant, savedAddress.getTenant());
            assertEquals("Country B", savedAddress.getCountry());
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when tenant does not exist")
        void createAddress_TenantDoesNotExist_ThrowsResourceNotFoundException() {
            when(tenantRepository.findById(TENANT_ID)).thenReturn(Optional.empty());

            assertThrows(
                    ResourceNotFoundException.class,
                    () -> addressService.createAddress(TENANT_ID, addressDetails)
            );
            verify(tenantRepository).findById(TENANT_ID);
            verify(addressRepository, never()).save(any(Address.class));
        }
    }


    @Nested
    @DisplayName("updateAddress Tests")
    class UpdateAddressTests {

        @Test
        @DisplayName("Should update and return address when found")
        void updateAddress_AddressFound_ReturnsUpdatedAddress() {
            when(addressRepository.findByTenantIdAndId(TENANT_ID, ADDRESS_ID)).thenReturn(Optional.of(address));
            when(addressRepository.save(any(Address.class))).thenAnswer(invocation -> invocation.getArgument(0));
            ArgumentCaptor<Address> addressCaptor = ArgumentCaptor.forClass(Address.class);

            Address result = addressService.updateAddress(TENANT_ID, ADDRESS_ID, addressDetails);

            assertNotNull(result);
            assertEquals(ADDRESS_ID, result.getId());
            assertEquals(TENANT_ID, result.getTenant().getId());
            assertEquals("Country B", result.getCountry());
            assertEquals("City B", result.getCity());
            assertEquals("Street B", result.getStreet());
            assertEquals("2", result.getHouseNumber());
            assertEquals("12345", result.getPostalCode());

            verify(addressRepository).findByTenantIdAndId(TENANT_ID, ADDRESS_ID);
            verify(addressRepository).save(addressCaptor.capture());
            Address savedAddress = addressCaptor.getValue();
            assertEquals(ADDRESS_ID, savedAddress.getId());
            assertEquals("City B", savedAddress.getCity());
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when address not found")
        void updateAddress_AddressNotFound_ThrowsResourceNotFoundException() {
            when(addressRepository.findByTenantIdAndId(TENANT_ID, ADDRESS_ID)).thenReturn(Optional.empty());

            assertThrows(
                    ResourceNotFoundException.class,
                    () -> addressService.updateAddress(TENANT_ID, ADDRESS_ID, addressDetails)
            );
            verify(addressRepository).findByTenantIdAndId(TENANT_ID, ADDRESS_ID);
            verify(addressRepository, never()).save(any(Address.class));
        }
    }

    @Nested
    @DisplayName("deleteAddress Tests")
    class DeleteAddressTests {

        @Test
        @DisplayName("Should delete address when found")
        void deleteAddress_AddressFound_DeletesAddress() {
            when(addressRepository.findByTenantIdAndId(TENANT_ID, ADDRESS_ID)).thenReturn(Optional.of(address));
            doNothing().when(addressRepository).delete(any(Address.class));
            ArgumentCaptor<Address> addressCaptor = ArgumentCaptor.forClass(Address.class);

            addressService.deleteAddress(TENANT_ID, ADDRESS_ID);

            verify(addressRepository).findByTenantIdAndId(TENANT_ID, ADDRESS_ID);
            verify(addressRepository).delete(addressCaptor.capture());
            assertEquals(address, addressCaptor.getValue());
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when address not found")
        void deleteAddress_AddressNotFound_ThrowsResourceNotFoundException() {
            when(addressRepository.findByTenantIdAndId(TENANT_ID, ADDRESS_ID)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> addressService.deleteAddress(TENANT_ID, ADDRESS_ID));

            verify(addressRepository).findByTenantIdAndId(TENANT_ID, ADDRESS_ID);
            verify(addressRepository, never()).delete(any(Address.class));
        }
    }
}
