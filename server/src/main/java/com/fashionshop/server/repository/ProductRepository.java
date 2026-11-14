package com.fashionshop.server.repository;

import com.fashionshop.server.database.DatabaseConnection;
import com.fashionshop.server.model.Product;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProductRepository {

    // =============================================================
    // FIND ALL ACTIVE PRODUCTS
    // =============================================================

    public List<Product> findAllActive() {

        String sql = """
                SELECT
                    p.id,
                    p.name,
                    p.description,
                    p.price,
                    p.category_id,
                    c.name AS category_name,
                    p.size,
                    p.color,
                    p.stock,
                    p.image_path,
                    p.status
                FROM products p
                LEFT JOIN categories c
                    ON c.id = p.category_id
                WHERE p.status = 'ACTIVE'
                ORDER BY p.id DESC
                """;

        List<Product> products =
                new ArrayList<>();

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql);

                ResultSet resultSet =
                        statement.executeQuery()
        ) {

            while (resultSet.next()) {

                products.add(
                        mapProduct(resultSet)
                );
            }

            return products;

        } catch (SQLException e) {

            throw new IllegalStateException(
                    "Không thể lấy danh sách sản phẩm.",
                    e
            );
        }
    }


    // =============================================================
    // FIND PRODUCT BY ID
    // =============================================================

    public Product findById(long id) {

        String sql = """
                SELECT
                    p.id,
                    p.name,
                    p.description,
                    p.price,
                    p.category_id,
                    c.name AS category_name,
                    p.size,
                    p.color,
                    p.stock,
                    p.image_path,
                    p.status
                FROM products p
                LEFT JOIN categories c
                    ON c.id = p.category_id
                WHERE p.id = ?
                  AND p.status = 'ACTIVE'
                LIMIT 1
                """;

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setLong(
                    1,
                    id
            );

            try (
                    ResultSet resultSet =
                            statement.executeQuery()
            ) {

                if (!resultSet.next()) {

                    return null;
                }

                return mapProduct(
                        resultSet
                );
            }

        } catch (SQLException e) {

            throw new IllegalStateException(
                    "Không thể tìm sản phẩm theo ID.",
                    e
            );
        }
    }


    // =============================================================
    // SEARCH / FILTER PRODUCTS
    // =============================================================

    public List<Product> search(
            String keyword,
            Long categoryId
    ) {

        StringBuilder sql =
                new StringBuilder("""
                        SELECT
                            p.id,
                            p.name,
                            p.description,
                            p.price,
                            p.category_id,
                            c.name AS category_name,
                            p.size,
                            p.color,
                            p.stock,
                            p.image_path,
                            p.status
                        FROM products p
                        LEFT JOIN categories c
                            ON c.id = p.category_id
                        WHERE p.status = 'ACTIVE'
                        """);

        List<Object> parameters =
                new ArrayList<>();


        // =========================================================
        // KEYWORD SEARCH
        // =========================================================

        if (
                keyword != null
                        && !keyword.isBlank()
        ) {

            sql.append("""
                    AND (
                        LOWER(p.name) LIKE ?
                        OR LOWER(p.description) LIKE ?
                    )
                    """);

            String searchKeyword =
                    "%"
                            + keyword.trim().toLowerCase()
                            + "%";

            parameters.add(
                    searchKeyword
            );

            parameters.add(
                    searchKeyword
            );
        }


        // =========================================================
        // CATEGORY FILTER
        // =========================================================

        if (categoryId != null) {

            sql.append("""
                    AND p.category_id = ?
                    """);

            parameters.add(
                    categoryId
            );
        }


        // =========================================================
        // SORT
        // =========================================================

        sql.append("""
                ORDER BY p.id DESC
                """);


        List<Product> products =
                new ArrayList<>();


        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(
                                sql.toString()
                        )
        ) {

            for (
                    int i = 0;
                    i < parameters.size();
                    i++
            ) {

                statement.setObject(
                        i + 1,
                        parameters.get(i)
                );
            }


            try (
                    ResultSet resultSet =
                            statement.executeQuery()
            ) {

                while (resultSet.next()) {

                    products.add(
                            mapProduct(resultSet)
                    );
                }
            }

            return products;

        } catch (SQLException e) {

            throw new IllegalStateException(
                    "Không thể tìm kiếm sản phẩm.",
                    e
            );
        }
    }


    // =============================================================
    // MAP RESULTSET -> PRODUCT
    // =============================================================

    private Product mapProduct(
            ResultSet resultSet
    ) throws SQLException {

        Product product =
                new Product();


        product.setId(
                resultSet.getLong(
                        "id"
                )
        );


        product.setName(
                resultSet.getString(
                        "name"
                )
        );


        product.setDescription(
                resultSet.getString(
                        "description"
                )
        );


        product.setPrice(
                resultSet.getBigDecimal(
                        "price"
                )
        );


        product.setCategoryId(
                resultSet.getLong(
                        "category_id"
                )
        );


        product.setCategoryName(
                resultSet.getString(
                        "category_name"
                )
        );


        product.setSize(
                resultSet.getString(
                        "size"
                )
        );


        product.setColor(
                resultSet.getString(
                        "color"
                )
        );


        product.setStock(
                resultSet.getInt(
                        "stock"
                )
        );


        product.setImageUrl(
                resultSet.getString(
                        "image_path"
                )
        );


        product.setStatus(
                resultSet.getString(
                        "status"
                )
        );


        return product;
    }
}