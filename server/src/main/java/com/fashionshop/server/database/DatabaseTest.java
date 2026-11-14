package com.fashionshop.server.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class DatabaseTest {

    private DatabaseTest() {
    }

    public static void run() {

        String sql = """
                SELECT
                    DATABASE() AS database_name,
                    COUNT(*) AS table_count
                FROM information_schema.tables
                WHERE table_schema = DATABASE()
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()
        ) {

            if (resultSet.next()) {

                String databaseName =
                        resultSet.getString("database_name");

                int tableCount =
                        resultSet.getInt("table_count");

                System.out.println("----------------------------------------------");
                System.out.println("DATABASE CONNECTION: SUCCESS");
                System.out.println("Database            : " + databaseName);
                System.out.println("Table count         : " + tableCount);
                System.out.println("----------------------------------------------");

            }

        } catch (SQLException e) {

            System.err.println("----------------------------------------------");
            System.err.println("DATABASE CONNECTION: FAILED");
            System.err.println("Error: " + e.getMessage());
            System.err.println("----------------------------------------------");

            throw new IllegalStateException(
                    "Không thể kiểm tra database.",
                    e
            );
        }
    }
}