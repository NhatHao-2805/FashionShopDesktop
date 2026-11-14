package com.fashionshop.admin.service;

import com.fashionshop.admin.model.User;
import com.fashionshop.admin.network.ApiClient;

import com.google.gson.Gson;

public class AuthService {

    private final ApiClient apiClient;

    private final Gson gson;

    public AuthService() {

        this.apiClient =
                new ApiClient();

        this.gson =
                new Gson();
    }

    public User login(
            String email,
            String password
    ) throws Exception {

        if (email == null
                || email.isBlank()) {

            throw new IllegalArgumentException(
                    "Vui lòng nhập email."
            );
        }

        if (password == null
                || password.isBlank()) {

            throw new IllegalArgumentException(
                    "Vui lòng nhập mật khẩu."
            );
        }

        LoginRequest request =
                new LoginRequest();

        request.email =
                email.trim();

        request.password =
                password;

        String requestJson =
                gson.toJson(
                        request
                );

        String responseJson =
                apiClient.post(
                        "/api/auth/login",
                        requestJson
                );

        ApiResponse response =
                gson.fromJson(
                        responseJson,
                        ApiResponse.class
                );

        if (response == null) {

            throw new IllegalStateException(
                    "Server không trả dữ liệu."
            );
        }

        if (!response.success) {

            throw new IllegalArgumentException(
                    response.message != null
                            ? response.message
                            : "Đăng nhập thất bại."
            );
        }

        if (response.data == null
                || response.data.token == null
                || response.data.token.isBlank()
                || response.data.user == null) {

            throw new IllegalStateException(
                    "Dữ liệu đăng nhập không hợp lệ."
            );
        }

        User user =
                response.data.user;

        if (!user.isAdmin()) {

            throw new SecurityException(
                    "Tài khoản này không có quyền ADMIN."
            );
        }

        if (user.getStatus() != null
                && !user.getStatus()
                .equalsIgnoreCase(
                        "ACTIVE"
                )) {

            throw new SecurityException(
                    "Tài khoản ADMIN hiện không hoạt động."
            );
        }

        SessionManager
                .getInstance()
                .login(
                        response.data.token,
                        user
                );

        return user;
    }

    public void logout() {

        SessionManager
                .getInstance()
                .logout();
    }

    private static class LoginRequest {

        String email;

        String password;
    }

    private static class ApiResponse {

        boolean success;

        String message;

        LoginData data;
    }

    private static class LoginData {

        String token;

        User user;
    }
}