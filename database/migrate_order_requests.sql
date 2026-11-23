-- Adds durable checkout request keys without changing existing orders.
USE fashion_shop;

CREATE TABLE IF NOT EXISTS order_requests (
    user_id BIGINT NOT NULL,
    request_id CHAR(36) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    fingerprint CHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    order_id BIGINT NULL,
    PRIMARY KEY (user_id, request_id),
    UNIQUE KEY uq_order_requests_order (order_id),
    CONSTRAINT fk_order_requests_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_order_requests_order FOREIGN KEY (order_id) REFERENCES orders(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
