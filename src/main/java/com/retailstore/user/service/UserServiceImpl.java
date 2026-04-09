package com.retailstore.user.service;

import com.retailstore.exception.ResourceConflictException;
import com.retailstore.exception.ResourceNotFoundException;
import com.retailstore.order.repository.OrderRepository;
import com.retailstore.user.dto.*;
import com.retailstore.user.entity.Address;
import com.retailstore.user.entity.User;
import com.retailstore.user.enums.UserRole;
import com.retailstore.user.mapper.UserMapper;
import com.retailstore.user.repository.AddressRepository;
import com.retailstore.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service implementation responsible for handling all business operations
 * related to {@link User} management.
 *
 * <p>This service provides functionality for:</p>
 * <ul>
 *     <li>User registration and authentication</li>
 *     <li>Retrieving user details</li>
 *     <li>Updating user profile and role</li>
 *     <li>Deleting users and their associated addresses</li>
 * </ul>
 *
 * <p>During user deletion, the service ensures that the user does not have
 * any active orders before removing the user and associated addresses.</p>
 */
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final AddressRepository addressRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, OrderRepository orderRepository,
                           AddressRepository addressRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.addressRepository = addressRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Registers a new user in the system.
     *
     * <p>This method performs the following validations:</p>
     * <ul>
     *     <li>Ensures the email is not already registered.</li>
     *     <li>Validates that the provided role is a valid {@link UserRole}.</li>
     * </ul>
     *
     * @param userRegisterRequestDTO DTO containing user registration details.
     * @return {@link UserResponseDTO} containing the created user's details.
     * @throws ResourceConflictException if a user already exists with the same email.
     * @throws IllegalArgumentException if the provided role is invalid.
     */
    @Override
    @Transactional
    public UserResponseDTO registerUser(UserRegisterRequestDTO userRegisterRequestDTO) {

        if(userRepository.existsByEmail(userRegisterRequestDTO.getEmail())){
            throw new ResourceConflictException(
                    "User already exists with email id: " + userRegisterRequestDTO.getEmail());
        }

        User user = new User();
        user.setName(userRegisterRequestDTO.getName());
        user.setEmail(userRegisterRequestDTO.getEmail());
        user.setPassword(passwordEncoder.encode(userRegisterRequestDTO.getPassword()));
        user.setPhone(userRegisterRequestDTO.getPhone());
        user.setRole(userRegisterRequestDTO.getRole());
        user.setFailedAttempts(0);
        user.setAccountLocked(false);
        userRepository.save(user);

        return UserMapper.mapToUserResponseDTO(user);
    }

    /**
     * Retrieves a user by their unique ID.
     *
     * @param userId the ID of the user.
     * @return {@link UserResponseDTO} containing the user details.
     * @throws ResourceNotFoundException if the user does not exist.
     */
    @Override
    public UserResponseDTO getUserById(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User does not exists with id: " + userId ));

        return UserMapper.mapToUserResponseDTO(user);
    }

    /**
     * Retrieves all users available in the system.
     *
     * @return a {@link List} of {@link UserResponseDTO} representing all users.
     */
    @Override
    public List<UserResponseDTO> getAllUsers() {

        List<User> users = userRepository.findAll();

        return users.stream()
                .map(UserMapper::mapToUserResponseDTO)
                .toList();
    }

    /**
     * Updates an existing user's profile details.
     *
     * <p>This method allows updating the user's name, password,
     * phone number, and role while ensuring that the email
     * remains unchanged.</p>
     *
     * @param userId               the ID of the user to update.
     * @param updateUserRequestDTO DTO containing updated user details.
     * @return {@link UserResponseDTO} containing the updated user information.
     * @throws ResourceNotFoundException if the user does not exist.
     * @throws IllegalArgumentException  if the email is changed or role is invalid.
     */
    @Override
    @Transactional
    public UserResponseDTO updateUser(Long userId, UpdateUserRequestDTO updateUserRequestDTO) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User does not exists with id: " + userId ));

        if (!user.getEmail().equals(updateUserRequestDTO.getEmail())){
            throw new IllegalArgumentException("Email cannot be changed");
        }

        user.setName(updateUserRequestDTO.getName());
        user.setPassword(passwordEncoder.encode(updateUserRequestDTO.getPassword()));
        user.setPhone(updateUserRequestDTO.getPhone());
        userRepository.save(user);

        return UserMapper.mapToUserResponseDTO(user);
    }

    /**
     * Deletes a user and all associated addresses.
     *
     * <p>This method first checks whether the user has active orders.
     * If active orders exist, the deletion is blocked.</p>
     *
     * <p>Upon successful validation:</p>
     * <ul>
     *     <li>All addresses associated with the user are deleted.</li>
     *     <li>The user record is removed from the system.</li>
     * </ul>
     *
     * @param userId the ID of the user to delete.
     * @return {@link DeletedUserAndAddressResponseDTO} containing
     *         the deleted user and their addresses.
     * @throws ResourceConflictException if the user has active orders.
     * @throws ResourceNotFoundException if the user does not exist.
     */
    @Override
    @Transactional
    public DeletedUserAndAddressResponseDTO deleteUser(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (orderRepository.existsByUserIdAndDeletedFalse(userId)){
            throw new ResourceConflictException("Cannot delete the user associated with active orders");
        }

        List<Address> addresses = addressRepository.findByUserId(userId);

        UserResponseDTO deletedUserResponseDTO = UserMapper.mapToUserResponseDTO(user);

        addressRepository.deleteAll(addresses);
        userRepository.delete(user);

        return new DeletedUserAndAddressResponseDTO(deletedUserResponseDTO, addresses);
    }

    /**
     * Updates the role assigned to a user.
     *
     * @param userId the ID of the user.
     * @param userRole the new role to assign.
     * @return {@link UserResponseDTO} containing the updated user information.
     * @throws ResourceNotFoundException if the user does not exist.
     * @throws IllegalArgumentException if the provided role is invalid.
     */
    @Override
    @Transactional
    public UserResponseDTO updateUserRole(Long userId, UserRole userRole) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        user.setRole(userRole);

        userRepository.save(user);

        return UserMapper.mapToUserResponseDTO(user);
    }


}
