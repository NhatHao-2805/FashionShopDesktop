package com.fashionshop.server.security;

import com.fashionshop.server.config.AppConfig;
import com.fashionshop.server.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

public class JwtService {

    private final SecretKey secretKey;
    private final long expirationMillis;

    public JwtService() {

        String secret = AppConfig.get("jwt.secret");

        if (secret.length() < 32) {
            throw new IllegalStateException(
                    "jwt.secret phải có ít nhất 32 ký tự."
            );
        }

        this.secretKey = Keys.hmacShaKeyFor(
                secret.getBytes(StandardCharsets.UTF_8)
        );

        this.expirationMillis =
                AppConfig.getInt("jwt.expiration.hours")
                        * 60L
                        * 60L
                        * 1000L;
    }

    public String generateToken(User user) {

        Date now = new Date();

        Date expiration = new Date(
                now.getTime() + expirationMillis
        );

        return Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .claim("email", user.getEmail())
                .claim("role", user.getRole())
                .issuedAt(now)
                .expiration(expiration)
                .signWith(secretKey)
                .compact();
    }

    public Claims parseToken(String token) {

        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException(
                    "JWT token không được để trống."
            );
        }

        try {

            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

        } catch (Exception e) {

            throw new IllegalArgumentException(
                    "JWT token không hợp lệ hoặc đã hết hạn.",
                    e
            );
        }
    }

    public long getUserId(String token) {

        Claims claims = parseToken(token);

        return Long.parseLong(
                claims.getSubject()
        );
    }

    public String getEmail(String token) {

        Claims claims = parseToken(token);

        return claims.get("email", String.class);
    }

    public String getRole(String token) {

        Claims claims = parseToken(token);

        return claims.get("role", String.class);
    }

    public boolean isValid(String token) {

        try {
            parseToken(token);
            return true;

        } catch (Exception e) {
            return false;
        }
    }
}