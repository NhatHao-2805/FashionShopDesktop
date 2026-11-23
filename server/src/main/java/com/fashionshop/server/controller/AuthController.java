package com.fashionshop.server.controller;

import com.fashionshop.server.model.User;
import com.fashionshop.server.service.AuthService;
import com.google.gson.Gson;

import static spark.Spark.post;

public class AuthController {

    private final AuthService authService;
    private final Gson gson;

    public AuthController() {
        this.authService = new AuthService();
        this.gson = new Gson();
    }

    public void registerRoutes() {

        post("/api/auth/login", (request, response) -> {

            response.type("application/json");

            try {
                LoginRequest loginRequest =
                        gson.fromJson(
                                request.body(),
                                LoginRequest.class
                        );

                if (loginRequest == null) {
                    response.status(400);
                    return error("Request không hợp lệ.");
                }

                AuthService.LoginResult result =
                        authService.login(
                                loginRequest.email,
                                loginRequest.password
                        );

                response.status(200);

                return success(result);

            } catch (IllegalArgumentException e) {

                response.status(401);

                return error(e.getMessage());

            } catch (IllegalStateException e) {

                response.status(403);

                return error(e.getMessage());

            } catch (Exception e) {

                e.printStackTrace();

                response.status(500);

                return error(
                        "Không thể xử lý đăng nhập."
                );
            }
        });

        post("/api/auth/register", (request, response) -> {
            response.type("application/json");
            try {
                RegisterRequest input = gson.fromJson(request.body(), RegisterRequest.class);
                if (input == null) throw new IllegalArgumentException("Request không hợp lệ.");
                User user = authService.registerCustomer(input.name, input.email, input.password, input.phone);
                response.status(201);
                return successMessage("Đăng ký tài khoản thành công.");
            } catch (IllegalArgumentException e) { response.status(400); return error(e.getMessage()); }
            catch (Exception e) { response.status(500); return error("Không thể đăng ký tài khoản."); }
        });
    }

    private String success(AuthService.LoginResult result) {

        User user = result.getUser();

        LoginResponse response = new LoginResponse();

        response.token = result.getToken();
        response.user = new UserResponse();

        response.user.id = user.getId();
        response.user.name = user.getName();
        response.user.email = user.getEmail();
        response.user.phone = user.getPhone();
        response.user.role = user.getRole();
        response.user.status = user.getStatus();

        ApiResponse apiResponse = new ApiResponse();

        apiResponse.success = true;
        apiResponse.message = "Đăng nhập thành công.";
        apiResponse.data = response;

        return gson.toJson(apiResponse);
    }

    private String error(String message) {

        ApiResponse response = new ApiResponse();

        response.success = false;
        response.message = message;
        response.data = null;

        return gson.toJson(response);
    }

    private String successMessage(String message) {
        ApiResponse response = new ApiResponse(); response.success = true; response.message = message; response.data = null;
        return gson.toJson(response);
    }

    private static class LoginRequest {
        String email;
        String password;
    }
    private static class RegisterRequest { String name; String email; String password; String phone; }

    private static class LoginResponse {
        String token;
        UserResponse user;
    }

    private static class UserResponse {
        long id;
        String name;
        String email;
        String phone;
        String role;
        String status;
    }

    private static class ApiResponse {
        boolean success;
        String message;
        Object data;
    }
}
