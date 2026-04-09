package com.retailstore.integration.repository;

import com.retailstore.user.entity.Address;
import com.retailstore.user.enums.AddressType;
import com.retailstore.user.repository.AddressRepository;
import com.retailstore.testdata.AddressTestData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(AddressTestData.class)
public class AddressRepositoryIT {

    @Autowired private AddressRepository addressRepository;
    @Autowired private AddressTestData addressTestData;

    // ============================<< SAVE ADDRESS >>============================
    @Test
    @DisplayName("Save address successfully")
    void saveAddress_success() {

        Address address = addressTestData.createAddress(1L, AddressType.HOME);

        assertThat(address.getId()).isNotNull();
        assertThat(address.getUserId()).isEqualTo(1L);
        assertThat(address.getAddressType()).isEqualTo(AddressType.HOME);
        assertThat(address.getStreet()).isNotBlank();
        assertThat(address.getPincode()).isNotBlank();
    }

    // ========================<< FIND BY USER ID >>========================
    @Test
    @DisplayName("findByUserId returns all addresses for user")
    void findByUserId_returnsList() {

        addressTestData.createAddress(10L, AddressType.HOME);
        addressTestData.createAddress(10L, AddressType.OFFICE);
        addressTestData.createAddress(20L, AddressType.HOME);

        List<Address> list = addressRepository.findByUserId(10L);

        assertThat(list).hasSize(2);
        assertThat(list).allMatch(a -> a.getUserId().equals(10L));
    }

    @Test
    @DisplayName("findByUserId returns empty list when no addresses exist")
    void findByUserId_empty() {

        List<Address> list = addressRepository.findByUserId(999L);

        assertThat(list).isEmpty();
    }

    // ========================<< EXISTS BY ID & USER ID >>========================
    @Test
    @DisplayName("existsByIdAndUserId returns true when address belongs to user")
    void existsByIdAndUserId_true() {

        Address address = addressTestData.createAddress(5L, AddressType.OFFICE);

        boolean exists = addressRepository.existsByIdAndUserId(address.getId(), 5L);

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("existsByIdAndUserId returns false when user mismatches")
    void existsByIdAndUserId_false_wrongUser() {

        Address address = addressTestData.createAddress(5L, AddressType.HOME);

        boolean exists = addressRepository.existsByIdAndUserId(address.getId(), 999L);

        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("existsByIdAndUserId returns false when address does not exist")
    void existsByIdAndUserId_false_noAddress() {

        boolean exists = addressRepository.existsByIdAndUserId(999L, 1L);

        assertThat(exists).isFalse();
    }
}