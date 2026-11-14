package com.fashionshop.server.security;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordHashGenerator {

    public static void main(String[] args) {

        String[] passwords = {
                "Admin@123",
                "User@123"
        };

        for (String password : passwords) {
            String hash = BCrypt.hashpw(
                    password,
                    BCrypt.gensalt(12)
            );

            System.out.println("Password: " + password);
            System.out.println("Hash    : " + hash);
            System.out.println();
        }
    }
}