package com.fashionshop.server.repository;

import com.fashionshop.server.database.DatabaseConnection;
import com.fashionshop.server.model.Order;
import com.fashionshop.server.model.OrderItem;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderRepository {

    public Order create(Order order) {

        Connection connection = null;

        try {

            connection =
                    DatabaseConnection.getConnection();

            connection.setAutoCommit(false);

            BigDecimal totalAmount =
                    BigDecimal.ZERO;

            String productSql = """
                    SELECT
                        id,
                        name,
                        price,
                        stock,
                        status
                    FROM products
                    WHERE id = ?
                    FOR UPDATE
                    """;

            try (
                    PreparedStatement statement =
                            connection.prepareStatement(
                                    productSql
                            )
            ) {

                for (OrderItem item :
                        order.getItems()) {

                    statement.setLong(
                            1,
                            item.getProductId()
                    );

                    try (
                            ResultSet resultSet =
                                    statement.executeQuery()
                    ) {

                        if (!resultSet.next()) {

                            throw new IllegalArgumentException(
                                    "Không tìm thấy sản phẩm ID="
                                            + item.getProductId()
                            );
                        }

                        String status =
                                resultSet.getString(
                                        "status"
                                );

                        if (!"ACTIVE".equalsIgnoreCase(
                                status
                        )) {

                            throw new IllegalArgumentException(
                                    "Sản phẩm ID="
                                            + item.getProductId()
                                            + " hiện không hoạt động."
                            );
                        }

                        int stock =
                                resultSet.getInt(
                                        "stock"
                                );

                        if (item.getQuantity() <= 0) {

                            throw new IllegalArgumentException(
                                    "Số lượng sản phẩm phải lớn hơn 0."
                            );
                        }

                        if (stock < item.getQuantity()) {

                            throw new IllegalArgumentException(
                                    "Sản phẩm "
                                            + resultSet.getString(
                                            "name"
                                    )
                                            + " không đủ tồn kho."
                            );
                        }

                        BigDecimal price =
                                resultSet.getBigDecimal(
                                        "price"
                                );

                        item.setPrice(price);

                        item.setProductName(
                                resultSet.getString(
                                        "name"
                                )
                        );

                        BigDecimal subtotal =
                                price.multiply(
                                        BigDecimal.valueOf(
                                                item.getQuantity()
                                        )
                                );

                        totalAmount =
                                totalAmount.add(
                                        subtotal
                                );
                    }
                }
            }

            order.setTotalAmount(
                    totalAmount
            );

            String orderSql = """
                    INSERT INTO orders
                    (
                        user_id,
                        total_amount,
                        receiver_name,
                        phone,
                        address,
                        note,
                        payment_method,
                        status
                    )
                    VALUES
                    (?, ?, ?, ?, ?, ?, ?, 'PENDING')
                    """;

            long orderId;

            try (
                    PreparedStatement statement =
                            connection.prepareStatement(
                                    orderSql,
                                    Statement.RETURN_GENERATED_KEYS
                            )
            ) {

                statement.setLong(
                        1,
                        order.getUserId()
                );

                statement.setBigDecimal(
                        2,
                        totalAmount
                );

                statement.setString(
                        3,
                        order.getReceiverName()
                );

                statement.setString(
                        4,
                        order.getPhone()
                );

                statement.setString(
                        5,
                        order.getAddress()
                );

                statement.setString(
                        6,
                        order.getNote()
                );

                statement.setString(
                        7,
                        order.getPaymentMethod()
                );

                statement.executeUpdate();

                try (
                        ResultSet keys =
                                statement.getGeneratedKeys()
                ) {

                    if (!keys.next()) {

                        throw new SQLException(
                                "Không lấy được ID đơn hàng."
                        );
                    }

                    orderId =
                            keys.getLong(1);
                }
            }

            String itemSql = """
                    INSERT INTO order_items
                    (
                        order_id,
                        product_id,
                        quantity,
                        price
                    )
                    VALUES (?, ?, ?, ?)
                    """;

            try (
                    PreparedStatement statement =
                            connection.prepareStatement(
                                    itemSql
                            )
            ) {

                for (OrderItem item :
                        order.getItems()) {

                    statement.setLong(
                            1,
                            orderId
                    );

                    statement.setLong(
                            2,
                            item.getProductId()
                    );

                    statement.setInt(
                            3,
                            item.getQuantity()
                    );

                    statement.setBigDecimal(
                            4,
                            item.getPrice()
                    );

                    statement.addBatch();
                }

                statement.executeBatch();
            }

            String stockSql = """
                    UPDATE products
                    SET stock = stock - ?
                    WHERE id = ?
                      AND stock >= ?
                    """;

            try (
                    PreparedStatement statement =
                            connection.prepareStatement(
                                    stockSql
                            )
            ) {

                for (OrderItem item :
                        order.getItems()) {

                    statement.setInt(
                            1,
                            item.getQuantity()
                    );

                    statement.setLong(
                            2,
                            item.getProductId()
                    );

                    statement.setInt(
                            3,
                            item.getQuantity()
                    );

                    int changed =
                            statement.executeUpdate();

                    if (changed != 1) {

                        throw new IllegalArgumentException(
                                "Không đủ tồn kho cho sản phẩm ID="
                                        + item.getProductId()
                        );
                    }

                    int stockAfter =
                            getCurrentStock(
                                    connection,
                                    item.getProductId()
                            );

                    int stockBefore =
                            stockAfter
                                    + item.getQuantity();

                    insertInventoryTransaction(
                            connection,
                            item.getProductId(),
                            order.getUserId(),
                            "SALE",
                            item.getQuantity(),
                            stockBefore,
                            stockAfter,
                            "Xuất kho cho Order #"
                                    + orderId
                    );
                }
            }

            connection.commit();

            order.setId(orderId);
            order.setStatus("PENDING");

            return order;

        } catch (Exception e) {

            rollbackQuietly(connection);

            if (e instanceof IllegalArgumentException) {

                throw (IllegalArgumentException) e;
            }

            throw new IllegalStateException(
                    "Không thể tạo đơn hàng.",
                    e
            );

        } finally {

            closeQuietly(connection);
        }
    }

    public List<Order> findByUserId(
            long userId
    ) {

        String sql = """
                SELECT
                    id,
                    user_id,
                    total_amount,
                    receiver_name,
                    phone,
                    address,
                    note,
                    payment_method,
                    status
                FROM orders
                WHERE user_id = ?
                ORDER BY id DESC
                """;

        List<Order> orders =
                new ArrayList<>();

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setLong(
                    1,
                    userId
            );

            try (
                    ResultSet resultSet =
                            statement.executeQuery()
            ) {

                while (resultSet.next()) {

                    orders.add(
                            mapOrder(resultSet)
                    );
                }
            }

            return orders;

        } catch (SQLException e) {

            throw new IllegalStateException(
                    "Không thể lấy lịch sử đơn hàng.",
                    e
            );
        }
    }

    public Order findByIdAndUserId(
            long orderId,
            long userId
    ) {

        String orderSql = """
                SELECT
                    id,
                    user_id,
                    total_amount,
                    receiver_name,
                    phone,
                    address,
                    note,
                    payment_method,
                    status
                FROM orders
                WHERE id = ?
                  AND user_id = ?
                LIMIT 1
                """;

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(
                                orderSql
                        )
        ) {

            statement.setLong(
                    1,
                    orderId
            );

            statement.setLong(
                    2,
                    userId
            );

            Order order;

            try (
                    ResultSet resultSet =
                            statement.executeQuery()
            ) {

                if (!resultSet.next()) {
                    return null;
                }

                order =
                        mapOrder(resultSet);
            }

            String itemSql = """
                    SELECT
                        oi.product_id,
                        p.name AS product_name,
                        oi.quantity,
                        oi.price
                    FROM order_items oi
                    LEFT JOIN products p
                        ON p.id = oi.product_id
                    WHERE oi.order_id = ?
                    ORDER BY oi.id
                    """;

            List<OrderItem> items =
                    new ArrayList<>();

            try (
                    PreparedStatement itemStatement =
                            connection.prepareStatement(
                                    itemSql
                            )
            ) {

                itemStatement.setLong(
                        1,
                        orderId
                );

                try (
                        ResultSet resultSet =
                                itemStatement.executeQuery()
                ) {

                    while (resultSet.next()) {

                        OrderItem item =
                                new OrderItem();

                        item.setProductId(
                                resultSet.getLong(
                                        "product_id"
                                )
                        );

                        item.setProductName(
                                resultSet.getString(
                                        "product_name"
                                )
                        );

                        item.setQuantity(
                                resultSet.getInt(
                                        "quantity"
                                )
                        );

                        item.setPrice(
                                resultSet.getBigDecimal(
                                        "price"
                                )
                        );

                        items.add(item);
                    }
                }
            }

            order.setItems(items);

            return order;

        } catch (SQLException e) {

            throw new IllegalStateException(
                    "Không thể lấy chi tiết đơn hàng.",
                    e
            );
        }
    }

    public Order cancel(
            long orderId,
            long userId
    ) {

        Connection connection = null;

        try {

            connection =
                    DatabaseConnection.getConnection();

            connection.setAutoCommit(false);

            String orderSql = """
                    SELECT
                        id,
                        user_id,
                        total_amount,
                        receiver_name,
                        phone,
                        address,
                        note,
                        payment_method,
                        status
                    FROM orders
                    WHERE id = ?
                      AND user_id = ?
                    FOR UPDATE
                    """;

            Order order;

            try (
                    PreparedStatement statement =
                            connection.prepareStatement(
                                    orderSql
                            )
            ) {

                statement.setLong(
                        1,
                        orderId
                );

                statement.setLong(
                        2,
                        userId
                );

                try (
                        ResultSet resultSet =
                                statement.executeQuery()
                ) {

                    if (!resultSet.next()) {

                        throw new IllegalArgumentException(
                                "Không tìm thấy đơn hàng."
                        );
                    }

                    order =
                            mapOrder(resultSet);
                }
            }

            if (!"PENDING".equalsIgnoreCase(
                    order.getStatus()
            )) {

                throw new IllegalArgumentException(
                        "Chỉ có thể hủy đơn đang ở trạng thái PENDING."
                );
            }

            String itemSql = """
                    SELECT
                        product_id,
                        quantity,
                        price
                    FROM order_items
                    WHERE order_id = ?
                    """;

            List<OrderItem> items =
                    new ArrayList<>();

            try (
                    PreparedStatement statement =
                            connection.prepareStatement(
                                    itemSql
                            )
            ) {

                statement.setLong(
                        1,
                        orderId
                );

                try (
                        ResultSet resultSet =
                                statement.executeQuery()
                ) {

                    while (resultSet.next()) {

                        OrderItem item =
                                new OrderItem();

                        item.setProductId(
                                resultSet.getLong(
                                        "product_id"
                                )
                        );

                        item.setQuantity(
                                resultSet.getInt(
                                        "quantity"
                                )
                        );

                        item.setPrice(
                                resultSet.getBigDecimal(
                                        "price"
                                )
                        );

                        items.add(item);
                    }
                }
            }

            String restoreSql = """
                    UPDATE products
                    SET stock = stock + ?
                    WHERE id = ?
                    """;

            try (
                    PreparedStatement statement =
                            connection.prepareStatement(
                                    restoreSql
                            )
            ) {

                for (OrderItem item : items) {

                    statement.setInt(
                            1,
                            item.getQuantity()
                    );

                    statement.setLong(
                            2,
                            item.getProductId()
                    );

                    int changed =
                            statement.executeUpdate();

                    if (changed != 1) {

                        throw new SQLException(
                                "Không thể hoàn tồn kho sản phẩm ID="
                                        + item.getProductId()
                        );
                    }

                    int stockAfter =
                            getCurrentStock(
                                    connection,
                                    item.getProductId()
                            );

                    int stockBefore =
                            stockAfter
                                    - item.getQuantity();

                    insertInventoryTransaction(
                            connection,
                            item.getProductId(),
                            userId,
                            "ORDER_CANCEL",
                            item.getQuantity(),
                            stockBefore,
                            stockAfter,
                            "Hoàn tồn do hủy Order #"
                                    + orderId
                    );
                }
            }

            String cancelSql = """
                    UPDATE orders
                    SET status = 'CANCELLED'
                    WHERE id = ?
                      AND user_id = ?
                      AND status = 'PENDING'
                    """;

            try (
                    PreparedStatement statement =
                            connection.prepareStatement(
                                    cancelSql
                            )
            ) {

                statement.setLong(
                        1,
                        orderId
                );

                statement.setLong(
                        2,
                        userId
                );

                int changed =
                        statement.executeUpdate();

                if (changed != 1) {

                    throw new IllegalStateException(
                            "Không thể cập nhật trạng thái đơn hàng."
                    );
                }
            }

            connection.commit();

            order.setStatus("CANCELLED");
            order.setItems(items);

            return order;

        } catch (Exception e) {

            rollbackQuietly(connection);

            if (e instanceof IllegalArgumentException) {

                throw (IllegalArgumentException) e;
            }

            throw new IllegalStateException(
                    "Không thể hủy đơn hàng.",
                    e
            );

        } finally {

            closeQuietly(connection);
        }
    }

    private Order mapOrder(
            ResultSet resultSet
    ) throws SQLException {

        Order order =
                new Order();

        order.setId(
                resultSet.getLong("id")
        );

        order.setUserId(
                resultSet.getLong("user_id")
        );

        order.setTotalAmount(
                resultSet.getBigDecimal(
                        "total_amount"
                )
        );

        order.setReceiverName(
                resultSet.getString(
                        "receiver_name"
                )
        );

        order.setPhone(
                resultSet.getString(
                        "phone"
                )
        );

        order.setAddress(
                resultSet.getString(
                        "address"
                )
        );

        order.setNote(
                resultSet.getString(
                        "note"
                )
        );

        order.setPaymentMethod(
                resultSet.getString(
                        "payment_method"
                )
        );

        order.setStatus(
                resultSet.getString(
                        "status"
                )
        );

        return order;
    }

    private int getCurrentStock(
            Connection connection,
            long productId
    ) throws SQLException {

        String sql = """
                SELECT stock
                FROM products
                WHERE id = ?
                LIMIT 1
                """;

        try (
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

                    throw new SQLException(
                            "Không tìm thấy sản phẩm ID="
                                    + productId
                    );
                }

                return resultSet.getInt(
                        "stock"
                );
            }
        }
    }

    private void insertInventoryTransaction(
            Connection connection,
            long productId,
            long userId,
            String type,
            int quantity,
            int stockBefore,
            int stockAfter,
            String reason
    ) throws SQLException {

        String sql = """
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

        try (
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setLong(
                    1,
                    productId
            );

            if (userId > 0) {

                statement.setLong(
                        2,
                        userId
                );

            } else {

                statement.setNull(
                        2,
                        Types.BIGINT
                );
            }

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

            if (
                    statement.executeUpdate()
                            != 1
            ) {

                throw new SQLException(
                        "Không thể ghi lịch sử giao dịch kho."
                );
            }
        }
    }
    private void rollbackQuietly(
            Connection connection
    ) {

        if (connection == null) {
            return;
        }

        try {
            connection.rollback();
        } catch (SQLException ignored) {
        }
    }

    private void closeQuietly(
            Connection connection
    ) {

        if (connection == null) {
            return;
        }

        try {

            connection.setAutoCommit(true);

        } catch (SQLException ignored) {
        }

        try {

            connection.close();

        } catch (SQLException ignored) {
        }
    }
}