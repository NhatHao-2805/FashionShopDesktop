package com.fashionshop.server.repository;

import com.fashionshop.server.database.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AdminRepository {

    public Map<String, Object> getDashboardStats() {

        Map<String, Object> stats =
                new LinkedHashMap<>();

        try (
                Connection connection =
                        DatabaseConnection.getConnection()
        ) {

            stats.put(
                    "totalCustomers",
                    queryLong(
                            connection,
                            """
                            SELECT COUNT(*)
                            FROM users
                            WHERE role = 'CUSTOMER'
                            """
                    )
            );

            stats.put(
                    "totalProducts",
                    queryLong(
                            connection,
                            """
                            SELECT COUNT(*)
                            FROM products
                            """
                    )
            );

            stats.put(
                    "totalStock",
                    queryLong(
                            connection,
                            """
                            SELECT COALESCE(SUM(stock), 0)
                            FROM products
                            """
                    )
            );

            stats.put(
                    "lowStockProducts",
                    queryLong(
                            connection,
                            """
                            SELECT COUNT(*)
                            FROM products
                            WHERE stock <= 5
                            """
                    )
            );

            stats.put(
                    "totalOrders",
                    queryLong(
                            connection,
                            """
                            SELECT COUNT(*)
                            FROM orders
                            """
                    )
            );

            stats.put(
                    "pendingOrders",
                    countOrdersByStatus(
                            connection,
                            "PENDING"
                    )
            );

            stats.put(
                    "processingOrders",
                    countOrdersByStatus(
                            connection,
                            "PROCESSING"
                    )
            );

            stats.put(
                    "shippingOrders",
                    countOrdersByStatus(
                            connection,
                            "SHIPPING"
                    )
            );

            stats.put(
                    "deliveredOrders",
                    countOrdersByStatus(
                            connection,
                            "DELIVERED"
                    )
            );

            stats.put(
                    "cancelledOrders",
                    countOrdersByStatus(
                            connection,
                            "CANCELLED"
                    )
            );

            stats.put(
                    "deliveredRevenue",
                    queryBigDecimal(
                            connection,
                            """
                            SELECT COALESCE(
                                SUM(total_amount),
                                0
                            )
                            FROM orders
                            WHERE status = 'DELIVERED'
                            """
                    )
            );

            return stats;

        } catch (SQLException e) {

            throw new IllegalStateException(
                    "Không thể tải thống kê admin.",
                    e
            );
        }
    }

    public List<Map<String, Object>>
    findAllOrders() {

        String sql = """
                SELECT
                    o.id,
                    o.user_id,
                    u.name AS customer_name,
                    u.email AS customer_email,
                    o.total_amount,
                    o.receiver_name,
                    o.phone,
                    o.address,
                    o.note,
                    o.payment_method,
                    o.status
                FROM orders o
                INNER JOIN users u
                    ON u.id = o.user_id
                ORDER BY o.id DESC
                """;

        List<Map<String, Object>> orders =
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

                orders.add(
                        mapOrder(resultSet)
                );
            }

            return orders;

        } catch (SQLException e) {

            throw new IllegalStateException(
                    "Không thể lấy danh sách đơn hàng.",
                    e
            );
        }
    }

    public Map<String, Object>
    findOrderById(long orderId) {

        String sql = """
                SELECT
                    o.id,
                    o.user_id,
                    u.name AS customer_name,
                    u.email AS customer_email,
                    o.total_amount,
                    o.receiver_name,
                    o.phone,
                    o.address,
                    o.note,
                    o.payment_method,
                    o.status
                FROM orders o
                INNER JOIN users u
                    ON u.id = o.user_id
                WHERE o.id = ?
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
                    orderId
            );

            try (
                    ResultSet resultSet =
                            statement.executeQuery()
            ) {

                if (!resultSet.next()) {
                    return null;
                }

                return mapOrder(
                        resultSet
                );
            }

        } catch (SQLException e) {

            throw new IllegalStateException(
                    "Không thể lấy đơn hàng.",
                    e
            );
        }
    }

    public String findOrderStatus(
            long orderId
    ) {

        String sql = """
                SELECT status
                FROM orders
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
                    orderId
            );

            try (
                    ResultSet resultSet =
                            statement.executeQuery()
            ) {

                if (!resultSet.next()) {
                    return null;
                }

                return resultSet.getString(
                        "status"
                );
            }

        } catch (SQLException e) {

            throw new IllegalStateException(
                    "Không thể đọc trạng thái đơn hàng.",
                    e
            );
        }
    }

    public boolean updateOrderStatus(
            long orderId,
            String currentStatus,
            String newStatus
    ) {

        String sql = """
                UPDATE orders
                SET status = ?
                WHERE id = ?
                  AND status = ?
                """;

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setString(
                    1,
                    newStatus
            );

            statement.setLong(
                    2,
                    orderId
            );

            statement.setString(
                    3,
                    currentStatus
            );

            return statement.executeUpdate()
                    == 1;

        } catch (SQLException e) {

            throw new IllegalStateException(
                    "Không thể cập nhật trạng thái đơn hàng.",
                    e
            );
        }
    }

    public List<Map<String, Object>>
    findCustomers() {

        String sql = """
                SELECT
                    id,
                    name,
                    email,
                    phone,
                    status
                FROM users
                WHERE role = 'CUSTOMER'
                ORDER BY id DESC
                """;

        List<Map<String, Object>> customers =
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

                Map<String, Object> customer =
                        new LinkedHashMap<>();

                customer.put(
                        "id",
                        resultSet.getLong(
                                "id"
                        )
                );

                customer.put(
                        "name",
                        resultSet.getString(
                                "name"
                        )
                );

                customer.put(
                        "email",
                        resultSet.getString(
                                "email"
                        )
                );

                customer.put(
                        "phone",
                        resultSet.getString(
                                "phone"
                        )
                );

                customer.put(
                        "status",
                        resultSet.getString(
                                "status"
                        )
                );

                customers.add(
                        customer
                );
            }

            return customers;

        } catch (SQLException e) {

            throw new IllegalStateException(
                    "Không thể lấy danh sách khách hàng.",
                    e
            );
        }
    }

    private long countOrdersByStatus(
            Connection connection,
            String status
    ) throws SQLException {

        String sql = """
                SELECT COUNT(*)
                FROM orders
                WHERE status = ?
                """;

        try (
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setString(
                    1,
                    status
            );

            try (
                    ResultSet resultSet =
                            statement.executeQuery()
            ) {

                resultSet.next();

                return resultSet.getLong(1);
            }
        }
    }

    private long queryLong(
            Connection connection,
            String sql
    ) throws SQLException {

        try (
                PreparedStatement statement =
                        connection.prepareStatement(sql);

                ResultSet resultSet =
                        statement.executeQuery()
        ) {

            resultSet.next();

            return resultSet.getLong(1);
        }
    }

    private BigDecimal queryBigDecimal(
            Connection connection,
            String sql
    ) throws SQLException {

        try (
                PreparedStatement statement =
                        connection.prepareStatement(sql);

                ResultSet resultSet =
                        statement.executeQuery()
        ) {

            resultSet.next();

            BigDecimal value =
                    resultSet.getBigDecimal(1);

            return value == null
                    ? BigDecimal.ZERO
                    : value;
        }
    }

    private Map<String, Object> mapOrder(
            ResultSet resultSet
    ) throws SQLException {

        Map<String, Object> order =
                new LinkedHashMap<>();

        order.put(
                "id",
                resultSet.getLong(
                        "id"
                )
        );

        order.put(
                "userId",
                resultSet.getLong(
                        "user_id"
                )
        );

        order.put(
                "customerName",
                resultSet.getString(
                        "customer_name"
                )
        );

        order.put(
                "customerEmail",
                resultSet.getString(
                        "customer_email"
                )
        );

        order.put(
                "totalAmount",
                resultSet.getBigDecimal(
                        "total_amount"
                )
        );

        order.put(
                "receiverName",
                resultSet.getString(
                        "receiver_name"
                )
        );

        order.put(
                "phone",
                resultSet.getString(
                        "phone"
                )
        );

        order.put(
                "address",
                resultSet.getString(
                        "address"
                )
        );

        order.put(
                "note",
                resultSet.getString(
                        "note"
                )
        );

        order.put(
                "paymentMethod",
                resultSet.getString(
                        "payment_method"
                )
        );

        order.put(
                "status",
                resultSet.getString(
                        "status"
                )
        );

        return order;
    }
}