package com.retailstore.user.controller;

import com.retailstore.user.dto.AddressRequestDTO;
import com.retailstore.user.dto.AddressResponseDTO;
import com.retailstore.user.service.AddressService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller responsible for managing user addresses.
 * Provides endpoints for creating, retrieving, updating,
 * and deleting address records associated with users.
 */
@RestController
@RequestMapping("/users")
public class AddressController {

    private final AddressService addressService;

    @Autowired
    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    /**
     * Adds a new address for a specific user.
     *
     * @param userId the ID of the user to whom the address belongs
     * @param addressRequestDTO DTO containing address details
     * @return {@link AddressResponseDTO} containing the created address information
     */
    @PreAuthorize("hasRole('ADMIN') or @addressSecurity.isOwnerByUserId(#userId, authentication)")
    @PostMapping("/{userId}/addresses")
    public ResponseEntity<AddressResponseDTO> addAddress(@PathVariable Long userId,
                                                         @Valid @RequestBody AddressRequestDTO addressRequestDTO){
        return ResponseEntity.status(HttpStatus.CREATED).body(addressService.addAddress(userId, addressRequestDTO));
    }

    /**
     * Retrieves all addresses associated with a specific user.
     *
     * @param userId the ID of the user
     * @return list of {@link AddressResponseDTO} representing the user's addresses
     */
    @PreAuthorize("hasRole('ADMIN') or @addressSecurity.isOwnerByUserId(#userId, authentication)")
    @GetMapping("/{userId}/addresses")
    public ResponseEntity<List<AddressResponseDTO>> getAllAddressesByUser(@PathVariable Long userId){

        return ResponseEntity.ok(addressService.getAllAddressesByUser(userId));
    }

    /**
     * Retrieves a specific address using its unique ID.
     *
     * @param addressId the ID of the address
     * @return {@link AddressResponseDTO} containing the address details
     */
    @PreAuthorize("hasRole('ADMIN') or @addressSecurity.isOwnerByAddressId(#addressId, authentication)")
    @GetMapping("/addresses/{addressId}")
    public ResponseEntity<AddressResponseDTO> getAddressById(@PathVariable Long addressId){

        return ResponseEntity.ok(addressService.getAddressById(addressId));
    }

    /**
     * Updates an existing address.
     *
     * @param addressId the ID of the address to update
     * @param addressRequestDTO DTO containing updated address details
     * @return {@link AddressResponseDTO} containing the updated address information
     */
    @PreAuthorize("hasRole('ADMIN') or @addressSecurity.isOwnerByAddressId(#addressId, authentication)")
    @PutMapping("/addresses/{addressId}")
    public ResponseEntity<AddressResponseDTO> updateAddress(@PathVariable Long addressId,
                                                            @Valid @RequestBody AddressRequestDTO addressRequestDTO){
        return ResponseEntity.ok(addressService.updateAddress(addressId, addressRequestDTO));
    }

    /**
     * Deletes an address using its ID.
     *
     * @param addressId the ID of the address to delete
     * @return {@link AddressResponseDTO} containing the deleted address information
     */
    @PreAuthorize("hasRole('ADMIN') or @addressSecurity.isOwnerByAddressId(#addressId, authentication)")
    @DeleteMapping("/addresses/{addressId}")
    public ResponseEntity<AddressResponseDTO> deleteAddress(@PathVariable Long addressId){

        return ResponseEntity.ok(addressService.deleteAddress(addressId));
    }
}
