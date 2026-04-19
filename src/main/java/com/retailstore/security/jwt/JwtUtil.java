package com.retailstore.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Component
public class JwtUtil {

    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);

    private static final String SECRET = "mysecretkeymysecretkeymysecretkeymysecretkey";

    private final Key key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

    private static final long EXPIRATION_TIME = 1000 * 360;

    public String generateToken(Long userId, String email, List<String> roles){

        return Jwts.builder()
                .setSubject(email)
                .claim("userId", userId)
                .claim("roles", roles)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .setId(UUID.randomUUID().toString())
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Long extractUserId(String token){
        return getClaims(token).get("userId", Long.class);
    }

    public String extractEmail(String token){
        return getClaims(token).getSubject();
    }

    public List<String> extractRoles(String token){
        List<?> roles = getClaims(token).get("roles", List.class);

        return roles.stream()
                .map(Object::toString)
                .toList();
    }

    public boolean validateToken(String token){
        try{
            getClaims(token);
            return true;

        }catch (ExpiredJwtException ex){
            log.warn("JWT expired: {}", ex.getMessage());

        }catch (UnsupportedJwtException ex){
            log.error("Unsupported JWT: {}", ex.getMessage());

        } catch(MalformedJwtException ex){
            log.error("Malformed JWT: {}", ex.getMessage());

        }catch(SignatureException ex){
            log.error("Invalid signature: {}", ex.getMessage());

        }catch (IllegalArgumentException ex){
            log.error("JWT token is empty or null");
        }

        return false;
    }

    private Claims getClaims(String token){

        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

}
