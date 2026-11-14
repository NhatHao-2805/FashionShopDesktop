package com.fashionshop.server.database;

import com.fashionshop.server.model.User;
import com.fashionshop.server.repository.UserRepository;

public class UserRepositoryTest {

    public static void main(String[] args) {
        UserRepository userRepository = new UserRepository();

        System.out.println("==============================================");
        System.out.println("       USER REPOSITORY DATABASE TEST");
        System.out.println("==============================================");

        testUser(userRepository, "admin@fashion.local");
        testUser(userRepository, "user1@fashion.local");
        testUser(userRepository, "user2@fashion.local");

        System.out.println("==============================================");
        System.out.println("USER REPOSITORY TEST: SUCCESS");
        System.out.println("==============================================");
    }

    private static void testUser(
            UserRepository userRepository,
            String email
    ) {
        User user = userRepository.findByEmail(email);

        if (user == null) {
            throw new IllegalStateException(
                    "Không tìm thấy user: " + email
            );
        }

        System.out.println("----------------------------------------------");
        System.out.println("ID     : " + user.getId());
        System.out.println("Name   : " + user.getName());
        System.out.println("Email  : " + user.getEmail());
        System.out.println("Phone  : " + user.getPhone());
        System.out.println("Role   : " + user.getRole());
        System.out.println("Status : " + user.getStatus());

        if (user.getPasswordHash() == null
                || !user.getPasswordHash().startsWith("$2a$")) {
            throw new IllegalStateException(
                    "Password hash không hợp lệ cho: " + email
            );
        }
    }
}