package com.retailstore.user.mapper;

import com.retailstore.user.dto.AddressResponseDTO;
import com.retailstore.user.entity.Address;

/**
 * Utility class for mapping {@link Address} entities to
 * {@link AddressResponseDTO} objects used in API responses.
 *
 * <p>Provides static methods to convert Address entities into DTOs,
 * ensuring only necessary address details are exposed to clients.</p>
 */
public class AddressMapper {

    private AddressMapper(){}


    public static AddressResponseDTO mapToAddressResponseDTO(Address address) {

        return AddressResponseDTO.builder()
                .id(address.getId())
                .street(address.getStreet())
                .city(address.getCity())
                .state(address.getState())
                .pincode(address.getPincode())
                .type(address.getAddressType())
                .build();
    }
}
