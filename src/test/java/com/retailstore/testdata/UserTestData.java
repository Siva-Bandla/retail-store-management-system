package com.retailstore.testdata;

import com.retailstore.user.entity.User;
import com.retailstore.user.enums.UserRole;
import com.retailstore.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

public class UserTestData {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User createAdmin() {
        User user = new User();
        user.setName("Admin User");
        user.setEmail("admin_" + System.nanoTime() + "@test.com");
        user.setPassword(passwordEncoder.encode("Pass1234"));
        user.setRole(UserRole.ROLE_ADMIN);
        user.setPhone("9999999999");

        return userRepository.save(user);
    }

    public User createCustomer() {
        User user = new User();
        user.setName("Customer User");
        user.setEmail("customer_" + System.nanoTime() + "@test.com");
        user.setPassword(passwordEncoder.encode("Pass1234"));
        user.setRole(UserRole.ROLE_CUSTOMER);
        user.setPhone("8888888888");

        return userRepository.save(user);
    }
}