package com.retailstore.unit.security;

import com.retailstore.security.jwt.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp(){
        jwtUtil = new JwtUtil();
    }

    @Test
    void shouldGenerateTokenSuccessfully(){
        String token = jwtUtil.generateToken("test@example.com", List.of("ROLE_CUSTOMER"));

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void shouldExtractEmailFromToken(){
        String token = jwtUtil.generateToken("john@example.com", List.of("ROLE_ADMIN"));
        String extractedEmail = jwtUtil.extractEmail(token);

        assertEquals("john@example.com", extractedEmail);
    }

    @Test
    void shouldExtractRolesFromToken(){
        List<String> roles = List.of("ROLE_ADMIN", "ROLE_CUSTOMER");
        String token = jwtUtil.generateToken("john@example.com",roles);
        List<String> extractedRoles = jwtUtil.extractRoles(token);

        assertFalse(extractedRoles.isEmpty());
        assertEquals(roles, extractedRoles);
    }

    @Test
    void shouldValidateToken() {
        String token = jwtUtil.generateToken("alex@example.com", List.of("ROLE_CUSTOMER"));

        assertTrue(jwtUtil.validateToken(token));
    }

    @Test
    void shouldRejectExpiredToken() throws Exception {
        JwtUtil util = new JwtUtil();

        String token = util.generateToken("expired@example.com", List.of("ROLE_CUSTOMER"));

        var secretField = JwtUtil.class.getDeclaredField("SECRET");
        secretField.setAccessible(true);
        String secret = (String) secretField.get(null);

        Key key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        claims.setExpiration(new Date(System.currentTimeMillis() - 5000));

        String expiredToken = Jwts.builder()
                .setClaims(claims)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        assertFalse(util.validateToken(expiredToken));
    }

    @Test
    void shouldRejectTamperedToken(){
        String token = jwtUtil.generateToken("tamper@example.com", List.of("ROLE_CUSTOMER"));

        String tampered = token.substring(0, token.length() - 2) + "xx";

        assertFalse(jwtUtil.validateToken(tampered));
    }

    @Test
    void shouldRejectMalformedToken(){
        assertFalse(jwtUtil.validateToken("invalid-token-x.y.z"));
    }

    @Test
    void shouldRejectNullOrEmptyToken(){
        assertFalse(jwtUtil.validateToken(null));
        assertFalse(jwtUtil.validateToken(""));
    }
}
