package com.retailstore.security;

import com.retailstore.security.userdetails.CustomUserDetails;
import com.retailstore.user.repository.AddressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class AddressSecurity {

    private final AddressRepository addressRepository;

    @Autowired
    public AddressSecurity(AddressRepository addressRepository) {
        this.addressRepository = addressRepository;
    }

    public boolean isOwnerByAddressId(Long addressId, Authentication authentication){

        if (!(authentication.getPrincipal() instanceof CustomUserDetails userDetails)){
            return false;
        }

        return addressRepository.existsByIdAndUserId(addressId, userDetails.getId());
    }

    public boolean isOwnerByUserId(Long userId, Authentication authentication){

        if (!(authentication.getPrincipal() instanceof CustomUserDetails userDetails)){
            return false;
        }

        return userDetails.getId().equals(userId);
    }
}
