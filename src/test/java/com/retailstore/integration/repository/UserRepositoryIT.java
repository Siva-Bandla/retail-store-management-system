package com.retailstore.integration.repository;

import com.retailstore.integration.repository.config.TestSecurityConfig;
import com.retailstore.testdata.UserTestData;
import com.retailstore.user.entity.User;
import com.retailstore.user.enums.UserRole;
import com.retailstore.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Import({UserTestData.class, TestSecurityConfig.class})
class UserRepositoryIT {

    @Autowired private UserRepository userRepository;
    @Autowired private UserTestData userTestData;
    @Autowired private PasswordEncoder passwordEncoder;

    //===============<< Save User>>================
    @Test
    @DisplayName("save(): should create a user with generated ID")
    void save_shouldCreateUser() {
        User user = userTestData.createCustomer();

        assertThat(user.getId()).isNotNull();
        assertThat(user.getEmail()).startsWith("customer_");
    }

    //===============<< Lifecycle: createdAt >>================
    @Test
    @DisplayName("createdAt: should set createdAt on save")
    void createdAt_shouldBeSet_OnCreate() {
        User user = userTestData.createCustomer();

        assertThat(user.getCreatedAt()).isNotNull();
        assertThat(user.getUpdatedAt()).isNull();
    }

    //===============<< Lifecycle: updatedAt >>================
    @Test
    @DisplayName("updatedAt: should update updatedAt on modifying entity")
    void updatedAt_shouldBeUpdated_OnUpdate() throws Exception {
        User user = userTestData.createCustomer();

        LocalDateTime createdTime = user.getCreatedAt();
        assertThat(createdTime).isNotNull();
        assertThat(user.getUpdatedAt()).isNull();

        Thread.sleep(10);

        user.setPhone("7777777777");
        User updated = userRepository.saveAndFlush(user);

        assertThat(updated.getUpdatedAt()).isNotNull();
        assertThat(updated.getUpdatedAt()).isAfter(createdTime);
    }

    //===============<< findByEmail >>================
    @Test
    @DisplayName("findByEmail(): should return the user when email exists")
    void findByEmail_shouldReturnUser() {
        User saved = userTestData.createCustomer();

        Optional<User> found = userRepository.findByEmail(saved.getEmail());

        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo(saved.getEmail());
    }

    @Test
    @DisplayName("findByEmail(): should return empty when no user with email")
    void findByEmail_shouldReturnEmpty() {
        Optional<User> found = userRepository.findByEmail("doesNotExist@test.com");

        assertThat(found).isEmpty();
    }

    //===============<< existsByEmail >>================
    @Test
    @DisplayName("existsByEmail(): should return true when email exists")
    void existsByEmail_shouldReturnTrue() {
        User saved = userTestData.createAdmin();

        boolean exists = userRepository.existsByEmail(saved.getEmail());

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("existsByEmail(): should return false when email does not exist")
    void existsByEmail_shouldReturnFalse() {
        boolean exists = userRepository.existsByEmail("nope@test.com");

        assertThat(exists).isFalse();
    }

    //===============<< Update user >>================
    @Test
    @DisplayName("update(): should update user fields")
    void update_shouldUpdateFields() {
        User user = userTestData.createCustomer();

        user.setName("Updated Name");
        user.setPhone("1112223333");
        user.setFailedAttempts(5);

        User updated = userRepository.save(user);

        assertThat(updated.getName()).isEqualTo("Updated Name");
        assertThat(updated.getPhone()).isEqualTo("1112223333");
        assertThat(updated.getFailedAttempts()).isEqualTo(5);
    }

    //===============<< Delete user >>================
    @Test
    @DisplayName("delete(): should remove the user from the DB")
    void delete_shouldRemoveUser() {
        User user = userTestData.createCustomer();

        userRepository.delete(user);

        Optional<User> found = userRepository.findById(user.getId());

        assertThat(found).isEmpty();
    }

    //===============<< Unique Constraint >>================
    @Test
    @DisplayName("save(): should throw exception for duplicate email")
    void save_duplicateEmail_shouldThrowException() {
        User u1 = userTestData.createCustomer();

        User u2 = new User();
        u2.setName("Other");
        u2.setEmail(u1.getEmail()); // duplicate
        u2.setPassword("Pass");
        u2.setRole(UserRole.ROLE_CUSTOMER);
        u2.setPhone("000");

        assertThatThrownBy(() -> userRepository.saveAndFlush(u2))
                .isInstanceOf(Exception.class);
    }
}