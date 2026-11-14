-- ============================================================
-- FASHION SHOP DESKTOP APPLICATION
-- Seed Data
-- Database: fashion_shop
-- MariaDB / MySQL
-- ============================================================

USE fashion_shop;

-- ============================================================
-- 1. USERS
-- ============================================================

INSERT INTO users
    (name, email, password_hash, phone, role, status)
VALUES
    (
        'Fashion Shop Admin',
        'admin@fashion.local',
        '$2a$12$ZBikl//qhDD7MihSQjKEc.WV/J5u.iqmsOOOkxLLhzN6y/RZba28y',
        '0900000001',
        'ADMIN',
        'ACTIVE'
    ),
    (
        'Nguyen Van User 1',
        'user1@fashion.local',
        '$2a$12$UcpKiDbKLf20xIAoqAMqw.vrB4qwFVYTukixYqAixYIVCmwtW1mrm',
        '0900000002',
        'CUSTOMER',
        'ACTIVE'
    ),
    (
        'Nguyen Van User 2',
        'user2@fashion.local',
        '$2a$12$UcpKiDbKLf20xIAoqAMqw.vrB4qwFVYTukixYqAixYIVCmwtW1mrm',
        '0900000003',
        'CUSTOMER',
        'ACTIVE'
    );

-- ============================================================
-- 2. CATEGORIES
-- ============================================================

INSERT INTO categories
    (name, description, status)
VALUES
    ('Áo', 'Các loại áo thời trang', 'ACTIVE'),
    ('Quần', 'Các loại quần thời trang', 'ACTIVE'),
    ('Váy', 'Các loại váy thời trang', 'ACTIVE'),
    ('Phụ kiện', 'Phụ kiện thời trang', 'ACTIVE');

-- ============================================================
-- 3. PRODUCTS
-- ============================================================

INSERT INTO products
    (category_id, name, description, price, image_path, size, color, stock, status)
VALUES
    (
        1,
        'Áo thun basic Premium',
        'Áo thun basic phong cách tối giản, phù hợp mặc hàng ngày.',
        249000,
        'products/ao-thun-basic.jpg',
        'M,L,XL',
        'Đen',
        30,
        'ACTIVE'
    ),
    (
        1,
        'Áo sơ mi Oxford',
        'Áo sơ mi Oxford thanh lịch, phù hợp đi học và đi làm.',
        399000,
        'products/ao-so-mi-oxford.jpg',
        'M,L,XL',
        'Trắng',
        20,
        'ACTIVE'
    ),
    (
        2,
        'Quần jean Slim Fit',
        'Quần jean dáng Slim Fit hiện đại.',
        499000,
        'products/quan-jean-slim-fit.jpg',
        '29,30,31,32',
        'Xanh',
        25,
        'ACTIVE'
    ),
    (
        2,
        'Quần kaki Basic',
        'Quần kaki cơ bản, dễ phối đồ.',
        429000,
        'products/quan-kaki-basic.jpg',
        '29,30,31,32',
        'Be',
        18,
        'ACTIVE'
    ),
    (
        3,
        'Váy Midi thanh lịch',
        'Váy Midi phong cách thanh lịch.',
        599000,
        'products/vay-midi.jpg',
        'S,M,L',
        'Đen',
        15,
        'ACTIVE'
    ),
    (
        3,
        'Váy hoa mùa hè',
        'Váy họa tiết hoa nhẹ nhàng cho mùa hè.',
        549000,
        'products/vay-hoa.jpg',
        'S,M,L',
        'Hoa',
        12,
        'ACTIVE'
    ),
    (
        4,
        'Túi tote Canvas',
        'Túi tote Canvas đơn giản, tiện dụng.',
        199000,
        'products/tui-tote.jpg',
        'FREE',
        'Kem',
        35,
        'ACTIVE'
    ),
    (
        4,
        'Mũ lưỡi trai Basic',
        'Mũ lưỡi trai phong cách casual.',
        159000,
        'products/mu-luoi-trai.jpg',
        'FREE',
        'Đen',
        40,
        'ACTIVE'
    );

