-- Run once after migrating size/color on the store database.
USE fashion_shop;

CREATE TABLE IF NOT EXISTS product_variants (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    size_value VARCHAR(100) COLLATE utf8mb4_bin NOT NULL,
    color_value VARCHAR(100) COLLATE utf8mb4_bin NOT NULL,
    stock INT NOT NULL DEFAULT 0 CHECK (stock >= 0),
    UNIQUE KEY uq_product_variant (product_id, size_value, color_value),
    CONSTRAINT fk_variant_product FOREIGN KEY (product_id) REFERENCES products(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

ALTER TABLE order_items
    ADD COLUMN variant_id BIGINT NULL,
    ADD CONSTRAINT fk_order_items_variant FOREIGN KEY (variant_id) REFERENCES product_variants(id);
