package com.fashionshop.server.repository;

import com.fashionshop.server.database.DatabaseConnection;
import com.fashionshop.server.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserRepository {

    public User createCustomer(String name, String email, String passwordHash, String phone) {
        String sql = "INSERT INTO users (name, email, password_hash, phone, role, status) VALUES (?, ?, ?, ?, 'CUSTOMER', 'ACTIVE')";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, name);
            statement.setString(2, email);
            statement.setString(3, passwordHash);
            statement.setString(4, phone == null || phone.isBlank() ? null : phone);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) throw new IllegalStateException("Không tạo được tài khoản.");
                User user = new User();
                user.setId(keys.getLong(1)); user.setName(name); user.setEmail(email);
                user.setPhone(phone); user.setRole("CUSTOMER"); user.setStatus("ACTIVE");
                return user;
            }
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) throw new IllegalArgumentException("Email đã được sử dụng.");
            throw new IllegalStateException("Không thể tạo tài khoản.", e);
        }
    }

    public User findByEmail(String email) {
        String sql = """
                SELECT
                    id,
                    name,
                    email,
                    password_hash,
                    phone,
                    role,
                    status
                FROM users
                WHERE email = ?
                LIMIT 1
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, email);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }

                return mapUser(resultSet);
            }

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Không thể tìm user theo email.",
                    e
            );
        }
    }

    /** Read current authorization data on every protected request. */
    public User findById(long id) {
        String sql = "SELECT id, email, role, status FROM users WHERE id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) return null;
                User user = new User();
                user.setId(result.getLong("id"));
                user.setEmail(result.getString("email"));
                user.setRole(result.getString("role"));
                user.setStatus(result.getString("status"));
                return user;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể kiểm tra tài khoản hiện tại.", e);
        }
    }

    public boolean existsByEmail(String email) {
        String sql = """
                SELECT 1
                FROM users
                WHERE email = ?
                LIMIT 1
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, email);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Không thể kiểm tra email.",
                    e
            );
        }
    }

    private User mapUser(ResultSet resultSet) throws SQLException {
        User user = new User();

        user.setId(resultSet.getLong("id"));
        user.setName(resultSet.getString("name"));
        user.setEmail(resultSet.getString("email"));
        user.setPasswordHash(resultSet.getString("password_hash"));
        user.setPhone(resultSet.getString("phone"));
        user.setRole(resultSet.getString("role"));
        user.setStatus(resultSet.getString("status"));

        return user;
    }
}
