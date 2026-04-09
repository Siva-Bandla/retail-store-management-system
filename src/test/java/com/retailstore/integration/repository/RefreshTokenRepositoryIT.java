package com.retailstore.integration.repository;

import com.retailstore.security.entity.RefreshToken;
import com.retailstore.security.repository.RefreshTokenRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
class RefreshTokenRepositoryIT {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private EntityManager entityManager;


    private RefreshToken createToken(String username, String tokenValue) {
        RefreshToken rt = new RefreshToken();
        rt.setUsername(username);
        rt.setToken(tokenValue);
        rt.setExpiryDate(LocalDateTime.now().plusDays(7));
        rt.setRevoked(false);
        return refreshTokenRepository.save(rt);
    }


    @Test
    @DisplayName("Should save and retrieve RefreshToken by token")
    void testFindByToken() {

        RefreshToken saved = createToken("user1", "token123");

        Optional<RefreshToken> found = refreshTokenRepository.findByToken("token123");

        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("user1");
    }


    @Test
    @DisplayName("Should return empty when token not found")
    void testFindByToken_NotFound() {

        Optional<RefreshToken> found = refreshTokenRepository.findByToken("unknown");

        assertThat(found).isNotPresent();
    }


    @Test
    @DisplayName("Should return all tokens for a username")
    void testFindAllByUsername() {

        createToken("userA", "token1");
        createToken("userA", "token2");
        createToken("otherUser", "token3");

        List<RefreshToken> results = refreshTokenRepository.findAllByUsername("userA");

        assertThat(results).hasSize(2);
    }


    @Test
    @DisplayName("Should delete all refresh tokens for a specific username")
    void testDeleteByUsername() {

        createToken("user2", "tokenX");
        createToken("user2", "tokenY");

        refreshTokenRepository.deleteByUsername("user2");

        entityManager.flush(); // Force execution

        List<RefreshToken> remaining = refreshTokenRepository.findAll();

        assertThat(remaining).isEmpty();
    }


    @Test
    @DisplayName("Should enforce unique constraint on token")
    void testUniqueTokenConstraint() {

        createToken("user3", "uniqueToken");

        RefreshToken duplicate = new RefreshToken();
        duplicate.setToken("uniqueToken"); // Same token -> should fail
        duplicate.setUsername("someoneElse");
        duplicate.setExpiryDate(LocalDateTime.now().plusDays(5));

        assertThatThrownBy(() -> {
            refreshTokenRepository.save(duplicate);
            entityManager.flush();
        }).isInstanceOf(Exception.class);
    }


    @Test
    @DisplayName("Should save revoked status correctly")
    void testRevokedFlag() {

        RefreshToken rt = new RefreshToken();
        rt.setToken("revTest");
        rt.setUsername("userRev");
        rt.setExpiryDate(LocalDateTime.now().plusDays(7));
        rt.setRevoked(true);

        RefreshToken saved = refreshTokenRepository.save(rt);

        assertThat(saved.isRevoked()).isTrue();
    }


    @Test
    @DisplayName("Should save and retrieve expiry date field correctly")
    void testExpiryDateField() {

        LocalDateTime expiry = LocalDateTime.now().plusDays(3);

        RefreshToken rt = new RefreshToken();
        rt.setToken("expToken");
        rt.setUsername("userExp");
        rt.setExpiryDate(expiry);

        RefreshToken saved = refreshTokenRepository.save(rt);

        assertThat(saved.getExpiryDate()).isEqualTo(expiry);
    }
}