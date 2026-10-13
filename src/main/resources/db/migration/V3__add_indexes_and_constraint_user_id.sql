ALTER TABLE carts ADD CONSTRAINT fk_carts_users FOREIGN KEY (user_id) REFERENCES users(id);

CREATE INDEX idx_cart_items_product ON cart_items(product_id);
CREATE INDEX idx_order_items_product ON order_items(product_id);