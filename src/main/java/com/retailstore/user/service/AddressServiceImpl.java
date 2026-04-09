package com.retailstore.user.service;

import com.retailstore.exception.ResourceNotFoundException;
import com.retailstore.user.dto.AddressRequestDTO;
import com.retailstore.user.dto.AddressResponseDTO;
import com.retailstore.user.entity.Address;
import com.retailstore.user.enums.AddressType;
import com.retailstore.user.mapper.AddressMapper;
import com.retailstore.user.repository.AddressRepository;
import com.retailstore.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service implementation responsible for handling business operations
 * related to {@link Address} management for users.
 *
 * <p>This service provides functionality for:</p>
 * <ul>
 *     <li>Adding a new address for a user</li>
 *     <li>Retrieving all addresses associated with a user</li>
 *     <li>Fetching address details by ID</li>
 *     <li>Updating an existing address</li>
 *     <li>Deleting an address</li>
 * </ul>
 *
 * <p>All operations validate the existence of the associated user or address
 * and ensure the provided {@link AddressType} is valid before persisting changes.</p>
 */
@Service
public class AddressServiceImpl implements AddressService{

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    @Autowired
    public AddressServiceImpl(AddressRepository addressRepository, UserRepository userRepository) {
        this.addressRepository = addressRepository;
        this.userRepository = userRepository;
    }


    /**
     * Adds a new address for the specified user.
     *
     * <p>This method validates that the user exists and verifies that the
     * provided {@link AddressType} is valid before creating and saving
     * the address entity.</p>
     *
     * @param userId the ID of the user to whom the address belongs
     * @param addressRequestDTO DTO containing address details
     * @return {@link AddressResponseDTO} containing the saved address information
     * @throws ResourceNotFoundException if the user does not exist
     * @throws IllegalArgumentException if the provided address type is invalid
     */
    @Override
    @Transactional
    public AddressResponseDTO addAddress(Long userId, AddressRequestDTO addressRequestDTO) {

        validateUser(userId);

        Address address = new Address();
        address.setUserId(userId);
        address.setStreet(addressRequestDTO.getStreet());
        address.setCity(addressRequestDTO.getCity());
        address.setState(addressRequestDTO.getState());
        address.setPincode(addressRequestDTO.getPincode());
        address.setAddressType(addressRequestDTO.getType());

        Address savedAddress = addressRepository.save(address);

        return AddressMapper.mapToAddressResponseDTO(savedAddress);
    }

    /**
     * Retrieves all addresses associated with a specific user.
     *
     * <p>This method first validates that the user exists, then fetches
     * all addresses linked to the given user ID and maps them to DTOs.</p>
     *
     * @param userId the ID of the user whose addresses should be retrieved
     * @return list of {@link AddressResponseDTO} containing address details
     * @throws ResourceNotFoundException if the user does not exist
     */
    @Override
    public List<AddressResponseDTO> getAllAddressesByUser(Long userId) {

        validateUser(userId);

        return addressRepository.findByUserId(userId)
                .stream()
                .map(AddressMapper::mapToAddressResponseDTO)
                .toList();
    }

    /**
     * Retrieves a specific address by its ID.
     *
     * @param addressId the ID of the address to retrieve
     * @return {@link AddressResponseDTO} containing the address details
     * @throws ResourceNotFoundException if the address does not exist
     */
    @Override
    public AddressResponseDTO getAddressById(Long addressId) {

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with id: " + addressId));

        return AddressMapper.mapToAddressResponseDTO(address);
    }

    /**
     * Updates an existing address.
     *
     * <p>This method validates that the address exists and ensures the
     * provided {@link AddressType} is valid before applying updates.</p>
     *
     * @param addressId the ID of the address to update
     * @param addressRequestDTO DTO containing updated address details
     * @return {@link AddressResponseDTO} containing the updated address information
     * @throws ResourceNotFoundException if the address does not exist
     * @throws IllegalArgumentException if the provided address type is invalid
     */
    @Override
    @Transactional
    public AddressResponseDTO updateAddress(Long addressId, AddressRequestDTO addressRequestDTO) {

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with id: " + addressId));

        address.setStreet(addressRequestDTO.getStreet());
        address.setCity(addressRequestDTO.getCity());
        address.setState(addressRequestDTO.getState());
        address.setPincode(addressRequestDTO.getPincode());
        address.setAddressType(addressRequestDTO.getType());

        Address updatedAddress = addressRepository.save(address);

        return AddressMapper.mapToAddressResponseDTO(updatedAddress);
    }

    /**
     * Deletes an address by its ID.
     *
     * <p>The address is first retrieved and mapped to a response DTO
     * before being removed from the database.</p>
     *
     * @param addressId the ID of the address to delete
     * @return {@link AddressResponseDTO} containing the deleted address details
     * @throws ResourceNotFoundException if the address does not exist
     */
    @Override
    @Transactional
    public AddressResponseDTO deleteAddress(Long addressId) {

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with id: " + addressId));

        AddressResponseDTO addressResponseDTO = AddressMapper.mapToAddressResponseDTO(address);

        addressRepository.delete(address);

        return addressResponseDTO;
    }

    /**
     * Validates that a user exists.
     */
    private void validateUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
    }
}
