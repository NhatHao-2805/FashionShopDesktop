-- Run once for an existing database. Does not delete existing orders.
USE fashion_shop;
ALTER TABLE order_items
    ADD COLUMN selected_size VARCHAR(100) NULL,
    ADD COLUMN selected_color VARCHAR(100) NULL;
