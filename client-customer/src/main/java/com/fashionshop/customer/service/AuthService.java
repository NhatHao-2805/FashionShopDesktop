package com.fashionshop.customer.service;

import com.fashionshop.customer.model.User;
import com.fashionshop.customer.network.ApiClient;
import com.google.gson.Gson;

public class AuthService {

    private final ApiClient apiClient;
    private final Gson gson;

    public AuthService() {
        this.apiClient = new ApiClient();
        this.gson = new Gson();
    }

    public User login(
            String email,
            String password
    ) throws Exception {

        LoginRequest request =
                new LoginRequest();

        request.email = email;
        request.password = password;

        String requestJson =
                gson.toJson(request);

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
                || response.data.user == null) {

            throw new IllegalStateException(
                    "Dữ liệu đăng nhập không hợp lệ."
            );
        }

        SessionManager.getInstance()
                .login(
                        response.data.token,
                        response.data.user
                );

        return response.data.user;
    }

    public void logout() {

        SessionManager.getInstance()
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