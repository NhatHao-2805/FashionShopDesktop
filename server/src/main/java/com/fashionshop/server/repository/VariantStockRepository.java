package com.fashionshop.server.repository;

import com.fashionshop.server.database.DatabaseConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class VariantStockRepository {

    @FunctionalInterface
    interface ConnectionProvider {
        Connection open() throws SQLException;
    }

    public record Variant(long id, String size, String color, int stock) {}
    public record Snapshot(int total, String sizes, String colors, List<Variant> variants) {}
    public record Allocation(String size, String color, Integer stock) {}
    private record Choice(String size, String color) {}
    private final ConnectionProvider connections;

    public VariantStockRepository() {
        this(DatabaseConnection::getConnection);
    }

    VariantStockRepository(ConnectionProvider connections) {
        this.connections = connections;
    }

    public Snapshot get(long productId) throws SQLException {
        try (var connection = connections.open()) {
            connection.setAutoCommit(false);
            var result = readLocked(connection, productId);
            connection.commit();
            return result;
        }
    }

    public Snapshot allocate(long productId, Snapshot expected, List<Allocation> allocations) throws SQLException {
        if (expected == null || allocations == null || allocations.isEmpty()) {
            throw new IllegalArgumentException("Cần nhập tồn kho cho từng size và màu.");
        }
        try (var connection = connections.open()) {
            connection.setAutoCommit(false);
            try {
                var current = readLocked(connection, productId);
                if (!current.equals(expected)) {
                    throw new IllegalArgumentException("Tồn kho hoặc cấu hình sản phẩm đã thay đổi. Hãy mở lại và kiểm tra.");
                }

                var sizes = options(current.sizes());
                var colors = options(current.colors());
                Set<Choice> seen = new HashSet<>();
                long allocated = 0;
                for (var allocation : allocations) {
                    if (allocation == null || allocation.stock() == null || allocation.stock() < 0
                            || !sizes.contains(allocation.size()) || !colors.contains(allocation.color())
                            || !seen.add(new Choice(allocation.size(), allocation.color()))) {
                        throw new IllegalArgumentException("Size, màu hoặc số lượng phân bổ không hợp lệ.");
                    }
                    allocated += allocation.stock();
                }
                if (seen.size() != sizes.size() * colors.size() || allocated > current.total()) {
                    throw new IllegalArgumentException("Cần nhập đủ các tổ hợp và tổng phân bổ không được vượt tồn kho.");
                }

                try (var statement = connection.prepareStatement("""
                        INSERT INTO product_variants (product_id, size_value, color_value, stock)
                        VALUES (?, ?, ?, ?)
                        ON DUPLICATE KEY UPDATE stock = ?
                        """)) {
                    for (var allocation : allocations) {
                        statement.setLong(1, productId);
                        statement.setString(2, allocation.size());
                        statement.setString(3, allocation.color());
                        statement.setInt(4, allocation.stock());
                        statement.setInt(5, allocation.stock());
                        statement.addBatch();
                    }
                    statement.executeBatch();
                }
                var result = readLocked(connection, productId);
                connection.commit();
                return result;
            } catch (SQLException | RuntimeException error) {
                connection.rollback();
                throw error;
            }
        }
    }

    static Snapshot readLocked(Connection connection, long productId) throws SQLException {
        try (var product = connection.prepareStatement("""
                SELECT stock, size, color FROM products WHERE id = ? FOR UPDATE
                """)) {
            product.setLong(1, productId);
            try (var row = product.executeQuery()) {
                if (!row.next()) {
                    throw new IllegalArgumentException("Không tìm thấy sản phẩm.");
                }
                return new Snapshot(row.getInt("stock"), row.getString("size"), row.getString("color"),
                        listLocked(connection, productId));
            }
        }
    }

    static List<Variant> listLocked(Connection connection, long productId) throws SQLException {
        try (var statement = connection.prepareStatement("""
                SELECT id, size_value, color_value, stock
                FROM product_variants WHERE product_id = ? ORDER BY id FOR UPDATE
                """)) {
            statement.setLong(1, productId);
            try (var rows = statement.executeQuery()) {
                List<Variant> variants = new ArrayList<>();
                while (rows.next()) {
                    variants.add(new Variant(rows.getLong("id"), rows.getString("size_value"),
                            rows.getString("color_value"), rows.getInt("stock")));
                }
                return variants;
            }
        }
    }

    static Long reserve(Connection connection, long productId, String size, String color, int quantity)
            throws SQLException {
        var variants = listLocked(connection, productId);
        if (variants.isEmpty()) {
            return null;
        }
        String sizeValue = size == null ? "" : size.trim();
        String colorValue = color == null ? "" : color.trim();
        var variant = variants.stream()
                .filter(row -> row.size().equals(sizeValue) && row.color().equals(colorValue))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Size/màu đã chọn chưa có hàng."));
        if (variant.stock() < quantity) {
            throw new IllegalArgumentException("Size/màu đã chọn chỉ còn " + variant.stock() + " sản phẩm.");
        }
        change(connection, variant.id(), productId, -quantity);
        return variant.id();
    }

    static void change(Connection connection, long variantId, long productId, int delta) throws SQLException {
        try (var statement = connection.prepareStatement("""
                UPDATE product_variants SET stock = stock + ? WHERE id = ? AND product_id = ?
                """)) {
            statement.setInt(1, delta);
            statement.setLong(2, variantId);
            statement.setLong(3, productId);
            if (statement.executeUpdate() != 1) {
                throw new SQLException("Không thể cập nhật tồn kho biến thể.");
            }
        }
    }

    static List<String> options(String csv) {
        var values = csv == null ? List.<String>of() : Arrays.stream(csv.split(","))
                .map(String::trim).filter(value -> !value.isEmpty()).distinct().toList();
        return values.isEmpty() ? List.of("") : values;
    }
}
