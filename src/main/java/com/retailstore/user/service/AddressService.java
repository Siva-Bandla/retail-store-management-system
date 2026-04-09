package com.retailstore.user.service;

import com.retailstore.user.dto.AddressRequestDTO;
import com.retailstore.user.dto.AddressResponseDTO;

import java.util.List;

public interface AddressService {

    AddressResponseDTO addAddress(Long userId, AddressRequestDTO addressRequestDTO);
    List<AddressResponseDTO> getAllAddressesByUser(Long userId);
    AddressResponseDTO getAddressById(Long addressId);
    AddressResponseDTO updateAddress(Long addressId, AddressRequestDTO addressRequestDTO);
    AddressResponseDTO deleteAddress(Long addressId);
}
