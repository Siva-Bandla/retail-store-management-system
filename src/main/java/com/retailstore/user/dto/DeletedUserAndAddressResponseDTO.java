package com.retailstore.user.dto;

import com.retailstore.user.entity.Address;
import lombok.Data;

import java.util.List;

@Data
public class DeletedUserAndAddressResponseDTO {

    private UserResponseDTO user;
    private List<Address> addresses;

    public DeletedUserAndAddressResponseDTO(UserResponseDTO user, List<Address> addresses){

        this.user = user;
        this.addresses = addresses;
    }
}
