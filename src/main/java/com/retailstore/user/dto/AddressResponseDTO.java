package com.retailstore.user.dto;

import com.retailstore.user.enums.AddressType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressResponseDTO {

    private Long id;
    private String street;
    private String city;
    private String state;
    private String pincode;
    private AddressType type;
}
