package com.fashionshop.server.repository;

import com.fashionshop.server.database.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class InventoryRepository {

    public List<Map<String, Object>> findStockOverview() {

        String sql = """
                SELECT
                    p.id,
                    p.name,
                    p.category_id,
                    c.name AS category_name,
                    p.stock,
                    p.status
                FROM products p
                LEFT JOIN categories c
                    ON c.id = p.category_id
                ORDER BY p.stock ASC, p.id DESC
                """;

        List<Map<String, Object>> result =
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

                Map<String, Object> item =
                        new LinkedHashMap<>();

                item.put(
                        "productId",
                        resultSet.getLong("id")
                );

                item.put(
                        "productName",
                        resultSet.getString("name")
                );

                long categoryId =
                        resultSet.getLong("category_id");

                item.put(
                        "categoryId",
                        resultSet.wasNull()
                                ? null
                                : categoryId
                );

                item.put(
                        "categoryName",
                        resultSet.getString("category_name")
                );

                item.put(
                        "stock",
                        resultSet.getInt("stock")
                );

                item.put(
                        "status",
                        resultSet.getString("status")
                );

                item.put(
                        "lowStock",
                        resultSet.getInt("stock") <= 5
                );

                result.add(item);
            }

            return result;

        } catch (SQLException e) {

            throw new IllegalStateException(
                    "Không thể tải dữ liệu kho.",
                    e
            );
        }
    }

    public List<Map<String, Object>> findTransactions(
            int limit
    ) {

        String sql = """
                SELECT
                    it.id,
                    it.product_id,
                    p.name AS product_name,
                    it.user_id,
                    u.name AS user_name,
                    it.type,
                    it.quantity,
                    it.stock_before,
                    it.stock_after,
                    it.reason,
                    it.created_at
                FROM inventory_transactions it
                JOIN products p
                    ON p.id = it.product_id
                LEFT JOIN users u
                    ON u.id = it.user_id
                ORDER BY it.id DESC
                LIMIT ?
                """;

        List<Map<String, Object>> result =
                new ArrayList<>();

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(
                    1,
                    limit
            );

            try (
                    ResultSet resultSet =
                            statement.executeQuery()
            ) {

                while (resultSet.next()) {

                    Map<String, Object> item =
                            new LinkedHashMap<>();

                    item.put(
                            "id",
                            resultSet.getLong("id")
                    );

                    item.put(
                            "productId",
                            resultSet.getLong("product_id")
                    );

                    item.put(
                            "productName",
                            resultSet.getString("product_name")
                    );

                    long userId =
                            resultSet.getLong("user_id");

                    item.put(
                            "userId",
                            resultSet.wasNull()
                                    ? null
                                    : userId
                    );

                    item.put(
                            "userName",
                            resultSet.getString("user_name")
                    );

                    item.put(
                            "type",
                            resultSet.getString("type")
                    );

                    item.put(
                            "quantity",
                            resultSet.getInt("quantity")
                    );

                    item.put(
                            "stockBefore",
                            resultSet.getInt("stock_before")
                    );

                    item.put(
                            "stockAfter",
                            resultSet.getInt("stock_after")
                    );

                    item.put(
                            "reason",
                            resultSet.getString("reason")
                    );

                    Timestamp createdAt =
                            resultSet.getTimestamp("created_at");

                    item.put(
                            "createdAt",
                            createdAt == null
                                    ? null
                                    : createdAt.toLocalDateTime().toString()
                    );

                    result.add(item);
                }
            }

            return result;

        } catch (SQLException e) {

            throw new IllegalStateException(
                    "Không thể tải lịch sử kho.",
                    e
            );
        }
    }

    public Map<String, Object> adjustStock(
            long productId,
            long userId,
            String type,
            int quantity,
            String reason
    ) {

        Connection connection =
                null;

        try {

            connection =
                    DatabaseConnection.getConnection();

            connection.setAutoCommit(
                    false
            );

            String lockSql = """
                    SELECT
                        id,
                        name,
                        stock,
                        status
                    FROM products
                    WHERE id = ?
                    FOR UPDATE
                    """;

            String productName;
            int stockBefore;

            try (
                    PreparedStatement statement =
                            connection.prepareStatement(lockSql)
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

                        throw new IllegalArgumentException(
                                "Không tìm thấy sản phẩm."
                        );
                    }

                    productName =
                            resultSet.getString("name");

                    stockBefore =
                            resultSet.getInt("stock");
                }
            }

            boolean increase =
                    "IMPORT".equalsIgnoreCase(type)
                            || "ADJUST_IN".equalsIgnoreCase(type);

            int stockAfter =
                    increase
                            ? stockBefore + quantity
                            : stockBefore - quantity;

            if (stockAfter < 0) {

                throw new IllegalArgumentException(
                        "Số lượng xuất vượt quá tồn kho hiện tại."
                );
            }

            long allocated = VariantStockRepository.listLocked(connection, productId).stream()
                    .mapToLong(VariantStockRepository.Variant::stock).sum();
            if (stockAfter < allocated) {
                throw new IllegalArgumentException(
                        "Chỉ có thể xuất tối đa " + (stockBefore - allocated)
                                + " sản phẩm chưa phân bổ. Giảm phân bổ size/màu trước khi xuất kho.");
            }

            String updateSql = """
                    UPDATE products
                    SET stock = ?
                    WHERE id = ?
                    """;

            try (
                    PreparedStatement statement =
                            connection.prepareStatement(updateSql)
            ) {

                statement.setInt(
                        1,
                        stockAfter
                );

                statement.setLong(
                        2,
                        productId
                );

                if (statement.executeUpdate() != 1) {

                    throw new IllegalStateException(
                            "Không thể cập nhật tồn kho."
                    );
                }
            }

            String insertSql = """
                    INSERT INTO inventory_transactions (
                        product_id,
                        user_id,
                        type,
                        quantity,
                        stock_before,
                        stock_after,
                        reason
                    )
                    VALUES (?, ?, ?, ?, ?, ?, ?)
                    """;

            long transactionId;

            try (
                    PreparedStatement statement =
                            connection.prepareStatement(
                                    insertSql,
                                    Statement.RETURN_GENERATED_KEYS
                            )
            ) {

                statement.setLong(
                        1,
                        productId
                );

                statement.setLong(
                        2,
                        userId
                );

                statement.setString(
                        3,
                        type
                );

                statement.setInt(
                        4,
                        quantity
                );

                statement.setInt(
                        5,
                        stockBefore
                );

                statement.setInt(
                        6,
                        stockAfter
                );

                statement.setString(
                        7,
                        reason
                );

                statement.executeUpdate();

                try (
                        ResultSet keys =
                                statement.getGeneratedKeys()
                ) {

                    if (!keys.next()) {

                        throw new SQLException(
                                "Không lấy được ID giao dịch kho."
                        );
                    }

                    transactionId =
                            keys.getLong(1);
                }
            }

            connection.commit();

            Map<String, Object> result =
                    new LinkedHashMap<>();

            result.put(
                    "transactionId",
                    transactionId
            );

            result.put(
                    "productId",
                    productId
            );

            result.put(
                    "productName",
                    productName
            );

            result.put(
                    "type",
                    type
            );

            result.put(
                    "quantity",
                    quantity
            );

            result.put(
                    "stockBefore",
                    stockBefore
            );

            result.put(
                    "stockAfter",
                    stockAfter
            );

            result.put(
                    "reason",
                    reason
            );

            return result;

        } catch (Exception e) {

            if (connection != null) {

                try {

                    connection.rollback();

                } catch (SQLException ignored) {
                }
            }

            if (e instanceof IllegalArgumentException) {

                throw (IllegalArgumentException) e;
            }

            throw new IllegalStateException(
                    "Không thể điều chỉnh tồn kho.",
                    e
            );

        } finally {

            if (connection != null) {

                try {

                    connection.setAutoCommit(true);
                    connection.close();

                } catch (SQLException ignored) {
                }
            }
        }
    }
}
