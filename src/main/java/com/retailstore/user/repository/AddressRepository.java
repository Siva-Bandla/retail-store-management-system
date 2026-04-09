package com.retailstore.user.repository;

import com.retailstore.user.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AddressRepository extends JpaRepository<Address, Long> {

    List<Address> findByUserId(Long userId);

    boolean existsByIdAndUserId(Long addressId, Long id);
}
