package com.fashionshop.server.database;

import com.fashionshop.server.config.AppConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DatabaseConnection {

    private static final String URL = AppConfig.get("db.url");
    private static final String USERNAME = AppConfig.get("db.username");
    private static final String PASSWORD = AppConfig.get("db.password");

    private DatabaseConnection() {
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                URL,
                USERNAME,
                PASSWORD
        );
    }

    public static boolean testConnection() {
        try (Connection connection = getConnection()) {

            return connection != null
                    && !connection.isClosed();

        } catch (SQLException e) {
            System.err.println("Không thể kết nối database.");
            System.err.println("Chi tiết: " + e.getMessage());
            return false;
        }
    }
}