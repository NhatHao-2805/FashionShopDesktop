package com.fashionshop.server.service;

import com.fashionshop.server.model.User;
import com.fashionshop.server.repository.UserRepository;
import com.fashionshop.server.security.JwtService;
import org.mindrot.jbcrypt.BCrypt;

public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    public AuthService() {
        this.userRepository = new UserRepository();
        this.jwtService = new JwtService();
    }

    public LoginResult login(String email, String password) {

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException(
                    "Email không được để trống."
            );
        }

        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException(
                    "Password không được để trống."
            );
        }

        String normalizedEmail = email.trim().toLowerCase();

        User user = userRepository.findByEmail(normalizedEmail);

        if (user == null) {
            throw new IllegalArgumentException(
                    "Email hoặc password không chính xác."
            );
        }

        if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
            throw new IllegalStateException(
                    "Tài khoản hiện không hoạt động."
            );
        }

        boolean passwordMatches = BCrypt.checkpw(
                password,
                user.getPasswordHash()
        );

        if (!passwordMatches) {
            throw new IllegalArgumentException(
                    "Email hoặc password không chính xác."
            );
        }

        String token = jwtService.generateToken(user);

        return new LoginResult(
                token,
                user
        );
    }

    public User registerCustomer(String name, String email, String password, String phone) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Họ tên không được để trống.");
        if (email == null || !email.trim().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$"))
            throw new IllegalArgumentException("Email không hợp lệ.");
        if (password == null || password.length() < 6)
            throw new IllegalArgumentException("Mật khẩu phải có ít nhất 6 ký tự.");
        String normalized = email.trim().toLowerCase();
        if (userRepository.existsByEmail(normalized)) throw new IllegalArgumentException("Email đã được sử dụng.");
        return userRepository.createCustomer(name.trim(), normalized, BCrypt.hashpw(password, BCrypt.gensalt(12)), phone);
    }

    public static class LoginResult {

        private final String token;
        private final User user;

        public LoginResult(String token, User user) {
            this.token = token;
            this.user = user;
        }

        public String getToken() {
            return token;
        }

        public User getUser() {
            return user;
        }
    }
}