-- ============================================================
-- 4. CARTS
-- ============================================================

INSERT INTO carts
    (user_id)
SELECT id
FROM users
WHERE email IN (
    'user1@fashion.local',
    'user2@fashion.local'
);

-- ============================================================
-- 5. CART ITEMS
-- ============================================================

INSERT INTO cart_items
    (cart_id, product_id, quantity)
SELECT
    c.id,
    p.id,
    1
FROM carts c
JOIN users u ON u.id = c.user_id
JOIN products p ON p.name = 'Áo thun basic Premium'
WHERE u.email = 'user1@fashion.local';

-- ============================================================
-- 6. ORDERS
-- ============================================================

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
SELECT
    u.id,
    748000,
    'Nguyen Van User 1',
    '0900000002',
    'Da Nang, Viet Nam',
    'Giao trong giờ hành chính',
    'COD',
    'DELIVERED'
FROM users u
WHERE u.email = 'user1@fashion.local';

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
SELECT
    u.id,
    499000,
    'Nguyen Van User 2',
    '0900000003',
    'Da Nang, Viet Nam',
    'Vui lòng kiểm tra hàng trước khi nhận',
    'COD',
    'PROCESSING'
FROM users u
WHERE u.email = 'user2@fashion.local';

-- ============================================================
-- 7. ORDER ITEMS
-- ============================================================

INSERT INTO order_items
    (order_id, product_id, quantity, price)
SELECT
    o.id,
    p.id,
    1,
    249000
FROM orders o
JOIN users u ON u.id = o.user_id
JOIN products p ON p.name = 'Áo thun basic Premium'
WHERE u.email = 'user1@fashion.local'
ORDER BY o.id
LIMIT 1;

INSERT INTO order_items
    (order_id, product_id, quantity, price)
SELECT
    o.id,
    p.id,
    1,
    499000
FROM orders o
JOIN users u ON u.id = o.user_id
JOIN products p ON p.name = 'Quần jean Slim Fit'
WHERE u.email = 'user1@fashion.local'
ORDER BY o.id
LIMIT 1;

INSERT INTO order_items
    (order_id, product_id, quantity, price)
SELECT
    o.id,
    p.id,
    1,
    499000
FROM orders o
JOIN users u ON u.id = o.user_id
JOIN products p ON p.name = 'Quần jean Slim Fit'
WHERE u.email = 'user2@fashion.local'
ORDER BY o.id DESC
LIMIT 1;

-- ============================================================
-- 8. NOTIFICATIONS
-- ============================================================

INSERT INTO notifications
    (user_id, title, message, type, is_read)
SELECT
    u.id,
    'Đơn hàng đã giao thành công',
    'Đơn hàng của bạn đã được giao thành công.',
    'ORDER',
    FALSE
FROM users u
WHERE u.email = 'user1@fashion.local';

INSERT INTO notifications
    (user_id, title, message, type, is_read)
SELECT
    u.id,
    'Đơn hàng đang được xử lý',
    'Đơn hàng của bạn đang được cửa hàng xử lý.',
    'ORDER',
    FALSE
FROM users u
WHERE u.email = 'user2@fashion.local';

-- ============================================================
-- 9. INVENTORY TRANSACTIONS
-- ============================================================

INSERT INTO inventory_transactions
    (
        product_id,
        user_id,
        type,
        quantity,
        stock_before,
        stock_after,
        reason
    )
SELECT
    p.id,
    u.id,
    'IMPORT',
    p.stock,
    0,
    p.stock,
    'Initial stock'
FROM products p
CROSS JOIN (
    SELECT id
    FROM users
    WHERE email = 'admin@fashion.local'
    LIMIT 1
) u;

-- ============================================================
-- DONE
-- ============================================================