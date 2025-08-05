package diploma.ecommerce.backend.shopbase.service.impl;

import java.util.List;

import diploma.ecommerce.backend.shopbase.exception.ResourceNotFoundException;
import diploma.ecommerce.backend.shopbase.model.Address;
import diploma.ecommerce.backend.shopbase.model.Tenant;
import diploma.ecommerce.backend.shopbase.repository.AddressRepository;
import diploma.ecommerce.backend.shopbase.repository.TenantRepository;
import diploma.ecommerce.backend.shopbase.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;
    private final TenantRepository tenantRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Address> getAllAddresses(Long tenantId) {
        if (!tenantRepository.existsById(tenantId)) {
            throw new ResourceNotFoundException("Tenant", "id", tenantId);
        }
        return addressRepository.findAllByTenantId(tenantId);
    }

    @Override
    @Transactional(readOnly = true)
    public Address getAddress(Long tenantId, Long id) {
        return addressRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Address",
                        "id",
                        id,
                        tenantId
                ));
    }

    @Override
    @Transactional
    public Address createAddress(Long tenantId, Address address) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant", "id", tenantId));

        address.setTenant(tenant);
        address.setId(null);

        return addressRepository.save(address);
    }

    @Override
    @Transactional
    public Address updateAddress(Long tenantId, Long id, Address addressDetails) {
        Address existingAddress = addressRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", id, tenantId));

        if (addressDetails.getCountry() != null) {
            existingAddress.setCountry(addressDetails.getCountry());
        }
        if (addressDetails.getCity() != null) {
            existingAddress.setCity(addressDetails.getCity());
        }
        if (addressDetails.getStreet() != null) {
            existingAddress.setStreet(addressDetails.getStreet());
        }
        if (addressDetails.getHouseNumber() != null) {
            existingAddress.setHouseNumber(addressDetails.getHouseNumber());
        }
        existingAddress.setApartment(addressDetails.getApartment());
        existingAddress.setPostalCode(addressDetails.getPostalCode());
        existingAddress.setComment(addressDetails.getComment());

        return addressRepository.save(existingAddress);
    }

    @Override
    @Transactional
    public void deleteAddress(Long tenantId, Long id) {
        Address addressToDelete = addressRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Address",
                                "id",
                                id,
                                tenantId
                        )
                );
        addressRepository.delete(addressToDelete);
    }
}
