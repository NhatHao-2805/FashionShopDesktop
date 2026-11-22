package com.fashionshop.server.repository;

import com.fashionshop.server.database.DatabaseConnection;
import com.fashionshop.server.model.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AdminProductRepository {

    public List<Product> findAll() {

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
                        mapProduct(
                                resultSet
                        )
                );
            }

            return products;

        } catch (SQLException e) {

            throw new IllegalStateException(
                    "Không thể lấy danh sách sản phẩm quản trị.",
                    e
            );
        }
    }

    public Product findById(
            long productId
    ) {

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
                    productId
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
                    "Không thể tìm sản phẩm.",
                    e
            );
        }
    }

    public Product create(
            Product product
    ) {

        String sql = """
                INSERT INTO products (
                    category_id,
                    name,
                    description,
                    price,
                    image_path,
                    size,
                    color,
                    stock,
                    status
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(
                                sql,
                                Statement.RETURN_GENERATED_KEYS
                        )
        ) {

            if (product.getCategoryId() > 0) {

                statement.setLong(
                        1,
                        product.getCategoryId()
                );

            } else {

                statement.setNull(
                        1,
                        Types.BIGINT
                );
            }

            statement.setString(
                    2,
                    product.getName()
            );

            statement.setString(
                    3,
                    product.getDescription()
            );

            statement.setBigDecimal(
                    4,
                    product.getPrice()
            );

            statement.setString(
                    5,
                    product.getImageUrl()
            );

            statement.setString(
                    6,
                    product.getSize()
            );

            statement.setString(
                    7,
                    product.getColor()
            );

            statement.setInt(
                    8,
                    product.getStock()
            );

            statement.setString(
                    9,
                    product.getStatus()
            );

            int changed =
                    statement.executeUpdate();

            if (changed != 1) {

                throw new IllegalStateException(
                        "Không thể thêm sản phẩm."
                );
            }

            try (
                    ResultSet keys =
                            statement.getGeneratedKeys()
            ) {

                if (!keys.next()) {

                    throw new IllegalStateException(
                            "Không lấy được ID sản phẩm mới."
                    );
                }

                product.setId(
                        keys.getLong(1)
                );
            }

            return findById(
                    product.getId()
            );

        } catch (SQLException e) {

            throw new IllegalStateException(
                    "Không thể thêm sản phẩm.",
                    e
            );
        }
    }

    public Product update(
            Product product
    ) {

        String sql = """
                UPDATE products
                SET
                    category_id = ?,
                    name = ?,
                    description = ?,
                    price = ?,
                    image_path = ?,
                    size = ?,
                    color = ?,
                    status = ?
                WHERE id = ?
                """;

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            connection.setAutoCommit(false);
            var current = VariantStockRepository.readLocked(connection, product.getId());
            var sizes = VariantStockRepository.options(product.getSize());
            var colors = VariantStockRepository.options(product.getColor());
            if (current.variants().stream().anyMatch(
                    variant -> !sizes.contains(variant.size()) || !colors.contains(variant.color()))) {
                connection.rollback();
                throw new IllegalArgumentException("Không thể xóa size/màu đã được quản lý trong kho.");
            }

            if (product.getCategoryId() > 0) {

                statement.setLong(
                        1,
                        product.getCategoryId()
                );

            } else {

                statement.setNull(
                        1,
                        Types.BIGINT
                );
            }

            statement.setString(
                    2,
                    product.getName()
            );

            statement.setString(
                    3,
                    product.getDescription()
            );

            statement.setBigDecimal(
                    4,
                    product.getPrice()
            );

            statement.setString(
                    5,
                    product.getImageUrl()
            );

            statement.setString(
                    6,
                    product.getSize()
            );

            statement.setString(
                    7,
                    product.getColor()
            );

            statement.setString(
                    8,
                    product.getStatus()
            );

            statement.setLong(
                    9,
                    product.getId()
            );

            int changed =
                    statement.executeUpdate();

            connection.commit();

            if (changed != 1) {

                return null;
            }

            return findById(
                    product.getId()
            );

        } catch (SQLException e) {

            throw new IllegalStateException(
                    "Không thể cập nhật sản phẩm.",
                    e
            );
        }
    }

    public Product updateStatus(
            long productId,
            String status
    ) {

        String sql = """
                UPDATE products
                SET status = ?
                WHERE id = ?
                """;

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setString(
                    1,
                    status
            );

            statement.setLong(
                    2,
                    productId
            );

            int changed =
                    statement.executeUpdate();

            if (changed != 1) {
                return null;
            }

            return findById(
                    productId
            );

        } catch (SQLException e) {

            throw new IllegalStateException(
                    "Không thể cập nhật trạng thái sản phẩm.",
                    e
            );
        }
    }

    public boolean categoryExists(
            long categoryId
    ) {

        String sql = """
                SELECT 1
                FROM categories
                WHERE id = ?
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
                    categoryId
            );

            try (
                    ResultSet resultSet =
                            statement.executeQuery()
            ) {

                return resultSet.next();
            }

        } catch (SQLException e) {

            throw new IllegalStateException(
                    "Không thể kiểm tra danh mục.",
                    e
            );
        }
    }

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

        long categoryId =
                resultSet.getLong(
                        "category_id"
                );

        if (!resultSet.wasNull()) {

            product.setCategoryId(
                    categoryId
            );
        }

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
