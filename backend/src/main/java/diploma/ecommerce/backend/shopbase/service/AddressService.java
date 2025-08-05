package diploma.ecommerce.backend.shopbase.service;

import java.util.List;

import diploma.ecommerce.backend.shopbase.model.Address;

public interface AddressService {

    List<Address> getAllAddresses(Long tenantId);

    Address getAddress(Long tenantId, Long id);

    Address createAddress(Long tenantId, Address address);

    Address updateAddress(Long tenantId, Long id, Address addressDetails);

    void deleteAddress(Long tenantId, Long id);
}
