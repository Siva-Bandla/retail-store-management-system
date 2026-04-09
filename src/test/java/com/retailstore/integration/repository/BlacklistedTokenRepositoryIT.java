package com.retailstore.integration.repository;

import com.retailstore.security.entity.BlacklistedToken;
import com.retailstore.security.repository.BlacklistedTokenRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import jakarta.persistence.EntityManager;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
class BlacklistedTokenRepositoryIT {

    @Autowired
    private BlacklistedTokenRepository repository;

    @Autowired
    private EntityManager entityManager;


    private BlacklistedToken create(String token) {
        BlacklistedToken b = new BlacklistedToken();
        b.setToken(token);
        return repository.save(b);
    }


    // ========================= TESTS =============================

    @Test
    @DisplayName("Should save and retrieve blacklisted token")
    void testSave() {

        BlacklistedToken saved = create("abc123token");

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getToken()).isEqualTo("abc123token");
    }


    @Test
    @DisplayName("existsByToken should return true when token exists")
    void testExistsByToken_True() {

        create("jwt-xyz-123");

        boolean exists = repository.existsByToken("jwt-xyz-123");

        assertThat(exists).isTrue();
    }


    @Test
    @DisplayName("existsByToken should return false when token does not exist")
    void testExistsByToken_False() {

        boolean exists = repository.existsByToken("non-existent");

        assertThat(exists).isFalse();
    }


    @Test
    @DisplayName("Should enforce unique token constraint")
    void testTokenUniqueConstraint() {

        create("unique-token");

        BlacklistedToken duplicate = new BlacklistedToken();
        duplicate.setToken("unique-token");

        assertThatThrownBy(() -> {
            repository.save(duplicate);
            entityManager.flush();  // force DB constraint check
        }).isInstanceOf(Exception.class);
    }
}