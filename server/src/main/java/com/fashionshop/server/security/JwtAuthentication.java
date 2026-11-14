package com.fashionshop.server.security;

import io.jsonwebtoken.Claims;

public class JwtAuthentication {

    private final long userId;
    private final String email;
    private final String role;

    public JwtAuthentication(
            long userId,
            String email,
            String role
    ) {
        this.userId = userId;
        this.email = email;
        this.role = role;
    }

    public long getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(role);
    }

    public boolean isCustomer() {
        return "CUSTOMER".equalsIgnoreCase(role);
    }

    public static JwtAuthentication fromClaims(
            Claims claims
    ) {

        long userId = Long.parseLong(
                claims.getSubject()
        );

        String email =
                claims.get("email", String.class);

        String role =
                claims.get("role", String.class);

        return new JwtAuthentication(
                userId,
                email,
                role
        );
    }
}