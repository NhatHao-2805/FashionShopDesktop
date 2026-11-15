package com.fashionshop.customer.service;

import com.fashionshop.customer.model.User;

public class SessionManager {

    private static final SessionManager INSTANCE =
            new SessionManager();

    private String token;
    private User currentUser;

    private SessionManager() {
    }

    public static SessionManager getInstance() {
        return INSTANCE;
    }

    public void login(
            String token,
            User user
    ) {
        this.token = token;
        this.currentUser = user;
        CartService.getInstance().clear();
    }

    public void logout() {
        this.token = null;
        this.currentUser = null;
        CartService.getInstance().clear();
    }

    public boolean isLoggedIn() {
        return token != null
                && !token.isBlank()
                && currentUser != null;
    }

    public boolean isCustomer() {
        return isLoggedIn() && "CUSTOMER".equalsIgnoreCase(currentUser.getRole());
    }

    public void requireCustomer() {
        if (!isCustomer()) {
            throw new IllegalStateException("Bạn cần đăng nhập tài khoản khách hàng để mua sắm.");
        }
    }

    public String getToken() {
        return token;
    }

    public User getCurrentUser() {
        return currentUser;
    }
}