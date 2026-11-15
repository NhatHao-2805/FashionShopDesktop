package com.fashionshop.server.security;

import com.fashionshop.server.model.User;
import com.fashionshop.server.repository.UserRepository;
import java.util.function.LongFunction;

/** JWT proves identity; the database supplies current permissions. */
public final class CurrentAccountAuthentication {
    private final LongFunction<User> findUser;

    public CurrentAccountAuthentication() {
        this(new UserRepository()::findById);
    }

    public CurrentAccountAuthentication(LongFunction<User> findUser) {
        this.findUser = java.util.Objects.requireNonNull(findUser);
    }

    public JwtAuthentication resolve(long userId) {
        if (userId <= 0) throw new SecurityException("Tài khoản không hợp lệ.");
        User user = findUser.apply(userId);
        if (user == null || !"ACTIVE".equalsIgnoreCase(user.getStatus()))
            throw new SecurityException("Tài khoản không tồn tại hoặc đã bị khóa. Vui lòng đăng nhập lại.");
        if (!"ADMIN".equalsIgnoreCase(user.getRole()) && !"CUSTOMER".equalsIgnoreCase(user.getRole()))
            throw new SecurityException("Tài khoản không có quyền truy cập hợp lệ.");
        return new JwtAuthentication(user.getId(), user.getEmail(), user.getRole());
    }
}
