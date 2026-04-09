package com.retailstore.testdata;

import com.retailstore.user.entity.Address;
import com.retailstore.user.enums.AddressType;
import com.retailstore.user.repository.AddressRepository;
import org.springframework.beans.factory.annotation.Autowired;

public class AddressTestData {

    @Autowired
    private AddressRepository addressRepository;

    public Address createAddress(Long userId, AddressType addressType){
        Address address = new Address();
        address.setStreet("Street " + System.nanoTime());
        address.setCity("Hyderabad");
        address.setState("TS");
        address.setPincode("Pin-" + System.nanoTime());
        address.setAddressType(addressType);
        address.setUserId(userId);

        return addressRepository.save(address);
    }
}
