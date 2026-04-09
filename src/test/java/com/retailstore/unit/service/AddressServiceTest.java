package com.retailstore.unit.service;

import com.retailstore.exception.ResourceNotFoundException;
import com.retailstore.user.dto.AddressRequestDTO;
import com.retailstore.user.dto.AddressResponseDTO;
import com.retailstore.user.entity.Address;
import com.retailstore.user.enums.AddressType;
import com.retailstore.user.repository.AddressRepository;
import com.retailstore.user.repository.UserRepository;
import com.retailstore.user.service.AddressServiceImpl;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AddressServiceTest {

    @Mock private AddressRepository addressRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private AddressServiceImpl addressService;

    //================<< BUILDERS >>================
    private AddressRequestDTO buildAddressRequest(AddressType addressType){
        AddressRequestDTO request = new AddressRequestDTO();
        request.setStreet("Church Street");
        request.setCity("Bangalore");
        request.setState("Karnataka");
        request.setPincode("4579616");
        request.setType(addressType);

        return request;
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

    //================<< ADD ADDRESS >>================
    @Nested
    class AddAddressTests{

        @Test
        void shouldAddAddressSuccessfully(){
            when(userRepository.existsById(9L)).thenReturn(true);
            when(addressRepository.save(any()))
                    .thenAnswer(i -> {
                        Address address = i.getArgument(0);
                        address.setId(10L);
                        return address;
                    });

            AddressResponseDTO response =
                    addressService.addAddress(9L, buildAddressRequest(AddressType.HOME));

            assertNotNull(response);
            assertEquals(10L, response.getId());

            verify(addressRepository).save(argThat(a -> a.getAddressType() == AddressType.HOME));
        }

        @Test
        void shouldThrowException_whenUserNotFound(){
            when(userRepository.existsById(8L)).thenReturn(false);

            assertThrows(ResourceNotFoundException.class,
                    () -> addressService.addAddress(8L, buildAddressRequest(AddressType.BILLING)));
        }
    }

    //================<< GET ALL ADDRESSES BY USER >>================
    @Nested
    class GetAllAddressesByUserTests{

        @Test
        void shouldGetAllAddressByUser(){
            when(userRepository.existsById(7L)).thenReturn(true);
            when(addressRepository.findByUserId(7L))
                    .thenReturn(List.of(buildAddress(7L, AddressType.HOME),
                            buildAddress(7L, AddressType.OFFICE)));

            List<AddressResponseDTO> responses = addressService.getAllAddressesByUser(7L);

            assertFalse(responses.isEmpty());
            assertEquals(2, responses.size());
        }

        @Test
        void shouldGetEmptyList_whenNoAddressesFound(){
            when(userRepository.existsById(6L)).thenReturn(true);
            when(addressRepository.findByUserId(6L)).thenReturn(List.of());

            List<AddressResponseDTO> responses = addressService.getAllAddressesByUser(6L);

            assertTrue(responses.isEmpty());
        }

        @Test
        void shouldThrowException_whenUserNotFound(){
            when(userRepository.existsById(5L)).thenReturn(false);

            assertThrows(ResourceNotFoundException.class,
                    () -> addressService.getAllAddressesByUser(5L));
        }
    }

    //================<< GET ADDRESS BY ID >>================
    @Nested
    class GetAddressByIdTests{

        @Test
        void shouldGetAddressByIdTests(){
            when(addressRepository.findById(4L))
                    .thenReturn(Optional.of(buildAddress(4L, AddressType.SHIPPING)));

            AddressResponseDTO response = addressService.getAddressById(4L);

            assertNotNull(response);
        }

        @Test
        void shouldThrowException_whenAddressNotFound(){
            when(addressRepository.findById(3L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> addressService.getAddressById(3L));
        }
    }

    //================<< UPDATE ADDRESS >>================
    @Nested
    class UpdateAddressTests{

        @Test
        void shouldUpdateAddressSuccessfully(){
            when(addressRepository.findById(2L))
                    .thenReturn(Optional.of(buildAddress(2L, AddressType.OFFICE)));
            when(addressRepository.save(any()))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            AddressResponseDTO response =
                    addressService.updateAddress(2L, buildAddressRequest(AddressType.OFFICE));

            assertNotNull(response);

            verify(addressRepository).save(argThat(a -> a.getAddressType() == AddressType.OFFICE));
        }

        @Test
        void shouldThrowException_whenAddressNotFound(){
            when(addressRepository.findById(1L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> addressService.updateAddress(1L, buildAddressRequest(AddressType.HOME)));
        }
    }

    //================<< DELETE ADDRESS >>================
    @Nested
    class ShouldDeleteAddress{

        @Test
        void shouldDeleteAddressSuccessfully(){
            when(addressRepository.findById(3L))
                    .thenReturn(Optional.of(buildAddress(3L, AddressType.OFFICE)));

            AddressResponseDTO response =
                    addressService.deleteAddress(3L);

            assertNotNull(response);

            verify(addressRepository).delete(any());
        }

        @Test
        void shouldThrowException_whenAddressNotFound(){
            when(addressRepository.findById(2L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> addressService.deleteAddress(2L));
        }
    }
}
