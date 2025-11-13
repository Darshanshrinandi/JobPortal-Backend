package com.jobPortal.Jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${secret.key}")
    private String key;

    private static final long JWT_EXPIRATION_MS = 1000 * 60 * 30;


    public String generateToken(Long id, String email, String type) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", id);
        claims.put("type", type.toUpperCase());
        claims.put("role", type.toUpperCase());

        return buildToken(claims, email);
    }


    public String refreshToken(String oldToken) {
        Claims claims = extractAllClaims(oldToken);

        Long id = Long.parseLong(claims.get("id").toString());
        String email = claims.getSubject();
        String type = claims.get("type").toString();

        return generateToken(id, email, type);
    }

    private String buildToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + JWT_EXPIRATION_MS))
                .signWith(getKey())
                .compact();
    }

    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Long extractId(String token) {
        Object id = extractAllClaims(token).get("id");
        return id != null ? Long.parseLong(id.toString()) : null;
    }

    public String extractRole(String token) {
        Object role = extractAllClaims(token).get("role");
        return role != null ? role.toString() : "USER";
    }

    public String extractType(String token) {
        Object type = extractAllClaims(token).get("type");
        return type != null ? type.toString() : "USER";
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validateToken(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private SecretKey getKey() {
        try {
            byte[] keyBytes = Decoders.BASE64.decode(key);
            return Keys.hmacShaKeyFor(keyBytes);
        } catch (IllegalArgumentException e) {
            return Keys.hmacShaKeyFor(key.getBytes(StandardCharsets.UTF_8));
        }
    }

    public long getJwtExpirationMs() {
        return JWT_EXPIRATION_MS;
    }
}
