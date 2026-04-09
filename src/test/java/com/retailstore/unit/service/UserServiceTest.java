package com.retailstore.unit.service;

import com.retailstore.exception.ResourceConflictException;
import com.retailstore.exception.ResourceNotFoundException;
import com.retailstore.order.repository.OrderRepository;
import com.retailstore.user.dto.*;
import com.retailstore.user.entity.Address;
import com.retailstore.user.entity.User;
import com.retailstore.user.enums.AddressType;
import com.retailstore.user.enums.UserRole;
import com.retailstore.user.repository.AddressRepository;
import com.retailstore.user.repository.UserRepository;
import com.retailstore.user.service.UserServiceImpl;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock private AddressRepository addressRepository;
    @Mock private UserRepository userRepository;
    @Mock private OrderRepository orderRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    //================<< BUILDERS >>================
    private UserRegisterRequestDTO buildUserRegisterRequest(){
        UserRegisterRequestDTO registerRequest = new UserRegisterRequestDTO();
        registerRequest.setName("Test User");
        registerRequest.setEmail("test@gmail.com");
        registerRequest.setPassword("encoded");
        registerRequest.setRole(UserRole.ROLE_CUSTOMER);
        registerRequest.setPhone("9876543210");

        return registerRequest;
    }

    private UpdateUserRequestDTO buildUpdateUserRequest(){
        UpdateUserRequestDTO updateRequest = new UpdateUserRequestDTO();
        updateRequest.setName("Test User");
        updateRequest.setEmail("test@gmail.com");
        updateRequest.setPassword("encoded");
        updateRequest.setPhone("9876543210");

        return updateRequest;
    }

    private User buildUser(Long id, UserRole userRole){
        User user = new User();
        user.setId(id);
        user.setName("Test User");
        user.setEmail("test@gmail.com");
        user.setPassword("encoded");
        user.setPhone("9876543210");
        user.setRole(userRole);

        return user;
    }

    private Address buildAddress(Long id, AddressType addressType){
        Address address = new Address();
        address.setId(id);
        address.setUserId(9L);
        address.setStreet("Church Street");
        address.setCity("Bangalore");
        address.setState("Karnataka");
        address.setPincode("4579616");
        address.setAddressType(addressType);

        return address;
    }

    //================<< REGISTER USER >>================
    @Nested
    class RegisterUserTests{

        @Test
        void shouldRegisterUserSuccessfully(){
            when(userRepository.existsByEmail("test@gmail.com")).thenReturn(false);
            when(passwordEncoder.encode("encoded")).thenReturn("encodedPass");
            when(userRepository.save(any()))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            UserResponseDTO response = userService.registerUser(buildUserRegisterRequest());

            assertNotNull(response);

            verify(userRepository).save(any());
        }

        @Test
        void shouldThrowException_whenUserAlreadyExists(){
            when(userRepository.existsByEmail("test@gmail.com")).thenReturn(true);

            assertThrows(ResourceConflictException.class,
                    () -> userService.registerUser(buildUserRegisterRequest()));
        }
    }

    //================<< GET USER BY ID >>================
    @Nested
    class GetUserByIdTests{

        @Test
        void getUserByIdSuccessfully(){
            when(userRepository.findById(2L)).thenReturn(Optional.of(buildUser(2L, UserRole.ROLE_ADMIN)));

            UserResponseDTO response = userService.getUserById(2L);

            assertNotNull(response);
        }

        @Test
        void shouldThrowException_whenUserNotFound(){
            when(userRepository.findById(3L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> userService.getUserById(3L));
        }
    }

    //================<< GET ALL USERS >>================
    @Nested
    class GetAllUsersTests{

        @Test
        void shouldGetAllUsersSuccessfully(){
            when(userRepository.findAll()).thenReturn(List.of(buildUser(5L, UserRole.ROLE_CUSTOMER)));

            List<UserResponseDTO> responses = userService.getAllUsers();

            assertFalse(responses.isEmpty());
            assertEquals(1, responses.size());
        }

        @Test
        void shouldGetEmptyList_whenNoUsersFound(){
            when(userRepository.findAll()).thenReturn(List.of());

            List<UserResponseDTO> responses = userService.getAllUsers();

            assertTrue(responses.isEmpty());
        }
    }

    //================<< UPDATE USER >>================
    @Nested
    class UpdateUserTests{

        @Test
        void shouldUpdateUserSuccessfully(){
            when(userRepository.findById(4L)).thenReturn(Optional.of(buildUser(4L, UserRole.ROLE_CUSTOMER)));
            when(passwordEncoder.encode("encoded")).thenReturn("encodedNew");
            when(userRepository.save(any()))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            UserResponseDTO response = userService.updateUser(4L, buildUpdateUserRequest());

            assertNotNull(response);
            assertEquals("test@gmail.com", response.getEmail());

            verify(userRepository).save(any());
        }

        @Test
        void shouldThrowException_whenUserNotFound(){
            when(userRepository.findById(7L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> userService.updateUser(7L, buildUpdateUserRequest()));
        }

        @Test
        void shouldThrowException_whenEmailAlreadyExists(){
            User user = buildUser(8L, UserRole.ROLE_ADMIN);
            user.setEmail("old@gmail.com");

            when(userRepository.findById(8L)).thenReturn(Optional.of(user));

            UpdateUserRequestDTO request = buildUpdateUserRequest();
            request.setEmail("new@gmail.com");

            assertThrows(IllegalArgumentException.class,
                    () -> userService.updateUser(8L, buildUpdateUserRequest()));
        }
    }

    //================<< DELETE USER >>================
    @Nested
    class DeleteUserTests{

        @Test
        void shouldDeleteUserSuccessfully(){
            when(userRepository.findById(9L)).thenReturn(Optional.of(buildUser(9L, UserRole.ROLE_CUSTOMER)));
            when(orderRepository.existsByUserIdAndDeletedFalse(9L)).thenReturn(false);
            when(addressRepository.findByUserId(9L)).thenReturn(List.of());

            DeletedUserAndAddressResponseDTO response = userService.deleteUser(9L);

            assertNotNull(response);
            assertTrue(response.getAddresses().isEmpty());

            verify(userRepository).delete(any());
        }

        @Test
        void shouldDeleteUserAndAddresses_whenAddressExist(){
            when(userRepository.findById(9L)).thenReturn(Optional.of(buildUser(9L, UserRole.ROLE_CUSTOMER)));
            when(orderRepository.existsByUserIdAndDeletedFalse(9L)).thenReturn(false);
            when(addressRepository.findByUserId(9L))
                    .thenReturn(List.of(buildAddress(1L, AddressType.OFFICE),
                            buildAddress(2L, AddressType.HOME)));

            DeletedUserAndAddressResponseDTO response = userService.deleteUser(9L);

            assertNotNull(response);
            assertEquals(2, response.getAddresses().size());

            verify(userRepository).delete(any());
            verify(addressRepository).deleteAll(any());
        }

        @Test
        void shouldThrowException_whenUserNotFound(){
            when(userRepository.findById(10L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> userService.deleteUser(10L));
        }

        @Test
        void shouldThrowException_whenActiveOrdersExist(){
            when(userRepository.findById(10L)).thenReturn(Optional.of(buildUser(10L, UserRole.ROLE_ADMIN)));
            when(orderRepository.existsByUserIdAndDeletedFalse(10L)).thenReturn(true);

            assertThrows(ResourceConflictException.class,
                    () -> userService.deleteUser(10L));
        }
    }

    //================<< UPDATE USER ROLE >>================
    @Nested
    class UpdateUserRoleTests{

        @Test
        void shouldUpdateUserRoleSuccessfully(){
            when(userRepository.findById(11L))
                    .thenReturn(Optional.of(buildUser(11L, UserRole.ROLE_CUSTOMER)));
            when(userRepository.save(any()))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            UserResponseDTO response = userService.updateUserRole(11L, UserRole.ROLE_ADMIN);

            assertNotNull(response);

            verify(userRepository).save(argThat(u -> u.getRole() == UserRole.ROLE_ADMIN));
        }

        @Test
        void shouldThrowException_whenUserNotFound(){
            when(userRepository.findById(12L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> userService.updateUserRole(12L, UserRole.ROLE_ADMIN));
        }
    }
}
