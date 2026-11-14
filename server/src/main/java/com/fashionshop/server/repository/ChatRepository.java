package com.fashionshop.server.repository;

import com.fashionshop.server.database.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ChatRepository {

    public Map<String, Object> getOrCreateCustomerConversation(
            long customerId
    ) {

        try (
                Connection connection =
                        DatabaseConnection.getConnection()
        ) {

            return getOrCreateCustomerConversation(
                    connection,
                    customerId
            );

        } catch (SQLException e) {

            throw new IllegalStateException(
                    "Không thể mở cuộc trò chuyện.",
                    e
            );
        }
    }

    public List<Map<String, Object>> findCustomerMessages(
            long customerId
    ) {

        try (
                Connection connection =
                        DatabaseConnection.getConnection()
        ) {

            Map<String, Object> conversation =
                    getOrCreateCustomerConversation(
                            connection,
                            customerId
                    );

            long conversationId =
                    ((Number) conversation.get("id"))
                            .longValue();

            markRead(
                    connection,
                    conversationId,
                    customerId
            );

            return findMessages(
                    connection,
                    conversationId
            );

        } catch (SQLException e) {

            throw new IllegalStateException(
                    "Không thể tải tin nhắn.",
                    e
            );
        }
    }

    public Map<String, Object> sendCustomerMessage(
            long customerId,
            String text
    ) {

        Connection connection =
                null;

        try {

            connection =
                    DatabaseConnection.getConnection();

            connection.setAutoCommit(false);

            Map<String, Object> conversation =
                    getOrCreateCustomerConversation(
                            connection,
                            customerId
                    );

            long conversationId =
                    ((Number) conversation.get("id"))
                            .longValue();

            long adminId =
                    ((Number) conversation.get("adminId"))
                            .longValue();

            Map<String, Object> message =
                    insertMessage(
                            connection,
                            conversationId,
                            customerId,
                            adminId,
                            text
                    );

            touchConversation(
                    connection,
                    conversationId
            );

            connection.commit();

            return message;

        } catch (Exception e) {

            rollbackQuietly(
                    connection
            );

            throw new IllegalStateException(
                    "Không thể gửi tin nhắn.",
                    e
            );

        } finally {

            closeQuietly(
                    connection
            );
        }
    }

    public List<Map<String, Object>> findAdminConversations(
            long adminId
    ) {

        String sql = """
                SELECT
                    c.id,
                    c.customer_id,
                    customer.name AS customer_name,
                    customer.email AS customer_email,
                    c.admin_id,
                    admin.name AS admin_name,
                    c.created_at,
                    c.updated_at,

                    (
                        SELECT m.message
                        FROM messages m
                        WHERE m.conversation_id = c.id
                        ORDER BY m.id DESC
                        LIMIT 1
                    ) AS last_message,

                    (
                        SELECT m.created_at
                        FROM messages m
                        WHERE m.conversation_id = c.id
                        ORDER BY m.id DESC
                        LIMIT 1
                    ) AS last_message_at,

                    (
                        SELECT COUNT(*)
                        FROM messages m
                        WHERE m.conversation_id = c.id
                          AND m.receiver_id = ?
                          AND m.is_read = FALSE
                    ) AS unread_count

                FROM conversations c

                JOIN users customer
                    ON customer.id = c.customer_id

                LEFT JOIN users admin
                    ON admin.id = c.admin_id

                WHERE c.admin_id = ?

                ORDER BY
                    COALESCE(
                        last_message_at,
                        c.updated_at
                    ) DESC,
                    c.id DESC
                """;

        List<Map<String, Object>> result =
                new ArrayList<>();

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setLong(
                    1,
                    adminId
            );

            statement.setLong(
                    2,
                    adminId
            );

            try (
                    ResultSet rs =
                            statement.executeQuery()
            ) {

                while (rs.next()) {

                    Map<String, Object> item =
                            new LinkedHashMap<>();

                    item.put(
                            "id",
                            rs.getLong("id")
                    );

                    item.put(
                            "customerId",
                            rs.getLong("customer_id")
                    );

                    item.put(
                            "customerName",
                            rs.getString("customer_name")
                    );

                    item.put(
                            "customerEmail",
                            rs.getString("customer_email")
                    );

                    item.put(
                            "adminId",
                            rs.getLong("admin_id")
                    );

                    item.put(
                            "adminName",
                            rs.getString("admin_name")
                    );

                    item.put(
                            "lastMessage",
                            rs.getString("last_message")
                    );

                    item.put(
                            "unreadCount",
                            rs.getInt("unread_count")
                    );

                    Timestamp lastMessageAt =
                            rs.getTimestamp(
                                    "last_message_at"
                            );

                    item.put(
                            "lastMessageAt",
                            lastMessageAt == null
                                    ? null
                                    : lastMessageAt
                                    .toLocalDateTime()
                                    .toString()
                    );

                    Timestamp updatedAt =
                            rs.getTimestamp(
                                    "updated_at"
                            );

                    item.put(
                            "updatedAt",
                            updatedAt == null
                                    ? null
                                    : updatedAt
                                    .toLocalDateTime()
                                    .toString()
                    );

                    result.add(item);
                }
            }

            return result;

        } catch (SQLException e) {

            throw new IllegalStateException(
                    "Không thể tải danh sách hội thoại.",
                    e
            );
        }
    }

    public List<Map<String, Object>> findAdminMessages(
            long adminId,
            long conversationId
    ) {

        try (
                Connection connection =
                        DatabaseConnection.getConnection()
        ) {

            ensureAdminOwnsConversation(
                    connection,
                    adminId,
                    conversationId
            );

            markRead(
                    connection,
                    conversationId,
                    adminId
            );

            return findMessages(
                    connection,
                    conversationId
            );

        } catch (SQLException e) {

            throw new IllegalStateException(
                    "Không thể tải tin nhắn.",
                    e
            );
        }
    }

    public Map<String, Object> sendAdminMessage(
            long adminId,
            long conversationId,
            String text
    ) {

        Connection connection =
                null;

        try {

            connection =
                    DatabaseConnection.getConnection();

            connection.setAutoCommit(false);

            long customerId =
                    getCustomerIdForAdminConversation(
                            connection,
                            adminId,
                            conversationId
                    );

            Map<String, Object> result =
                    insertMessage(
                            connection,
                            conversationId,
                            adminId,
                            customerId,
                            text
                    );

            touchConversation(
                    connection,
                    conversationId
            );

            connection.commit();

            return result;

        } catch (Exception e) {

            rollbackQuietly(
                    connection
            );

            if (e instanceof IllegalArgumentException) {

                throw (IllegalArgumentException) e;
            }

            throw new IllegalStateException(
                    "Không thể gửi tin nhắn.",
                    e
            );

        } finally {

            closeQuietly(
                    connection
            );
        }
    }

    public int countUnreadForUser(
            long userId
    ) {

        String sql = """
                SELECT COUNT(*)
                FROM messages
                WHERE receiver_id = ?
                  AND is_read = FALSE
                """;

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
                    ResultSet rs =
                            statement.executeQuery()
            ) {

                rs.next();

                return rs.getInt(1);
            }

        } catch (SQLException e) {

            throw new IllegalStateException(
                    "Không thể đếm tin nhắn chưa đọc.",
                    e
            );
        }
    }

    private Map<String, Object> getOrCreateCustomerConversation(
            Connection connection,
            long customerId
    ) throws SQLException {

        String findSql = """
                SELECT
                    c.id,
                    c.customer_id,
                    c.admin_id,
                    a.name AS admin_name
                FROM conversations c
                LEFT JOIN users a
                    ON a.id = c.admin_id
                WHERE c.customer_id = ?
                ORDER BY c.id DESC
                LIMIT 1
                """;

        try (
                PreparedStatement statement =
                        connection.prepareStatement(
                                findSql
                        )
        ) {

            statement.setLong(
                    1,
                    customerId
            );

            try (
                    ResultSet rs =
                            statement.executeQuery()
            ) {

                if (rs.next()) {

                    if (rs.getObject(
                            "admin_id"
                    ) == null) {

                        long adminId =
                                findAvailableAdmin(
                                        connection
                                );

                        assignAdmin(
                                connection,
                                rs.getLong("id"),
                                adminId
                        );

                        return conversationMap(
                                rs.getLong("id"),
                                customerId,
                                adminId,
                                findUserName(
                                        connection,
                                        adminId
                                )
                        );
                    }

                    return conversationMap(
                            rs.getLong("id"),
                            customerId,
                            rs.getLong("admin_id"),
                            rs.getString("admin_name")
                    );
                }
            }
        }

        long adminId =
                findAvailableAdmin(
                        connection
                );

        String insertSql = """
                INSERT INTO conversations (
                    customer_id,
                    admin_id
                )
                VALUES (?, ?)
                """;

        try (
                PreparedStatement statement =
                        connection.prepareStatement(
                                insertSql,
                                Statement.RETURN_GENERATED_KEYS
                        )
        ) {

            statement.setLong(
                    1,
                    customerId
            );

            statement.setLong(
                    2,
                    adminId
            );

            statement.executeUpdate();

            try (
                    ResultSet keys =
                            statement.getGeneratedKeys()
            ) {

                if (!keys.next()) {

                    throw new SQLException(
                            "Không lấy được ID hội thoại."
                    );
                }

                long conversationId =
                        keys.getLong(1);

                return conversationMap(
                        conversationId,
                        customerId,
                        adminId,
                        findUserName(
                                connection,
                                adminId
                        )
                );
            }
        }
    }

    private long findAvailableAdmin(
            Connection connection
    ) throws SQLException {

        String sql = """
                SELECT id
                FROM users
                WHERE role = 'ADMIN'
                  AND status = 'ACTIVE'
                ORDER BY id
                LIMIT 1
                """;

        try (
                PreparedStatement statement =
                        connection.prepareStatement(sql);

                ResultSet rs =
                        statement.executeQuery()
        ) {

            if (!rs.next()) {

                throw new IllegalStateException(
                        "Hiện chưa có admin hoạt động để hỗ trợ."
                );
            }

            return rs.getLong(
                    "id"
            );
        }
    }

    private void assignAdmin(
            Connection connection,
            long conversationId,
            long adminId
    ) throws SQLException {

        String sql = """
                UPDATE conversations
                SET admin_id = ?
                WHERE id = ?
                """;

        try (
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setLong(
                    1,
                    adminId
            );

            statement.setLong(
                    2,
                    conversationId
            );

            statement.executeUpdate();
        }
    }

    private List<Map<String, Object>> findMessages(
            Connection connection,
            long conversationId
    ) throws SQLException {

        String sql = """
                SELECT
                    m.id,
                    m.conversation_id,
                    m.sender_id,
                    sender.name AS sender_name,
                    m.receiver_id,
                    receiver.name AS receiver_name,
                    m.message,
                    m.message_type,
                    m.is_read,
                    m.created_at
                FROM messages m

                JOIN users sender
                    ON sender.id = m.sender_id

                JOIN users receiver
                    ON receiver.id = m.receiver_id

                WHERE m.conversation_id = ?

                ORDER BY m.id
                """;

        List<Map<String, Object>> result =
                new ArrayList<>();

        try (
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setLong(
                    1,
                    conversationId
            );

            try (
                    ResultSet rs =
                            statement.executeQuery()
            ) {

                while (rs.next()) {

                    result.add(
                            messageMap(
                                    rs
                            )
                    );
                }
            }
        }

        return result;
    }

    private Map<String, Object> insertMessage(
            Connection connection,
            long conversationId,
            long senderId,
            long receiverId,
            String text
    ) throws SQLException {

        String sql = """
                INSERT INTO messages (
                    conversation_id,
                    sender_id,
                    receiver_id,
                    message,
                    message_type,
                    is_read
                )
                VALUES (?, ?, ?, ?, 'TEXT', FALSE)
                """;

        long messageId;

        try (
                PreparedStatement statement =
                        connection.prepareStatement(
                                sql,
                                Statement.RETURN_GENERATED_KEYS
                        )
        ) {

            statement.setLong(
                    1,
                    conversationId
            );

            statement.setLong(
                    2,
                    senderId
            );

            statement.setLong(
                    3,
                    receiverId
            );

            statement.setString(
                    4,
                    text
            );

            statement.executeUpdate();

            try (
                    ResultSet keys =
                            statement.getGeneratedKeys()
            ) {

                if (!keys.next()) {

                    throw new SQLException(
                            "Không lấy được ID tin nhắn."
                    );
                }

                messageId =
                        keys.getLong(1);
            }
        }

        String findSql = """
                SELECT
                    m.id,
                    m.conversation_id,
                    m.sender_id,
                    sender.name AS sender_name,
                    m.receiver_id,
                    receiver.name AS receiver_name,
                    m.message,
                    m.message_type,
                    m.is_read,
                    m.created_at
                FROM messages m

                JOIN users sender
                    ON sender.id = m.sender_id

                JOIN users receiver
                    ON receiver.id = m.receiver_id

                WHERE m.id = ?
                """;

        try (
                PreparedStatement statement =
                        connection.prepareStatement(
                                findSql
                        )
        ) {

            statement.setLong(
                    1,
                    messageId
            );

            try (
                    ResultSet rs =
                            statement.executeQuery()
            ) {

                if (!rs.next()) {

                    throw new SQLException(
                            "Không tải được tin nhắn vừa gửi."
                    );
                }

                return messageMap(
                        rs
                );
            }
        }
    }

    private Map<String, Object> messageMap(
            ResultSet rs
    ) throws SQLException {

        Map<String, Object> item =
                new LinkedHashMap<>();

        item.put(
                "id",
                rs.getLong("id")
        );

        item.put(
                "conversationId",
                rs.getLong("conversation_id")
        );

        item.put(
                "senderId",
                rs.getLong("sender_id")
        );

        item.put(
                "senderName",
                rs.getString("sender_name")
        );

        item.put(
                "receiverId",
                rs.getLong("receiver_id")
        );

        item.put(
                "receiverName",
                rs.getString("receiver_name")
        );

        item.put(
                "message",
                rs.getString("message")
        );

        item.put(
                "messageType",
                rs.getString("message_type")
        );

        item.put(
                "read",
                rs.getBoolean("is_read")
        );

        Timestamp createdAt =
                rs.getTimestamp(
                        "created_at"
                );

        item.put(
                "createdAt",
                createdAt == null
                        ? null
                        : createdAt
                        .toLocalDateTime()
                        .toString()
        );

        return item;
    }

    private void markRead(
            Connection connection,
            long conversationId,
            long receiverId
    ) throws SQLException {

        String sql = """
                UPDATE messages
                SET is_read = TRUE
                WHERE conversation_id = ?
                  AND receiver_id = ?
                  AND is_read = FALSE
                """;

        try (
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setLong(
                    1,
                    conversationId
            );

            statement.setLong(
                    2,
                    receiverId
            );

            statement.executeUpdate();
        }
    }

    private void touchConversation(
            Connection connection,
            long conversationId
    ) throws SQLException {

        String sql = """
                UPDATE conversations
                SET updated_at = CURRENT_TIMESTAMP
                WHERE id = ?
                """;

        try (
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setLong(
                    1,
                    conversationId
            );

            statement.executeUpdate();
        }
    }

    private void ensureAdminOwnsConversation(
            Connection connection,
            long adminId,
            long conversationId
    ) throws SQLException {

        getCustomerIdForAdminConversation(
                connection,
                adminId,
                conversationId
        );
    }

    private long getCustomerIdForAdminConversation(
            Connection connection,
            long adminId,
            long conversationId
    ) throws SQLException {

        String sql = """
                SELECT customer_id
                FROM conversations
                WHERE id = ?
                  AND admin_id = ?
                LIMIT 1
                """;

        try (
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setLong(
                    1,
                    conversationId
            );

            statement.setLong(
                    2,
                    adminId
            );

            try (
                    ResultSet rs =
                            statement.executeQuery()
            ) {

                if (!rs.next()) {

                    throw new IllegalArgumentException(
                            "Bạn không có quyền truy cập hội thoại này."
                    );
                }

                return rs.getLong(
                        "customer_id"
                );
            }
        }
    }

    private String findUserName(
            Connection connection,
            long userId
    ) throws SQLException {

        String sql = """
                SELECT name
                FROM users
                WHERE id = ?
                """;

        try (
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setLong(
                    1,
                    userId
            );

            try (
                    ResultSet rs =
                            statement.executeQuery()
            ) {

                if (!rs.next()) {
                    return "Admin";
                }

                return rs.getString(
                        "name"
                );
            }
        }
    }

    private Map<String, Object> conversationMap(
            long id,
            long customerId,
            long adminId,
            String adminName
    ) {

        Map<String, Object> result =
                new LinkedHashMap<>();

        result.put(
                "id",
                id
        );

        result.put(
                "customerId",
                customerId
        );

        result.put(
                "adminId",
                adminId
        );

        result.put(
                "adminName",
                adminName
        );

        return result;
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