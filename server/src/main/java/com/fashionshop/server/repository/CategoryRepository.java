package com.fashionshop.server.repository;

import com.fashionshop.server.database.DatabaseConnection;
import com.fashionshop.server.model.Category;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CategoryRepository {

    public List<Category> findAllActive() {

        String sql = """
                SELECT
                    id,
                    name,
                    description,
                    status
                FROM categories
                WHERE status = 'ACTIVE'
                ORDER BY id ASC
                """;

        List<Category> categories = new ArrayList<>();

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql);

                ResultSet resultSet =
                        statement.executeQuery()
        ) {

            while (resultSet.next()) {

                categories.add(mapCategory(resultSet));
            }

            return categories;

        } catch (SQLException e) {

            throw new IllegalStateException(
                    "Không thể lấy danh sách categories.",
                    e
            );
        }
    }

    public Category findById(long id) {

        String sql = """
                SELECT
                    id,
                    name,
                    description,
                    status
                FROM categories
                WHERE id = ?
                  AND status = 'ACTIVE'
                LIMIT 1
                """;

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setLong(1, id);

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                if (!resultSet.next()) {
                    return null;
                }

                return mapCategory(resultSet);
            }

        } catch (SQLException e) {

            throw new IllegalStateException(
                    "Không thể tìm category.",
                    e
            );
        }
    }

    private Category mapCategory(
            ResultSet resultSet
    ) throws SQLException {

        Category category = new Category();

        category.setId(
                resultSet.getLong("id")
        );

        category.setName(
                resultSet.getString("name")
        );

        category.setDescription(
                resultSet.getString("description")
        );

        category.setStatus(
                resultSet.getString("status")
        );

        return category;
    }
}